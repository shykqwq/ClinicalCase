import { IsOptional, IsString } from 'class-validator';

export class AttachmentQueryDto {
  @IsOptional()
  @IsString()
  patientId?: string;

  @IsOptional()
  @IsString()
  caseId?: string;

  @IsOptional()
  @IsString()
  category?: string;
}

