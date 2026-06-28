import { Body, Controller, Delete, Get, Param, Post, Put, Query } from '@nestjs/common';
import { CurrentUser } from '../../common/current-user';
import { CurrentUserParam } from '../../common/decorators/current-user.decorator';
import { Roles } from '../../common/decorators/roles.decorator';
import { CaseQueryDto } from './dto/case-query.dto';
import { CreateCaseDto } from './dto/create-case.dto';
import { SetCaseTagsDto } from './dto/set-case-tags.dto';
import { UpdateCaseDto } from './dto/update-case.dto';
import { CasesService } from './cases.service';

@Controller('cases')
export class CasesController {
  constructor(private readonly casesService: CasesService) {}

  @Get()
  list(@CurrentUserParam() user: CurrentUser, @Query() query: CaseQueryDto) {
    return this.casesService.list(user, query);
  }

  @Roles('admin', 'doctor', 'research_assistant')
  @Post()
  create(@CurrentUserParam() user: CurrentUser, @Body() body: CreateCaseDto) {
    return this.casesService.create(user, body);
  }

  @Get(':caseId')
  detail(@CurrentUserParam() user: CurrentUser, @Param('caseId') caseId: string) {
    return this.casesService.detail(user, caseId);
  }

  @Roles('admin', 'doctor', 'research_assistant')
  @Put(':caseId')
  update(
    @CurrentUserParam() user: CurrentUser,
    @Param('caseId') caseId: string,
    @Body() body: UpdateCaseDto,
  ) {
    return this.casesService.update(user, caseId, body);
  }

  @Roles('admin', 'doctor', 'research_assistant')
  @Post(':caseId/archive')
  archive(@CurrentUserParam() user: CurrentUser, @Param('caseId') caseId: string) {
    return this.casesService.archive(user, caseId);
  }

  @Roles('admin', 'doctor', 'research_assistant')
  @Put(':caseId/tags')
  setTags(
    @CurrentUserParam() user: CurrentUser,
    @Param('caseId') caseId: string,
    @Body() body: SetCaseTagsDto,
  ) {
    return this.casesService.setTags(user, caseId, body.tagIds);
  }

  @Roles('admin')
  @Delete(':caseId')
  delete(@CurrentUserParam() user: CurrentUser, @Param('caseId') caseId: string) {
    return this.casesService.delete(user, caseId);
  }
}
