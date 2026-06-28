import { Module } from '@nestjs/common';
import { AuditModule } from '../audit/audit.module';
import { FollowupsController } from './followups.controller';
import { FollowupsService } from './followups.service';

@Module({
  imports: [AuditModule],
  controllers: [FollowupsController],
  providers: [FollowupsService],
})
export class FollowupsModule {}
