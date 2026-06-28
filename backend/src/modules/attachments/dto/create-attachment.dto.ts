import { IsIn, IsInt, IsOptional, IsString, Max, Min } from 'class-validator';

export class CreateAttachmentDto {
  @IsString()
  patientId!: string;

  @IsOptional()
  @IsString()
  caseId?: string;

  @IsIn([
    'pathology',
    'imaging',
    'laboratory',
    'surgery',
    'discharge_summary',
    'outpatient_record',
    'follow_up',
    'other',
  ])
  category!: string;

  @IsString()
  originalFilename!: string;

  @IsIn(['image/jpeg', 'image/png', 'application/pdf'])
  contentType!: string;

  @IsInt()
  @Min(1)
  @Max(20 * 1024 * 1024)
  fileSize!: number;

  @IsString()
  objectKey!: string;

  @IsOptional()
  @IsString()
  checksum?: string;
}

