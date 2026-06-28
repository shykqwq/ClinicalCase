import { ConflictException, Injectable, NotFoundException } from '@nestjs/common';
import * as bcrypt from 'bcrypt';
import { ok, page } from '../../common/api-response';
import { CurrentUser } from '../../common/current-user';
import { PaginationDto } from '../../common/pagination.dto';
import { PrismaService } from '../../prisma/prisma.service';
import { AuditService } from '../audit/audit.service';
import { CreateUserDto } from './dto/create-user.dto';
import { UpdateUserDto } from './dto/update-user.dto';

const userSelect = {
  id: true,
  departmentId: true,
  username: true,
  displayName: true,
  phone: true,
  email: true,
  role: true,
  status: true,
  lastLoginAt: true,
  createdAt: true,
  updatedAt: true,
};

@Injectable()
export class UsersService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly audit: AuditService,
  ) {}

  async list(currentUser: CurrentUser, query: PaginationDto) {
    const skip = (query.page - 1) * query.pageSize;
    const where = {
      departmentId: currentUser.departmentId,
      deletedAt: null,
    };
    const [items, total] = await this.prisma.$transaction([
      this.prisma.user.findMany({
        where,
        select: userSelect,
        orderBy: { createdAt: 'desc' },
        skip,
        take: query.pageSize,
      }),
      this.prisma.user.count({ where }),
    ]);

    return page(items, query.page, query.pageSize, total);
  }

  async create(currentUser: CurrentUser, body: CreateUserDto) {
    const existing = await this.prisma.user.findUnique({
      where: {
        departmentId_username: {
          departmentId: currentUser.departmentId,
          username: body.username,
        },
      },
    });
    if (existing) {
      throw new ConflictException('Username already exists in this department');
    }

    const passwordHash = await bcrypt.hash(body.password, 12);
    const user = await this.prisma.user.create({
      data: {
        departmentId: currentUser.departmentId,
        username: body.username,
        passwordHash,
        displayName: body.displayName,
        phone: body.phone,
        email: body.email,
        role: body.role,
        status: body.status ?? 'active',
      },
      select: userSelect,
    });

    await this.audit.write({
      currentUser,
      action: 'USER_CREATE',
      objectType: 'User',
      objectId: user.id,
      afterSnapshot: user,
    });

    return ok(user);
  }

  async detail(currentUser: CurrentUser, userId: string) {
    const user = await this.prisma.user.findFirst({
      where: {
        id: userId,
        departmentId: currentUser.departmentId,
        deletedAt: null,
      },
      select: userSelect,
    });
    if (!user) {
      throw new NotFoundException('User not found');
    }

    return ok(user);
  }

  async update(currentUser: CurrentUser, userId: string, body: UpdateUserDto) {
    const before = await this.findUserForAudit(currentUser, userId);
    const user = await this.prisma.user.update({
      where: { id: userId },
      data: body,
      select: userSelect,
    });

    await this.audit.write({
      currentUser,
      action: 'USER_UPDATE',
      objectType: 'User',
      objectId: user.id,
      beforeSnapshot: before,
      afterSnapshot: user,
    });

    return ok(user);
  }

  async setStatus(
    currentUser: CurrentUser,
    userId: string,
    status: 'active' | 'disabled',
  ) {
    const before = await this.findUserForAudit(currentUser, userId);
    const user = await this.prisma.user.update({
      where: { id: userId },
      data: { status },
      select: userSelect,
    });

    await this.audit.write({
      currentUser,
      action: 'USER_STATUS_CHANGE',
      objectType: 'User',
      objectId: user.id,
      beforeSnapshot: before,
      afterSnapshot: user,
    });

    return ok(user);
  }

  private async ensureUserExists(currentUser: CurrentUser, userId: string) {
    await this.findUserForAudit(currentUser, userId);
  }

  private async findUserForAudit(currentUser: CurrentUser, userId: string) {
    const user = await this.prisma.user.findFirst({
      where: {
        id: userId,
        departmentId: currentUser.departmentId,
        deletedAt: null,
      },
      select: userSelect,
    });
    if (!user) {
      throw new NotFoundException('User not found');
    }

    return user;
  }
}
