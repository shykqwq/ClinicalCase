import {
  IsBoolean,
  IsDateString,
  IsIn,
  IsNumber,
  IsOptional,
  IsString,
  Max,
  Min,
} from 'class-validator';
import { Type } from 'class-transformer';

export class CreateCaseDto {
  @IsString()
  patientId!: string;

  @IsString()
  title!: string;

  @IsIn(['outpatient', 'inpatient', 'postoperative_review'])
  visitType!: string;

  @IsIn(['left', 'right', 'bilateral', 'unknown'])
  laterality!: string;

  @IsIn(['breast_cancer', 'benign_tumor', 'mastitis', 'other'])
  diseaseType!: string;

  @IsOptional()
  @IsString()
  preliminaryDiagnosis?: string;

  @IsOptional()
  @IsString()
  confirmedDiagnosis?: string;

  @IsOptional()
  @IsString()
  pathologyType?: string;

  @IsOptional()
  @IsIn(['positive', 'negative', 'unknown'])
  erStatus?: string;

  @IsOptional()
  @IsIn(['positive', 'negative', 'unknown'])
  prStatus?: string;

  @IsOptional()
  @IsIn(['zero', 'one_plus', 'two_plus', 'three_plus', 'fish_positive', 'fish_negative', 'unknown'])
  her2Status?: string;

  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  @Max(100)
  ki67Percent?: number;

  @IsOptional()
  @IsIn([
    'luminal_a',
    'luminal_b',
    'luminal_b_her2_negative',
    'luminal_b_her2_positive',
    'her2_positive',
    'triple_negative',
    'unknown',
  ])
  molecularSubtype?: string;

  @IsOptional()
  @IsString()
  clinicalTStage?: string;

  @IsOptional()
  @IsString()
  clinicalNStage?: string;

  @IsOptional()
  @IsString()
  clinicalMStage?: string;

  @IsOptional()
  @IsString()
  pathologicalTStage?: string;

  @IsOptional()
  @IsString()
  pathologicalNStage?: string;

  @IsOptional()
  @IsString()
  pathologicalMStage?: string;

  @IsOptional()
  @IsString()
  clinicalStage?: string;

  @IsOptional()
  @IsString()
  pathologicalStage?: string;

  @IsOptional()
  @IsBoolean()
  hasNeoadjuvantTherapy?: boolean;

  @IsOptional()
  @IsString()
  neoadjuvantTherapyPlan?: string;

  @IsOptional()
  @IsDateString()
  surgeryDate?: string;

  @IsOptional()
  @IsString()
  surgeryType?: string;

  @IsOptional()
  @IsString()
  axillaryManagement?: string;

  @IsOptional()
  @IsString()
  chemotherapyPlan?: string;

  @IsOptional()
  @IsBoolean()
  hasRadiotherapy?: boolean;

  @IsOptional()
  @IsBoolean()
  hasTargetedTherapy?: boolean;

  @IsOptional()
  @IsString()
  targetedTherapyPlan?: string;

  @IsOptional()
  @IsBoolean()
  hasEndocrineTherapy?: boolean;

  @IsOptional()
  @IsString()
  endocrineTherapyPlan?: string;

  @IsOptional()
  @IsIn(['none', 'local_recurrence', 'distant_metastasis', 'unknown'])
  recurrenceStatus?: string;

  @IsIn(['treating', 'follow_up', 'recurrence', 'deceased', 'lost'])
  currentStatus!: string;

  @IsOptional()
  @IsString()
  summary?: string;

  @IsOptional()
  @IsIn(['draft', 'archived'])
  caseStatus?: string;
}
