import { Injectable, NotFoundException } from '@nestjs/common';
import { Prisma } from '@prisma/client';
import { ok, page } from '../../common/api-response';
import { CurrentUser } from '../../common/current-user';
import { PrismaService } from '../../prisma/prisma.service';
import { AuditLogQueryDto } from './dto/audit-log-query.dto';

export type AuditAction =
  | 'USER_CREATE'
  | 'USER_UPDATE'
  | 'USER_STATUS_CHANGE'
  | 'PATIENT_CREATE'
  | 'PATIENT_UPDATE'
  | 'PATIENT_DELETE'
  | 'CASE_CREATE'
  | 'CASE_UPDATE'
  | 'CASE_ARCHIVE'
  | 'CASE_TAGS_SET'
  | 'CASE_DELETE'
  | 'TAG_CREATE'
  | 'TAG_UPDATE'
  | 'TAG_DELETE'
  | 'ATTACHMENT_CREATE'
  | 'ATTACHMENT_DELETE'
  | 'FOLLOW_UP_PLAN_CREATE'
  | 'FOLLOW_UP_PLAN_UPDATE'
  | 'FOLLOW_UP_PLAN_MARK_LOST'
  | 'FOLLOW_UP_PLAN_DELETE'
  | 'FOLLOW_UP_RECORD_CREATE'
  | 'FOLLOW_UP_RECORD_UPDATE'
  | 'FOLLOW_UP_RECORD_DELETE';

export interface WriteAuditLogInput {
  currentUser: CurrentUser;
  action: AuditAction;
  objectType: string;
  objectId: string;
  beforeSnapshot?: unknown;
  afterSnapshot?: unknown;
}

@Injectable()
export class AuditService {
  constructor(private readonly prisma: PrismaService) {}

  async list(currentUser: CurrentUser, query: AuditLogQueryDto) {
    const skip = (query.page - 1) * query.pageSize;
    const where: Prisma.AuditLogWhereInput = {
      departmentId: currentUser.departmentId,
      ...(query.action ? { action: query.action } : {}),
      ...(query.actorId ? { actorId: query.actorId } : {}),
      ...(query.objectType ? { objectType: query.objectType } : {}),
      ...(query.objectId ? { objectId: query.objectId } : {}),
      ...(query.start || query.end
        ? {
            createdAt: {
              ...(query.start ? { gte: new Date(query.start) } : {}),
              ...(query.end ? { lte: new Date(query.end) } : {}),
            },
          }
        : {}),
    };

    const [items, total] = await this.prisma.$transaction([
      this.prisma.auditLog.findMany({
        where,
        orderBy: { createdAt: 'desc' },
        skip,
        take: query.pageSize,
      }),
      this.prisma.auditLog.count({ where }),
    ]);

    return page(items, query.page, query.pageSize, total);
  }

  async detail(currentUser: CurrentUser, logId: string) {
    const log = await this.prisma.auditLog.findFirst({
      where: {
        id: logId,
        departmentId: currentUser.departmentId,
      },
    });
    if (!log) {
      throw new NotFoundException('Audit log not found');
    }

    return ok(log);
  }

  async write(input: WriteAuditLogInput) {
    await this.prisma.auditLog.create({
      data: {
        departmentId: input.currentUser.departmentId,
        actorId: input.currentUser.id,
        action: input.action,
        objectType: input.objectType,
        objectId: input.objectId,
        beforeSnapshot: this.toJson(input.beforeSnapshot),
        afterSnapshot: this.toJson(input.afterSnapshot),
      },
    });
  }

  private toJson(value: unknown): Prisma.InputJsonValue | undefined {
    if (value === undefined) {
      return undefined;
    }
    return JSON.parse(
      JSON.stringify(value, (_key, item) =>
        typeof item === 'bigint' ? Number(item) : item,
      ),
    ) as Prisma.InputJsonValue;
  }
}
