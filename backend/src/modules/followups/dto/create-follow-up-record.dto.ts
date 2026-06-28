import { IsBoolean, IsDateString, IsIn, IsOptional, IsString } from 'class-validator';

export class CreateFollowUpRecordDto {
  @IsString()
  followUpPlanId!: string;

  @IsDateString()
  actualDate!: string;

  @IsIn(['outpatient', 'phone', 'wechat', 'inpatient_review', 'other'])
  method!: string;

  @IsIn(['disease_free', 'alive_with_disease', 'recurrence', 'metastasis', 'deceased', 'lost', 'unknown'])
  survivalStatus!: string;

  @IsOptional()
  @IsBoolean()
  hasRecurrence?: boolean;

  @IsOptional()
  @IsString()
  recurrenceSite?: string;

  @IsOptional()
  @IsBoolean()
  hasMetastasis?: boolean;

  @IsOptional()
  @IsString()
  metastasisSite?: string;

  @IsOptional()
  @IsString()
  imagingSummary?: string;

  @IsOptional()
  @IsString()
  tumorMarkerSummary?: string;

  @IsOptional()
  @IsString()
  currentTreatment?: string;

  @IsOptional()
  @IsString()
  adverseReactions?: string;

  @IsOptional()
  @IsString()
  remark?: string;

  @IsOptional()
  @IsString()
  summary?: string;

  @IsOptional()
  @IsDateString()
  nextFollowUpDate?: string;
}
