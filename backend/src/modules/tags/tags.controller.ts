import { Body, Controller, Delete, Get, Param, Post, Put } from '@nestjs/common';
import { CurrentUser } from '../../common/current-user';
import { CurrentUserParam } from '../../common/decorators/current-user.decorator';
import { Roles } from '../../common/decorators/roles.decorator';
import { CreateTagDto } from './dto/create-tag.dto';
import { UpdateTagDto } from './dto/update-tag.dto';
import { TagsService } from './tags.service';

@Controller('tags')
export class TagsController {
  constructor(private readonly tagsService: TagsService) {}

  @Get()
  list(@CurrentUserParam() user: CurrentUser) {
    return this.tagsService.list(user);
  }

  @Roles('admin', 'doctor', 'research_assistant')
  @Post()
  create(@CurrentUserParam() user: CurrentUser, @Body() body: CreateTagDto) {
    return this.tagsService.create(user, body);
  }

  @Roles('admin', 'doctor', 'research_assistant')
  @Put(':tagId')
  update(
    @CurrentUserParam() user: CurrentUser,
    @Param('tagId') tagId: string,
    @Body() body: UpdateTagDto,
  ) {
    return this.tagsService.update(user, tagId, body);
  }

  @Roles('admin', 'doctor', 'research_assistant')
  @Delete(':tagId')
  delete(@CurrentUserParam() user: CurrentUser, @Param('tagId') tagId: string) {
    return this.tagsService.delete(user, tagId);
  }
}
