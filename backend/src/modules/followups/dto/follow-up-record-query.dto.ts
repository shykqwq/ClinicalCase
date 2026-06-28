import { IsOptional, IsString } from 'class-validator';

export class FollowUpRecordQueryDto {
  @IsOptional()
  @IsString()
  patientId?: string;

  @IsOptional()
  @IsString()
  caseId?: string;
}

