import { Type } from 'class-transformer';
import { IsDateString, IsIn, IsInt, IsOptional, IsString, Min } from 'class-validator';

export class CreatePatientDto {
  @IsString()
  name!: string;

  @IsIn(['female', 'male', 'other', 'unknown'])
  gender!: string;

  @IsOptional()
  @IsDateString()
  birthDate?: string;

  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(0)
  ageAtFirstVisit?: number;

  @IsOptional()
  @IsString()
  phone?: string;

  @IsOptional()
  @IsString()
  identityNo?: string;

  @IsOptional()
  @IsString()
  outpatientNo?: string;

  @IsOptional()
  @IsString()
  inpatientNo?: string;

  @IsDateString()
  firstVisitDate!: string;

  @IsOptional()
  @IsString()
  attendingDoctorId?: string;

  @IsOptional()
  @IsString()
  remark?: string;
}
