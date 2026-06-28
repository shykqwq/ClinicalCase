import { Type } from 'class-transformer';
import { IsBoolean, IsDateString, IsInt, IsOptional, IsString, Max, Min } from 'class-validator';

export class CaseQueryDto {
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  page = 1;

  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(100)
  pageSize = 20;

  @IsOptional()
  @IsString()
  keyword?: string;

  @IsOptional()
  @IsString()
  patientId?: string;

  @IsOptional()
  @IsString()
  diseaseType?: string;

  @IsOptional()
  @IsString()
  laterality?: string;

  @IsOptional()
  @IsString()
  pathologyType?: string;

  @IsOptional()
  @IsString()
  erStatus?: string;

  @IsOptional()
  @IsString()
  prStatus?: string;

  @IsOptional()
  @IsString()
  her2Status?: string;

  @IsOptional()
  @IsString()
  molecularSubtype?: string;

  @IsOptional()
  @IsString()
  clinicalStage?: string;

  @IsOptional()
  @IsString()
  pathologicalStage?: string;

  @IsOptional()
  @IsString()
  surgeryType?: string;

  @IsOptional()
  @Type(() => Boolean)
  @IsBoolean()
  hasNeoadjuvantTherapy?: boolean;

  @IsOptional()
  @Type(() => Boolean)
  @IsBoolean()
  hasRadiotherapy?: boolean;

  @IsOptional()
  @Type(() => Boolean)
  @IsBoolean()
  hasTargetedTherapy?: boolean;

  @IsOptional()
  @Type(() => Boolean)
  @IsBoolean()
  hasEndocrineTherapy?: boolean;

  @IsOptional()
  @IsString()
  recurrenceStatus?: string;

  @IsOptional()
  @IsString()
  currentStatus?: string;

  @IsOptional()
  @IsString()
  caseStatus?: string;

  @IsOptional()
  @IsString()
  tagIds?: string;

  @IsOptional()
  @IsDateString()
  surgeryStart?: string;

  @IsOptional()
  @IsDateString()
  surgeryEnd?: string;
}

