import { PartialType } from '@nestjs/mapped-types';
import { CreateFollowUpRecordDto } from './create-follow-up-record.dto';

export class UpdateFollowUpRecordDto extends PartialType(CreateFollowUpRecordDto) {}

