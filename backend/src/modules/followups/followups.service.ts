import { BadRequestException, ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { FollowUpPlan, Prisma } from '@prisma/client';
import { ok, page } from '../../common/api-response';
import { CurrentUser } from '../../common/current-user';
import { PrismaService } from '../../prisma/prisma.service';
import { AuditService } from '../audit/audit.service';
import { CreateFollowUpPlanDto } from './dto/create-follow-up-plan.dto';
import { CreateFollowUpRecordDto } from './dto/create-follow-up-record.dto';
import { FollowUpPlanQueryDto } from './dto/follow-up-plan-query.dto';
import { FollowUpRecordQueryDto } from './dto/follow-up-record-query.dto';
import { UpdateFollowUpPlanDto } from './dto/update-follow-up-plan.dto';
import { UpdateFollowUpRecordDto } from './dto/update-follow-up-record.dto';

type FollowUpRecordSummaryInput = {
  currentTreatment?: string | null;
  imagingSummary?: string | null;
  tumorMarkerSummary?: string | null;
  adverseReactions?: string | null;
  hasRecurrence?: boolean | null;
  recurrenceSite?: string | null;
  hasMetastasis?: boolean | null;
  metastasisSite?: string | null;
  remark?: string | null;
};

@Injectable()
export class FollowupsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly audit: AuditService,
  ) {}

  async listPlans(currentUser: CurrentUser, query: FollowUpPlanQueryDto) {
    await this.markOverduePlans(currentUser.departmentId);

    const skip = (query.page - 1) * query.pageSize;
    const where: Prisma.FollowUpPlanWhereInput = {
      departmentId: currentUser.departmentId,
      deletedAt: null,
      ...(query.status ? { status: query.status } : {}),
      ...(query.assigneeId ? { assigneeId: query.assigneeId } : {}),
      ...(query.patientId ? { patientId: query.patientId } : {}),
      ...(query.caseId ? { caseId: query.caseId } : {}),
      ...(query.plannedStart || query.plannedEnd
        ? {
            plannedDate: {
              ...(query.plannedStart ? { gte: new Date(query.plannedStart) } : {}),
              ...(query.plannedEnd ? { lte: new Date(query.plannedEnd) } : {}),
            },
          }
        : {}),
    };

    const [items, total] = await this.prisma.$transaction([
      this.prisma.followUpPlan.findMany({
        where,
        include: {
          clinicalCase: { include: { patient: true } },
        },
        orderBy: { plannedDate: 'asc' },
        skip,
        take: query.pageSize,
      }),
      this.prisma.followUpPlan.count({ where }),
    ]);

    return page(items, query.page, query.pageSize, total);
  }

  async todayPlans(currentUser: CurrentUser) {
    await this.markOverduePlans(currentUser.departmentId);
    const { start, end } = this.todayRange();
    const plans = await this.prisma.followUpPlan.findMany({
      where: {
        departmentId: currentUser.departmentId,
        deletedAt: null,
        status: 'pending',
        plannedDate: { gte: start, lte: end },
      },
      include: {
        clinicalCase: { include: { patient: true } },
      },
      orderBy: { plannedDate: 'asc' },
    });

    return ok(plans);
  }

  async overduePlans(currentUser: CurrentUser) {
    await this.markOverduePlans(currentUser.departmentId);
    const plans = await this.prisma.followUpPlan.findMany({
      where: {
        departmentId: currentUser.departmentId,
        deletedAt: null,
        status: 'overdue',
      },
      include: {
        clinicalCase: { include: { patient: true } },
      },
      orderBy: { plannedDate: 'asc' },
    });

    return ok(plans);
  }

  async createPlan(currentUser: CurrentUser, body: CreateFollowUpPlanDto) {
    await this.ensurePatientAndCaseMatch(currentUser, body.patientId, body.caseId);
    await this.ensureCaseWritable(currentUser, body.caseId);
    if (body.assigneeId) {
      await this.ensureUserExists(currentUser, body.assigneeId);
    }

    const plan = await this.prisma.followUpPlan.create({
      data: {
        departmentId: currentUser.departmentId,
        patientId: body.patientId,
        caseId: body.caseId,
        followUpType: body.followUpType,
        plannedDate: new Date(body.plannedDate),
        assigneeId: body.assigneeId,
        status: 'pending',
        createdBy: currentUser.id,
        updatedBy: currentUser.id,
      },
    });

    await this.audit.write({
      currentUser,
      action: 'FOLLOW_UP_PLAN_CREATE',
      objectType: 'FollowUpPlan',
      objectId: plan.id,
      afterSnapshot: plan,
    });

    return ok(plan);
  }

  async updatePlan(
    currentUser: CurrentUser,
    planId: string,
    body: UpdateFollowUpPlanDto,
  ) {
    const before = await this.findPlanById(currentUser, planId);
    await this.ensureCaseWritable(currentUser, before.caseId);
    if (body.assigneeId) {
      await this.ensureUserExists(currentUser, body.assigneeId);
    }

    const plan = await this.prisma.followUpPlan.update({
      where: { id: planId },
      data: {
        followUpType: body.followUpType,
        plannedDate: body.plannedDate ? new Date(body.plannedDate) : undefined,
        assigneeId: body.assigneeId,
        status: body.status,
        updatedBy: currentUser.id,
      },
    });

    await this.audit.write({
      currentUser,
      action: 'FOLLOW_UP_PLAN_UPDATE',
      objectType: 'FollowUpPlan',
      objectId: plan.id,
      beforeSnapshot: before,
      afterSnapshot: plan,
    });

    return ok(plan);
  }

  async markLost(currentUser: CurrentUser, planId: string) {
    const before = await this.findPlanById(currentUser, planId);
    await this.ensureCaseWritable(currentUser, before.caseId);
    const plan = await this.prisma.followUpPlan.update({
      where: { id: planId },
      data: {
        status: 'lost',
        updatedBy: currentUser.id,
      },
    });

    await this.audit.write({
      currentUser,
      action: 'FOLLOW_UP_PLAN_MARK_LOST',
      objectType: 'FollowUpPlan',
      objectId: plan.id,
      beforeSnapshot: before,
      afterSnapshot: plan,
    });

    return ok(plan);
  }

  async deletePlan(currentUser: CurrentUser, planId: string) {
    const before = await this.findPlanById(currentUser, planId);
    const plan = await this.prisma.followUpPlan.update({
      where: { id: planId },
      data: {
        deletedAt: new Date(),
        updatedBy: currentUser.id,
      },
    });

    await this.audit.write({
      currentUser,
      action: 'FOLLOW_UP_PLAN_DELETE',
      objectType: 'FollowUpPlan',
      objectId: plan.id,
      beforeSnapshot: before,
      afterSnapshot: { id: plan.id, deletedAt: plan.deletedAt },
    });

    return ok({ id: plan.id, deleted: true });
  }

  async createRecord(currentUser: CurrentUser, body: CreateFollowUpRecordDto) {
    const plan = await this.findPlanById(currentUser, body.followUpPlanId);
    await this.ensureCaseWritable(currentUser, plan.caseId);
    if (plan.status === 'completed') {
      throw new BadRequestException('Follow-up plan is already completed');
    }

    const result = await this.prisma.$transaction(async (tx) => {
      const summary = body.summary ?? this.buildRecordSummary(body);
      const record = await tx.followUpRecord.create({
        data: {
          departmentId: currentUser.departmentId,
          followUpPlanId: plan.id,
          patientId: plan.patientId,
          caseId: plan.caseId,
          actualDate: new Date(body.actualDate),
          method: body.method,
          survivalStatus: body.survivalStatus,
          hasRecurrence: body.hasRecurrence,
          recurrenceSite: body.recurrenceSite,
          hasMetastasis: body.hasMetastasis,
          metastasisSite: body.metastasisSite,
          imagingSummary: body.imagingSummary,
          tumorMarkerSummary: body.tumorMarkerSummary,
          currentTreatment: body.currentTreatment,
          adverseReactions: body.adverseReactions,
          remark: body.remark,
          summary,
          nextFollowUpDate: body.nextFollowUpDate
            ? new Date(body.nextFollowUpDate)
            : undefined,
          recordedBy: currentUser.id,
        },
      });

      await tx.followUpPlan.update({
        where: { id: plan.id },
        data: {
          status: 'completed',
          updatedBy: currentUser.id,
        },
      });

      let nextPlan: FollowUpPlan | null = null;
      if (body.nextFollowUpDate) {
        nextPlan = await tx.followUpPlan.create({
          data: {
            departmentId: currentUser.departmentId,
            patientId: plan.patientId,
            caseId: plan.caseId,
            followUpType: plan.followUpType,
            plannedDate: new Date(body.nextFollowUpDate),
            assigneeId: plan.assigneeId,
            status: 'pending',
            createdBy: currentUser.id,
            updatedBy: currentUser.id,
          },
        });
      }

      return { record, nextPlan };
    });

    await this.audit.write({
      currentUser,
      action: 'FOLLOW_UP_RECORD_CREATE',
      objectType: 'FollowUpRecord',
      objectId: result.record.id,
      beforeSnapshot: { plan },
      afterSnapshot: result,
    });

    return ok(result);
  }

  async listRecords(currentUser: CurrentUser, query: FollowUpRecordQueryDto) {
    const records = await this.prisma.followUpRecord.findMany({
      where: {
        departmentId: currentUser.departmentId,
        deletedAt: null,
        ...(query.patientId ? { patientId: query.patientId } : {}),
        ...(query.caseId ? { caseId: query.caseId } : {}),
      },
      orderBy: { actualDate: 'desc' },
    });

    return ok({ items: records });
  }

  async recordDetail(currentUser: CurrentUser, recordId: string) {
    const record = await this.prisma.followUpRecord.findFirst({
      where: {
        id: recordId,
        departmentId: currentUser.departmentId,
        deletedAt: null,
      },
    });
    if (!record) {
      throw new NotFoundException('Follow-up record not found');
    }

    return ok(record);
  }

  async updateRecord(
    currentUser: CurrentUser,
    recordId: string,
    body: UpdateFollowUpRecordDto,
  ) {
    const existing = await this.prisma.followUpRecord.findFirst({
      where: {
        id: recordId,
        departmentId: currentUser.departmentId,
        deletedAt: null,
      },
    });
    if (!existing) {
      throw new NotFoundException('Follow-up record not found');
    }
    await this.ensureCaseWritable(currentUser, existing.caseId);

    const record = await this.prisma.followUpRecord.update({
      where: { id: recordId },
      data: {
        actualDate: body.actualDate ? new Date(body.actualDate) : undefined,
        method: body.method,
        survivalStatus: body.survivalStatus,
        hasRecurrence: body.hasRecurrence,
        recurrenceSite: body.recurrenceSite,
        hasMetastasis: body.hasMetastasis,
        metastasisSite: body.metastasisSite,
        imagingSummary: body.imagingSummary,
        tumorMarkerSummary: body.tumorMarkerSummary,
        currentTreatment: body.currentTreatment,
        adverseReactions: body.adverseReactions,
        remark: body.remark,
        summary: body.summary ?? this.buildRecordSummary({ ...existing, ...body }),
        nextFollowUpDate: body.nextFollowUpDate
          ? new Date(body.nextFollowUpDate)
          : undefined,
      },
    });

    await this.audit.write({
      currentUser,
      action: 'FOLLOW_UP_RECORD_UPDATE',
      objectType: 'FollowUpRecord',
      objectId: record.id,
      beforeSnapshot: existing,
      afterSnapshot: record,
    });

    return ok(record);
  }

  async deleteRecord(currentUser: CurrentUser, recordId: string) {
    const existing = await this.prisma.followUpRecord.findFirst({
      where: {
        id: recordId,
        departmentId: currentUser.departmentId,
        deletedAt: null,
      },
    });
    if (!existing) {
      throw new NotFoundException('Follow-up record not found');
    }

    const record = await this.prisma.followUpRecord.update({
      where: { id: recordId },
      data: { deletedAt: new Date() },
    });

    await this.audit.write({
      currentUser,
      action: 'FOLLOW_UP_RECORD_DELETE',
      objectType: 'FollowUpRecord',
      objectId: record.id,
      beforeSnapshot: existing,
      afterSnapshot: { id: record.id, deletedAt: record.deletedAt },
    });

    return ok({ id: record.id, deleted: true });
  }

  private async ensurePatientAndCaseMatch(
    currentUser: CurrentUser,
    patientId: string,
    caseId: string,
  ) {
    const clinicalCase = await this.prisma.clinicalCase.findFirst({
      where: {
        id: caseId,
        patientId,
        departmentId: currentUser.departmentId,
        deletedAt: null,
      },
      select: { id: true },
    });
    if (!clinicalCase) {
      throw new NotFoundException('Patient or case not found');
    }
  }

  private async ensureCaseWritable(currentUser: CurrentUser, caseId: string) {
    const clinicalCase = await this.prisma.clinicalCase.findFirst({
      where: {
        id: caseId,
        departmentId: currentUser.departmentId,
        deletedAt: null,
      },
      select: { caseStatus: true },
    });
    if (!clinicalCase) {
      throw new NotFoundException('Case not found');
    }
    if (clinicalCase.caseStatus === 'archived' && currentUser.role !== 'admin') {
      throw new ForbiddenException('Archived cases can only be modified by admin');
    }
  }

  private async ensureUserExists(currentUser: CurrentUser, userId: string) {
    const user = await this.prisma.user.findFirst({
      where: {
        id: userId,
        departmentId: currentUser.departmentId,
        deletedAt: null,
        status: 'active',
      },
      select: { id: true },
    });
    if (!user) {
      throw new NotFoundException('Assignee not found');
    }
  }

  private async ensurePlanExists(currentUser: CurrentUser, planId: string) {
    await this.findPlanById(currentUser, planId);
  }

  private async findPlanById(currentUser: CurrentUser, planId: string) {
    const plan = await this.prisma.followUpPlan.findFirst({
      where: {
        id: planId,
        departmentId: currentUser.departmentId,
        deletedAt: null,
      },
    });
    if (!plan) {
      throw new NotFoundException('Follow-up plan not found');
    }

    return plan;
  }

  private async markOverduePlans(departmentId: string) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    await this.prisma.followUpPlan.updateMany({
      where: {
        departmentId,
        deletedAt: null,
        status: 'pending',
        plannedDate: { lt: today },
      },
      data: { status: 'overdue' },
    });
  }

  private todayRange() {
    const start = new Date();
    start.setHours(0, 0, 0, 0);
    const end = new Date(start);
    end.setHours(23, 59, 59, 999);
    return { start, end };
  }

  private buildRecordSummary(record: FollowUpRecordSummaryInput): string {
    const parts = [
      record.currentTreatment ? `当前治疗：${record.currentTreatment}` : undefined,
      record.imagingSummary ? `影像：${record.imagingSummary}` : undefined,
      record.tumorMarkerSummary ? `肿瘤标志物：${record.tumorMarkerSummary}` : undefined,
      record.adverseReactions ? `不良反应：${record.adverseReactions}` : undefined,
      record.hasRecurrence === true ? `复发：${record.recurrenceSite || '有'}` : undefined,
      record.hasMetastasis === true ? `转移：${record.metastasisSite || '有'}` : undefined,
      record.remark,
    ].filter(Boolean);

    return parts.join('；') || '待生成 AI 摘要';
  }
}
