import { ConflictException, Injectable, NotFoundException } from '@nestjs/common';
import { ok } from '../../common/api-response';
import { CurrentUser } from '../../common/current-user';
import { PrismaService } from '../../prisma/prisma.service';
import { AuditService } from '../audit/audit.service';
import { CreateTagDto } from './dto/create-tag.dto';
import { UpdateTagDto } from './dto/update-tag.dto';

@Injectable()
export class TagsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly audit: AuditService,
  ) {}

  async list(currentUser: CurrentUser) {
    const tags = await this.prisma.tag.findMany({
      where: {
        departmentId: currentUser.departmentId,
        deletedAt: null,
      },
      orderBy: [{ createdAt: 'desc' }],
    });

    return ok(tags);
  }

  async create(currentUser: CurrentUser, body: CreateTagDto) {
    await this.ensureNameAvailable(currentUser, body.name);
    const tag = await this.prisma.tag.create({
      data: {
        departmentId: currentUser.departmentId,
        name: body.name,
        color: body.color,
        createdBy: currentUser.id,
      },
    });

    await this.audit.write({
      currentUser,
      action: 'TAG_CREATE',
      objectType: 'Tag',
      objectId: tag.id,
      afterSnapshot: tag,
    });

    return ok(tag);
  }

  async update(currentUser: CurrentUser, tagId: string, body: UpdateTagDto) {
    const before = await this.ensureTagExists(currentUser, tagId);
    if (body.name) {
      await this.ensureNameAvailable(currentUser, body.name, tagId);
    }

    const tag = await this.prisma.tag.update({
      where: { id: tagId },
      data: body,
    });

    await this.audit.write({
      currentUser,
      action: 'TAG_UPDATE',
      objectType: 'Tag',
      objectId: tag.id,
      beforeSnapshot: before,
      afterSnapshot: tag,
    });

    return ok(tag);
  }

  async delete(currentUser: CurrentUser, tagId: string) {
    const before = await this.ensureTagExists(currentUser, tagId);
    await this.prisma.$transaction([
      this.prisma.caseTagRelation.deleteMany({
        where: {
          departmentId: currentUser.departmentId,
          tagId,
        },
      }),
      this.prisma.tag.update({
        where: { id: tagId },
        data: { deletedAt: new Date() },
      }),
    ]);

    await this.audit.write({
      currentUser,
      action: 'TAG_DELETE',
      objectType: 'Tag',
      objectId: tagId,
      beforeSnapshot: before,
      afterSnapshot: { id: tagId, deleted: true },
    });

    return ok({ id: tagId, deleted: true });
  }

  private async ensureNameAvailable(
    currentUser: CurrentUser,
    name: string,
    excludeTagId?: string,
  ) {
    const existing = await this.prisma.tag.findFirst({
      where: {
        departmentId: currentUser.departmentId,
        name,
        deletedAt: null,
        ...(excludeTagId ? { id: { not: excludeTagId } } : {}),
      },
      select: { id: true },
    });
    if (existing) {
      throw new ConflictException('Tag name already exists in this department');
    }
  }

  private async ensureTagExists(currentUser: CurrentUser, tagId: string) {
    const tag = await this.prisma.tag.findFirst({
      where: {
        id: tagId,
        departmentId: currentUser.departmentId,
        deletedAt: null,
      },
    });
    if (!tag) {
      throw new NotFoundException('Tag not found');
    }

    return tag;
  }
}
