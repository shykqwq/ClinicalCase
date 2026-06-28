import { Body, Controller, Get, Param, Post, Put, Query } from '@nestjs/common';
import { CurrentUser } from '../../common/current-user';
import { CurrentUserParam } from '../../common/decorators/current-user.decorator';
import { Roles } from '../../common/decorators/roles.decorator';
import { PaginationDto } from '../../common/pagination.dto';
import { CreateUserDto } from './dto/create-user.dto';
import { UpdateUserDto } from './dto/update-user.dto';
import { UsersService } from './users.service';

@Roles('admin')
@Controller('users')
export class UsersController {
  constructor(private readonly usersService: UsersService) {}

  @Get()
  list(@CurrentUserParam() user: CurrentUser, @Query() query: PaginationDto) {
    return this.usersService.list(user, query);
  }

  @Post()
  create(@CurrentUserParam() user: CurrentUser, @Body() body: CreateUserDto) {
    return this.usersService.create(user, body);
  }

  @Get(':userId')
  detail(@CurrentUserParam() user: CurrentUser, @Param('userId') userId: string) {
    return this.usersService.detail(user, userId);
  }

  @Put(':userId')
  update(
    @CurrentUserParam() user: CurrentUser,
    @Param('userId') userId: string,
    @Body() body: UpdateUserDto,
  ) {
    return this.usersService.update(user, userId, body);
  }

  @Post(':userId/disable')
  disable(@CurrentUserParam() user: CurrentUser, @Param('userId') userId: string) {
    return this.usersService.setStatus(user, userId, 'disabled');
  }

  @Post(':userId/enable')
  enable(@CurrentUserParam() user: CurrentUser, @Param('userId') userId: string) {
    return this.usersService.setStatus(user, userId, 'active');
  }
}
