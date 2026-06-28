import { IsEmail, IsIn, IsOptional, IsString } from 'class-validator';
import { UserRole } from '../../../common/current-user';

const roles: UserRole[] = ['admin', 'doctor', 'research_assistant', 'readonly'];

export class UpdateUserDto {
  @IsOptional()
  @IsString()
  displayName?: string;

  @IsOptional()
  @IsString()
  phone?: string;

  @IsOptional()
  @IsEmail()
  email?: string;

  @IsOptional()
  @IsIn(roles)
  role?: UserRole;

  @IsOptional()
  @IsIn(['active', 'disabled'])
  status?: 'active' | 'disabled';
}

