import { ConflictException, Injectable, NotFoundException, UnauthorizedException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';
import { ok } from '../../common/api-response';
import { CurrentUser } from '../../common/current-user';
import { PrismaService } from '../../prisma/prisma.service';
import { ChangePasswordDto } from './dto/change-password.dto';
import { LoginDto } from './dto/login.dto';
import { RegisterDto } from './dto/register.dto';

@Injectable()
export class AuthService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly jwtService: JwtService,
    private readonly configService: ConfigService,
  ) {}

  async register(body: RegisterDto) {
    const departmentId =
      this.configService.get<string>('SEED_DEPARTMENT_ID') ??
      '00000000-0000-0000-0000-000000000001';
    const department = await this.prisma.department.findFirst({
      where: {
        id: departmentId,
        status: 'active',
      },
    });
    if (!department) {
      throw new NotFoundException('Default department not found');
    }

    const existing = await this.prisma.user.findFirst({
      where: {
        departmentId: department.id,
        username: body.username.trim(),
        deletedAt: null,
      },
    });
    if (existing) {
      throw new ConflictException('Username already exists');
    }

    const passwordHash = await bcrypt.hash(body.password, 12);
    const user = await this.prisma.user.create({
      data: {
        departmentId: department.id,
        username: body.username.trim(),
        passwordHash,
        displayName: body.displayName.trim(),
        role: 'doctor',
        status: 'active',
      },
    });

    const payload: CurrentUser = {
      id: user.id,
      departmentId: user.departmentId,
      role: user.role as CurrentUser['role'],
      displayName: user.displayName,
    };

    return ok({
      accessToken: await this.signAccessToken(payload),
      refreshToken: await this.signRefreshToken(payload),
      expiresIn: 7200,
      user: payload,
    });
  }

  async login(body: LoginDto) {
    const user = await this.prisma.user.findFirst({
      where: {
        username: body.username,
        deletedAt: null,
      },
    });

    if (!user || user.status !== 'active') {
      throw new UnauthorizedException('Invalid username or password');
    }

    const passwordMatches = await bcrypt.compare(body.password, user.passwordHash);
    if (!passwordMatches) {
      throw new UnauthorizedException('Invalid username or password');
    }

    await this.prisma.user.update({
      where: { id: user.id },
      data: { lastLoginAt: new Date() },
    });

    const payload: CurrentUser = {
      id: user.id,
      departmentId: user.departmentId,
      role: user.role as CurrentUser['role'],
      displayName: user.displayName,
    };

    return ok({
      accessToken: await this.signAccessToken(payload),
      refreshToken: await this.signRefreshToken(payload),
      expiresIn: 7200,
      user: payload,
    });
  }

  async refresh(refreshToken: string) {
    try {
      const payload = await this.jwtService.verifyAsync<CurrentUser>(refreshToken, {
        secret: this.configService.get<string>('JWT_REFRESH_SECRET'),
      });
      const nextPayload: CurrentUser = {
        id: payload.id,
        departmentId: payload.departmentId,
        role: payload.role,
        displayName: payload.displayName,
      };

      return ok({
        accessToken: await this.signAccessToken(nextPayload),
        refreshToken: await this.signRefreshToken(nextPayload),
        expiresIn: 7200,
      });
    } catch {
      throw new UnauthorizedException('Invalid or expired refresh token');
    }
  }

  me(user: CurrentUser) {
    return ok(user);
  }

  async changePassword(currentUser: CurrentUser, body: ChangePasswordDto) {
    const user = await this.prisma.user.findFirst({
      where: {
        id: currentUser.id,
        departmentId: currentUser.departmentId,
        deletedAt: null,
      },
    });
    if (!user || user.status !== 'active') {
      throw new UnauthorizedException('User not found');
    }

    const passwordMatches = await bcrypt.compare(body.currentPassword, user.passwordHash);
    if (!passwordMatches) {
      throw new UnauthorizedException('Current password is incorrect');
    }

    const passwordHash = await bcrypt.hash(body.newPassword, 12);
    await this.prisma.user.update({
      where: { id: user.id },
      data: { passwordHash },
    });

    return ok({ changed: true });
  }

  private signAccessToken(payload: CurrentUser) {
    return this.jwtService.signAsync(payload, {
      secret: this.configService.get<string>('JWT_ACCESS_SECRET'),
      expiresIn: this.configService.get<string>('JWT_ACCESS_EXPIRES_IN', '2h'),
    });
  }

  private signRefreshToken(payload: CurrentUser) {
    return this.jwtService.signAsync(payload, {
      secret: this.configService.get<string>('JWT_REFRESH_SECRET'),
      expiresIn: this.configService.get<string>('JWT_REFRESH_EXPIRES_IN', '14d'),
    });
  }
}
