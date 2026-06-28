import { Body, Controller, Delete, Get, Param, Post, Query } from '@nestjs/common';
import { CurrentUser } from '../../common/current-user';
import { CurrentUserParam } from '../../common/decorators/current-user.decorator';
import { Roles } from '../../common/decorators/roles.decorator';
import { AttachmentQueryDto } from './dto/attachment-query.dto';
import { CreateAttachmentDto } from './dto/create-attachment.dto';
import { CreateUploadTokenDto } from './dto/create-upload-token.dto';
import { AttachmentsService } from './attachments.service';

@Controller('attachments')
export class AttachmentsController {
  constructor(private readonly attachmentsService: AttachmentsService) {}

  @Roles('admin', 'doctor', 'research_assistant')
  @Post('upload-token')
  createUploadToken(
    @CurrentUserParam() user: CurrentUser,
    @Body() body: CreateUploadTokenDto,
  ) {
    return this.attachmentsService.createUploadToken(user, body);
  }

  @Roles('admin', 'doctor', 'research_assistant')
  @Post()
  create(@CurrentUserParam() user: CurrentUser, @Body() body: CreateAttachmentDto) {
    return this.attachmentsService.create(user, body);
  }

  @Get()
  list(@CurrentUserParam() user: CurrentUser, @Query() query: AttachmentQueryDto) {
    return this.attachmentsService.list(user, query);
  }

  @Get(':attachmentId/preview-url')
  previewUrl(
    @CurrentUserParam() user: CurrentUser,
    @Param('attachmentId') attachmentId: string,
  ) {
    return this.attachmentsService.previewUrl(user, attachmentId);
  }

  @Delete(':attachmentId')
  delete(
    @CurrentUserParam() user: CurrentUser,
    @Param('attachmentId') attachmentId: string,
  ) {
    return this.attachmentsService.delete(user, attachmentId);
  }
}
