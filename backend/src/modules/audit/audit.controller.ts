import { Controller, Get, Param, Query } from '@nestjs/common';
import { CurrentUser } from '../../common/current-user';
import { CurrentUserParam } from '../../common/decorators/current-user.decorator';
import { Roles } from '../../common/decorators/roles.decorator';
import { AuditLogQueryDto } from './dto/audit-log-query.dto';
import { AuditService } from './audit.service';

@Roles('admin')
@Controller('audit-logs')
export class AuditController {
  constructor(private readonly auditService: AuditService) {}

  @Get()
  list(@CurrentUserParam() user: CurrentUser, @Query() query: AuditLogQueryDto) {
    return this.auditService.list(user, query);
  }

  @Get(':logId')
  detail(@CurrentUserParam() user: CurrentUser, @Param('logId') logId: string) {
    return this.auditService.detail(user, logId);
  }
}
