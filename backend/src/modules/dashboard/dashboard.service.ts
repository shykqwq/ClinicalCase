import { Injectable } from '@nestjs/common';
import { ok } from '../../common/api-response';
import { CurrentUser } from '../../common/current-user';
import { PrismaService } from '../../prisma/prisma.service';

@Injectable()
export class DashboardService {
  constructor(private readonly prisma: PrismaService) {}

  async summary(currentUser: CurrentUser) {
    await this.markOverduePlans(currentUser.departmentId);
    const { start, end } = this.todayRange();

    const [
      todayFollowUpCount,
      overdueFollowUpCount,
      totalPatientCount,
      totalCaseCount,
      recentCases,
    ] = await this.prisma.$transaction([
      this.prisma.followUpPlan.count({
        where: {
          departmentId: currentUser.departmentId,
          deletedAt: null,
          status: 'pending',
          plannedDate: { gte: start, lte: end },
        },
      }),
      this.prisma.followUpPlan.count({
        where: {
          departmentId: currentUser.departmentId,
          deletedAt: null,
          status: 'overdue',
        },
      }),
      this.prisma.patient.count({
        where: {
          departmentId: currentUser.departmentId,
          deletedAt: null,
        },
      }),
      this.prisma.clinicalCase.count({
        where: {
          departmentId: currentUser.departmentId,
          deletedAt: null,
        },
      }),
      this.prisma.clinicalCase.findMany({
        where: {
          departmentId: currentUser.departmentId,
          deletedAt: null,
        },
        include: {
          patient: true,
        },
        orderBy: { createdAt: 'desc' },
        take: 3,
      }),
    ]);
    const uploaderIds = [...new Set(recentCases.map((item) => item.createdBy))];
    const uploaders = await this.prisma.user.findMany({
      where: {
        departmentId: currentUser.departmentId,
        id: { in: uploaderIds },
        deletedAt: null,
      },
      select: {
        id: true,
        displayName: true,
        username: true,
      },
    });
    const uploaderById = new Map(uploaders.map((item) => [item.id, item]));

    return ok({
      todayFollowUpCount,
      overdueFollowUpCount,
      totalPatientCount,
      totalCaseCount,
      recentCases: recentCases.map((item) => ({
        id: item.id,
        title: item.title,
        diseaseType: item.diseaseType,
        currentStatus: item.currentStatus,
        caseStatus: item.caseStatus,
        createdAt: item.createdAt,
        uploader: uploaderById.get(item.createdBy)
          ? {
              id: item.createdBy,
              displayName: uploaderById.get(item.createdBy)?.displayName,
              username: uploaderById.get(item.createdBy)?.username,
            }
          : null,
        patient: {
          id: item.patient.id,
          name: item.patient.name,
          inpatientNo: item.patient.inpatientNo,
          outpatientNo: item.patient.outpatientNo,
        },
      })),
    });
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
}
