import { Body, Controller, Delete, Get, Param, Post, Put, Query } from '@nestjs/common';
import { CurrentUser } from '../../common/current-user';
import { CurrentUserParam } from '../../common/decorators/current-user.decorator';
import { Roles } from '../../common/decorators/roles.decorator';
import { CreateFollowUpPlanDto } from './dto/create-follow-up-plan.dto';
import { CreateFollowUpRecordDto } from './dto/create-follow-up-record.dto';
import { FollowUpPlanQueryDto } from './dto/follow-up-plan-query.dto';
import { FollowUpRecordQueryDto } from './dto/follow-up-record-query.dto';
import { UpdateFollowUpPlanDto } from './dto/update-follow-up-plan.dto';
import { UpdateFollowUpRecordDto } from './dto/update-follow-up-record.dto';
import { FollowupsService } from './followups.service';

@Controller()
export class FollowupsController {
  constructor(private readonly followupsService: FollowupsService) {}

  @Get('follow-up-plans')
  listPlans(@CurrentUserParam() user: CurrentUser, @Query() query: FollowUpPlanQueryDto) {
    return this.followupsService.listPlans(user, query);
  }

  @Get('follow-up-plans/today')
  todayPlans(@CurrentUserParam() user: CurrentUser) {
    return this.followupsService.todayPlans(user);
  }

  @Get('follow-up-plans/overdue')
  overduePlans(@CurrentUserParam() user: CurrentUser) {
    return this.followupsService.overduePlans(user);
  }

  @Roles('admin', 'doctor', 'research_assistant')
  @Post('follow-up-plans')
  createPlan(@CurrentUserParam() user: CurrentUser, @Body() body: CreateFollowUpPlanDto) {
    return this.followupsService.createPlan(user, body);
  }

  @Roles('admin', 'doctor', 'research_assistant')
  @Put('follow-up-plans/:planId')
  updatePlan(
    @CurrentUserParam() user: CurrentUser,
    @Param('planId') planId: string,
    @Body() body: UpdateFollowUpPlanDto,
  ) {
    return this.followupsService.updatePlan(user, planId, body);
  }

  @Roles('admin', 'doctor', 'research_assistant')
  @Post('follow-up-plans/:planId/mark-lost')
  markLost(@CurrentUserParam() user: CurrentUser, @Param('planId') planId: string) {
    return this.followupsService.markLost(user, planId);
  }

  @Delete('follow-up-plans/:planId')
  deletePlan(@CurrentUserParam() user: CurrentUser, @Param('planId') planId: string) {
    return this.followupsService.deletePlan(user, planId);
  }

  @Roles('admin', 'doctor', 'research_assistant')
  @Post('follow-up-records')
  createRecord(
    @CurrentUserParam() user: CurrentUser,
    @Body() body: CreateFollowUpRecordDto,
  ) {
    return this.followupsService.createRecord(user, body);
  }

  @Get('follow-up-records')
  listRecords(
    @CurrentUserParam() user: CurrentUser,
    @Query() query: FollowUpRecordQueryDto,
  ) {
    return this.followupsService.listRecords(user, query);
  }

  @Get('follow-up-records/:recordId')
  recordDetail(@CurrentUserParam() user: CurrentUser, @Param('recordId') recordId: string) {
    return this.followupsService.recordDetail(user, recordId);
  }

  @Roles('admin', 'doctor', 'research_assistant')
  @Put('follow-up-records/:recordId')
  updateRecord(
    @CurrentUserParam() user: CurrentUser,
    @Param('recordId') recordId: string,
    @Body() body: UpdateFollowUpRecordDto,
  ) {
    return this.followupsService.updateRecord(user, recordId, body);
  }

  @Roles('admin')
  @Delete('follow-up-records/:recordId')
  deleteRecord(@CurrentUserParam() user: CurrentUser, @Param('recordId') recordId: string) {
    return this.followupsService.deleteRecord(user, recordId);
  }
}
