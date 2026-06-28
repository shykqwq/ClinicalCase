import {
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { ok } from '../../common/api-response';
import { CurrentUser } from '../../common/current-user';
import { PrismaService } from '../../prisma/prisma.service';
import { AuditService } from '../audit/audit.service';
import { AttachmentQueryDto } from './dto/attachment-query.dto';
import { CreateAttachmentDto } from './dto/create-attachment.dto';
import { CreateUploadTokenDto } from './dto/create-upload-token.dto';

@Injectable()
export class AttachmentsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly configService: ConfigService,
    private readonly audit: AuditService,
  ) {}

  async createUploadToken(currentUser: CurrentUser, body: CreateUploadTokenDto) {
    await this.ensurePatientExists(currentUser, body.patientId);
    if (body.caseId) {
      await this.ensureCaseExists(currentUser, body.caseId, body.patientId);
    }

    const safeFilename = body.filename.replace(/[^\w.\-]+/g, '_');
    const objectKey = [
      'departments',
      currentUser.departmentId,
      'patients',
      body.patientId,
      body.caseId ? `cases/${body.caseId}` : 'general',
      `${Date.now()}-${safeFilename}`,
    ].join('/');

    return ok({
      uploadUrl: this.buildPlaceholderObjectUrl(objectKey),
      objectKey,
      expiresIn: 600,
      uploadMethod: 'PUT',
      note: 'P0 placeholder URL. Replace with real object storage signed URL before production use.',
    });
  }

  async create(currentUser: CurrentUser, body: CreateAttachmentDto) {
    await this.ensurePatientExists(currentUser, body.patientId);
    if (body.caseId) {
      await this.ensureCaseExists(currentUser, body.caseId, body.patientId);
    }

    const attachment = await this.prisma.attachment.create({
      data: {
        departmentId: currentUser.departmentId,
        patientId: body.patientId,
        caseId: body.caseId,
        category: body.category,
        originalFilename: body.originalFilename,
        contentType: body.contentType,
        fileSize: BigInt(body.fileSize),
        objectKey: body.objectKey,
        checksum: body.checksum,
        uploadedBy: currentUser.id,
      },
    });

    await this.audit.write({
      currentUser,
      action: 'ATTACHMENT_CREATE',
      objectType: 'Attachment',
      objectId: attachment.id,
      afterSnapshot: this.serializeAttachment(attachment),
    });

    return ok(this.serializeAttachment(attachment));
  }

  async list(currentUser: CurrentUser, query: AttachmentQueryDto) {
    const attachments = await this.prisma.attachment.findMany({
      where: {
        departmentId: currentUser.departmentId,
        deletedAt: null,
        ...(query.patientId ? { patientId: query.patientId } : {}),
        ...(query.caseId ? { caseId: query.caseId } : {}),
        ...(query.category ? { category: query.category } : {}),
      },
      orderBy: { uploadedAt: 'desc' },
    });

    return ok({ items: attachments.map((item) => this.serializeAttachment(item)) });
  }

  async previewUrl(currentUser: CurrentUser, attachmentId: string) {
    const attachment = await this.findAttachmentById(currentUser, attachmentId);
    return ok({
      id: attachment.id,
      url: this.buildPlaceholderObjectUrl(attachment.objectKey),
      expiresIn: 600,
      contentType: attachment.contentType,
      originalFilename: attachment.originalFilename,
      note: 'P0 placeholder URL. Replace with real object storage signed URL before production use.',
    });
  }

  async delete(currentUser: CurrentUser, attachmentId: string) {
    const attachment = await this.findAttachmentById(currentUser, attachmentId);
    if (currentUser.role !== 'admin' && attachment.uploadedBy !== currentUser.id) {
      throw new ForbiddenException('Only admin or uploader can delete this attachment');
    }

    const deleted = await this.prisma.attachment.update({
      where: { id: attachmentId },
      data: { deletedAt: new Date() },
    });

    await this.audit.write({
      currentUser,
      action: 'ATTACHMENT_DELETE',
      objectType: 'Attachment',
      objectId: attachmentId,
      beforeSnapshot: this.serializeAttachment(attachment),
      afterSnapshot: {
        id: deleted.id,
        deletedAt: deleted.deletedAt,
      },
    });

    return ok({ id: attachmentId, deleted: true });
  }

  private async ensurePatientExists(currentUser: CurrentUser, patientId: string) {
    const patient = await this.prisma.patient.findFirst({
      where: {
        id: patientId,
        departmentId: currentUser.departmentId,
        deletedAt: null,
      },
      select: { id: true },
    });
    if (!patient) {
      throw new NotFoundException('Patient not found');
    }
  }

  private async ensureCaseExists(
    currentUser: CurrentUser,
    caseId: string,
    patientId?: string,
  ) {
    const clinicalCase = await this.prisma.clinicalCase.findFirst({
      where: {
        id: caseId,
        departmentId: currentUser.departmentId,
        deletedAt: null,
        ...(patientId ? { patientId } : {}),
      },
      select: { id: true },
    });
    if (!clinicalCase) {
      throw new NotFoundException('Case not found');
    }
  }

  private async findAttachmentById(currentUser: CurrentUser, attachmentId: string) {
    const attachment = await this.prisma.attachment.findFirst({
      where: {
        id: attachmentId,
        departmentId: currentUser.departmentId,
        deletedAt: null,
      },
    });
    if (!attachment) {
      throw new NotFoundException('Attachment not found');
    }

    return attachment;
  }

  private buildPlaceholderObjectUrl(objectKey: string) {
    const endpoint = this.configService.get<string>('OBJECT_STORAGE_ENDPOINT');
    const bucket = this.configService.get<string>('OBJECT_STORAGE_BUCKET');
    if (endpoint && bucket) {
      return `${endpoint.replace(/\/$/, '')}/${bucket}/${objectKey}`;
    }

    return `object-storage:///${objectKey}`;
  }

  private serializeAttachment<T extends { fileSize: bigint }>(attachment: T) {
    return {
      ...attachment,
      fileSize: Number(attachment.fileSize),
    };
  }
}
