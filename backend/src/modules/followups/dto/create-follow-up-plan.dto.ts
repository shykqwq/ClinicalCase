import { IsDateString, IsIn, IsOptional, IsString } from 'class-validator';

export class CreateFollowUpPlanDto {
  @IsString()
  patientId!: string;

  @IsString()
  caseId!: string;

  @IsIn(['postoperative', 'chemotherapy', 'endocrine', 'imaging_review', 'routine', 'other'])
  followUpType!: string;

  @IsDateString()
  plannedDate!: string;

  @IsOptional()
  @IsString()
  assigneeId?: string;
}

