import { IsDateString, IsIn, IsOptional, IsString } from 'class-validator';

export class UpdateFollowUpPlanDto {
  @IsOptional()
  @IsIn(['postoperative', 'chemotherapy', 'endocrine', 'imaging_review', 'routine', 'other'])
  followUpType?: string;

  @IsOptional()
  @IsDateString()
  plannedDate?: string;

  @IsOptional()
  @IsString()
  assigneeId?: string;

  @IsOptional()
  @IsIn(['pending', 'completed', 'overdue', 'lost'])
  status?: string;
}

