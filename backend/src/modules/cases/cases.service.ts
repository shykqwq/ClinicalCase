import { BadRequestException, ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { ClinicalCase, Patient, Prisma, Tag } from '@prisma/client';
import { ok, page } from '../../common/api-response';
import { CurrentUser } from '../../common/current-user';
import {
  canViewSensitivePatientData,
  maskIdentityNo,
  maskName,
  maskPhone,
} from '../../common/sensitive-data';
import { PrismaService } from '../../prisma/prisma.service';
import { AuditService } from '../audit/audit.service';
import { CaseQueryDto } from './dto/case-query.dto';
import { CreateCaseDto } from './dto/create-case.dto';
import { UpdateCaseDto } from './dto/update-case.dto';

type CaseWithRelations = ClinicalCase & {
  patient: Patient;
  tagRelations: Array<{ tag: Tag }>;
};

type ArchiveCandidate = {
  patientId?: unknown;
  title?: unknown;
  visitType?: unknown;
  laterality?: unknown;
  diseaseType?: unknown;
  currentStatus?: unknown;
};

@Injectable()
export class CasesService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly audit: AuditService,
  ) {}

  async list(currentUser: CurrentUser, query: CaseQueryDto) {
    const skip = (query.page - 1) * query.pageSize;
    const tagIds = query.tagIds?.split(',').filter(Boolean) ?? [];
    const where: Prisma.ClinicalCaseWhereInput = {
      departmentId: currentUser.departmentId,
      deletedAt: null,
      ...(query.patientId ? { patientId: query.patientId } : {}),
      ...(query.diseaseType ? { diseaseType: query.diseaseType } : {}),
      ...(query.laterality ? { laterality: query.laterality } : {}),
      ...(query.pathologyType ? { pathologyType: { contains: query.pathologyType } } : {}),
      ...(query.erStatus ? { erStatus: query.erStatus } : {}),
      ...(query.prStatus ? { prStatus: query.prStatus } : {}),
      ...(query.her2Status ? { her2Status: query.her2Status } : {}),
      ...(query.molecularSubtype ? { molecularSubtype: query.molecularSubtype } : {}),
      ...(query.clinicalStage ? { clinicalStage: query.clinicalStage } : {}),
      ...(query.pathologicalStage ? { pathologicalStage: query.pathologicalStage } : {}),
      ...(query.surgeryType ? { surgeryType: { contains: query.surgeryType } } : {}),
      ...(query.hasNeoadjuvantTherapy !== undefined
        ? { hasNeoadjuvantTherapy: query.hasNeoadjuvantTherapy }
        : {}),
      ...(query.hasRadiotherapy !== undefined ? { hasRadiotherapy: query.hasRadiotherapy } : {}),
      ...(query.hasTargetedTherapy !== undefined
        ? { hasTargetedTherapy: query.hasTargetedTherapy }
        : {}),
      ...(query.hasEndocrineTherapy !== undefined
        ? { hasEndocrineTherapy: query.hasEndocrineTherapy }
        : {}),
      ...(query.recurrenceStatus ? { recurrenceStatus: query.recurrenceStatus } : {}),
      ...(query.currentStatus ? { currentStatus: query.currentStatus } : {}),
      ...(query.caseStatus ? { caseStatus: query.caseStatus } : {}),
      ...(query.surgeryStart || query.surgeryEnd
        ? {
            surgeryDate: {
              ...(query.surgeryStart ? { gte: new Date(query.surgeryStart) } : {}),
              ...(query.surgeryEnd ? { lte: new Date(query.surgeryEnd) } : {}),
            },
          }
        : {}),
      ...(tagIds.length
        ? {
            tagRelations: {
              some: {
                tagId: { in: tagIds },
                departmentId: currentUser.departmentId,
              },
            },
          }
        : {}),
      ...(query.keyword
        ? {
            OR: [
              { title: { contains: query.keyword } },
              { preliminaryDiagnosis: { contains: query.keyword } },
              { confirmedDiagnosis: { contains: query.keyword } },
              { summary: { contains: query.keyword } },
              { patient: { name: { contains: query.keyword } } },
              { patient: { outpatientNo: { contains: query.keyword } } },
              { patient: { inpatientNo: { contains: query.keyword } } },
            ],
          }
        : {}),
    };

    const [items, total] = await this.prisma.$transaction([
      this.prisma.clinicalCase.findMany({
        where,
        include: {
          patient: true,
          tagRelations: { include: { tag: true } },
        },
        orderBy: { createdAt: 'desc' },
        skip,
        take: query.pageSize,
      }),
      this.prisma.clinicalCase.count({ where }),
    ]);

    return page(
      items.map((item) => this.serializeCase(currentUser, item)),
      query.page,
      query.pageSize,
      total,
    );
  }

  async create(currentUser: CurrentUser, body: CreateCaseDto) {
    await this.ensurePatientExists(currentUser, body.patientId);
    if (body.caseStatus === 'archived') {
      this.validateArchiveRequiredFields(body);
    }

    const clinicalCase = await this.prisma.clinicalCase.create({
      data: {
        ...this.toCaseData(body),
        departmentId: currentUser.departmentId,
        patientId: body.patientId,
        caseStatus: body.caseStatus ?? 'draft',
        archivedAt: body.caseStatus === 'archived' ? new Date() : undefined,
        createdBy: currentUser.id,
        updatedBy: currentUser.id,
      },
      include: {
        patient: true,
        tagRelations: { include: { tag: true } },
      },
    });

    await this.audit.write({
      currentUser,
      action: 'CASE_CREATE',
      objectType: 'ClinicalCase',
      objectId: clinicalCase.id,
      afterSnapshot: clinicalCase,
    });

    return ok(this.serializeCase(currentUser, clinicalCase));
  }

  async detail(currentUser: CurrentUser, caseId: string) {
    const clinicalCase = await this.findCaseById(currentUser, caseId);
    return ok(this.serializeCase(currentUser, clinicalCase));
  }

  async update(currentUser: CurrentUser, caseId: string, body: UpdateCaseDto) {
    const before = await this.findCaseById(currentUser, caseId);
    if (before.caseStatus === 'archived' && currentUser.role !== 'admin') {
      throw new ForbiddenException('Archived cases can only be modified by admin');
    }
    if (body.patientId) {
      await this.ensurePatientExists(currentUser, body.patientId);
    }
    if (body.caseStatus === 'archived') {
      const merged = await this.getMergedCaseForArchive(currentUser, caseId, body);
      this.validateArchiveRequiredFields(merged);
    }

    const clinicalCase = await this.prisma.clinicalCase.update({
      where: { id: caseId },
      data: {
        ...this.toCaseData(body),
        patientId: body.patientId,
        archivedAt: body.caseStatus === 'archived' ? new Date() : undefined,
        updatedBy: currentUser.id,
      },
      include: {
        patient: true,
        tagRelations: { include: { tag: true } },
      },
    });

    await this.audit.write({
      currentUser,
      action: body.caseStatus === 'archived' ? 'CASE_ARCHIVE' : 'CASE_UPDATE',
      objectType: 'ClinicalCase',
      objectId: clinicalCase.id,
      beforeSnapshot: before,
      afterSnapshot: clinicalCase,
    });

    return ok(this.serializeCase(currentUser, clinicalCase));
  }

  async archive(currentUser: CurrentUser, caseId: string) {
    const existing = await this.findCaseById(currentUser, caseId);
    this.validateArchiveRequiredFields(existing);

    const clinicalCase = await this.prisma.clinicalCase.update({
      where: { id: caseId },
      data: {
        caseStatus: 'archived',
        archivedAt: new Date(),
        updatedBy: currentUser.id,
      },
      include: {
        patient: true,
        tagRelations: { include: { tag: true } },
      },
    });

    await this.audit.write({
      currentUser,
      action: 'CASE_ARCHIVE',
      objectType: 'ClinicalCase',
      objectId: clinicalCase.id,
      beforeSnapshot: existing,
      afterSnapshot: clinicalCase,
    });

    return ok(this.serializeCase(currentUser, clinicalCase));
  }

  async setTags(currentUser: CurrentUser, caseId: string, tagIds: string[]) {
    const before = await this.findCaseById(currentUser, caseId);
    const tags = await this.prisma.tag.findMany({
      where: {
        id: { in: tagIds },
        departmentId: currentUser.departmentId,
        deletedAt: null,
      },
      select: { id: true },
    });
    if (tags.length !== new Set(tagIds).size) {
      throw new BadRequestException('Some tags do not exist in current department');
    }

    await this.prisma.$transaction([
      this.prisma.caseTagRelation.deleteMany({
        where: {
          caseId,
          departmentId: currentUser.departmentId,
        },
      }),
      ...tagIds.map((tagId) =>
        this.prisma.caseTagRelation.create({
          data: {
            departmentId: currentUser.departmentId,
            caseId,
            tagId,
            createdBy: currentUser.id,
          },
        }),
      ),
    ]);

    const after = await this.findCaseById(currentUser, caseId);
    await this.audit.write({
      currentUser,
      action: 'CASE_TAGS_SET',
      objectType: 'ClinicalCase',
      objectId: caseId,
      beforeSnapshot: before,
      afterSnapshot: after,
    });

    return ok(this.serializeCase(currentUser, after));
  }

  async delete(currentUser: CurrentUser, caseId: string) {
    const before = await this.findCaseById(currentUser, caseId);
    const clinicalCase = await this.prisma.clinicalCase.update({
      where: { id: caseId },
      data: {
        deletedAt: new Date(),
        updatedBy: currentUser.id,
      },
    });

    await this.audit.write({
      currentUser,
      action: 'CASE_DELETE',
      objectType: 'ClinicalCase',
      objectId: clinicalCase.id,
      beforeSnapshot: before,
      afterSnapshot: { id: clinicalCase.id, deletedAt: clinicalCase.deletedAt },
    });

    return ok({ id: clinicalCase.id, deleted: true });
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

  private async ensureCaseExists(currentUser: CurrentUser, caseId: string) {
    const clinicalCase = await this.prisma.clinicalCase.findFirst({
      where: {
        id: caseId,
        departmentId: currentUser.departmentId,
        deletedAt: null,
      },
      select: { id: true },
    });
    if (!clinicalCase) {
      throw new NotFoundException('Case not found');
    }
  }

  private async findCaseById(currentUser: CurrentUser, caseId: string) {
    const clinicalCase = await this.prisma.clinicalCase.findFirst({
      where: {
        id: caseId,
        departmentId: currentUser.departmentId,
        deletedAt: null,
      },
      include: {
        patient: true,
        tagRelations: { include: { tag: true } },
      },
    });
    if (!clinicalCase) {
      throw new NotFoundException('Case not found');
    }

    return clinicalCase;
  }

  private async getMergedCaseForArchive(
    currentUser: CurrentUser,
    caseId: string,
    body: UpdateCaseDto,
  ) {
    const existing = await this.findCaseById(currentUser, caseId);
    return { ...existing, ...body };
  }

  private validateArchiveRequiredFields(clinicalCase: ArchiveCandidate) {
    const missing = [
      ['patientId', clinicalCase.patientId],
      ['title', clinicalCase.title],
      ['visitType', clinicalCase.visitType],
      ['laterality', clinicalCase.laterality],
      ['diseaseType', clinicalCase.diseaseType],
      ['currentStatus', clinicalCase.currentStatus],
    ]
      .filter(([, value]) => !value)
      .map(([field]) => field);

    if (missing.length) {
      throw new BadRequestException({
        message: 'Missing required fields for archive',
        missing,
      });
    }
  }

  private toCaseData(body: Partial<CreateCaseDto>): Prisma.ClinicalCaseUncheckedCreateInput {
    return {
      title: body.title,
      visitType: body.visitType,
      laterality: body.laterality,
      diseaseType: body.diseaseType,
      preliminaryDiagnosis: body.preliminaryDiagnosis,
      confirmedDiagnosis: body.confirmedDiagnosis,
      pathologyType: body.pathologyType,
      erStatus: body.erStatus,
      prStatus: body.prStatus,
      her2Status: body.her2Status,
      ki67Percent: body.ki67Percent,
      molecularSubtype: body.molecularSubtype,
      clinicalTStage: body.clinicalTStage,
      clinicalNStage: body.clinicalNStage,
      clinicalMStage: body.clinicalMStage,
      pathologicalTStage: body.pathologicalTStage,
      pathologicalNStage: body.pathologicalNStage,
      pathologicalMStage: body.pathologicalMStage,
      clinicalStage: body.clinicalStage,
      pathologicalStage: body.pathologicalStage,
      hasNeoadjuvantTherapy: body.hasNeoadjuvantTherapy,
      neoadjuvantTherapyPlan: body.neoadjuvantTherapyPlan,
      surgeryDate: body.surgeryDate ? new Date(body.surgeryDate) : undefined,
      surgeryType: body.surgeryType,
      axillaryManagement: body.axillaryManagement,
      chemotherapyPlan: body.chemotherapyPlan,
      hasRadiotherapy: body.hasRadiotherapy,
      hasTargetedTherapy: body.hasTargetedTherapy,
      targetedTherapyPlan: body.targetedTherapyPlan,
      hasEndocrineTherapy: body.hasEndocrineTherapy,
      endocrineTherapyPlan: body.endocrineTherapyPlan,
      recurrenceStatus: body.recurrenceStatus,
      currentStatus: body.currentStatus,
      summary: body.summary,
      caseStatus: body.caseStatus,
    } as Prisma.ClinicalCaseUncheckedCreateInput;
  }

  private serializeCase(currentUser: CurrentUser, clinicalCase: CaseWithRelations) {
    const canViewSensitive = canViewSensitivePatientData(currentUser);
    return {
      ...clinicalCase,
      ki67Percent: clinicalCase.ki67Percent ? Number(clinicalCase.ki67Percent) : null,
      patient: {
        ...clinicalCase.patient,
        name: canViewSensitive
          ? clinicalCase.patient.name
          : maskName(clinicalCase.patient.name),
        phone: canViewSensitive
          ? clinicalCase.patient.phone
          : maskPhone(clinicalCase.patient.phone),
        identityNo: canViewSensitive
          ? clinicalCase.patient.identityNo
          : maskIdentityNo(clinicalCase.patient.identityNo),
      },
      tags: clinicalCase.tagRelations.map((relation) => relation.tag),
      tagRelations: undefined,
    };
  }
}
