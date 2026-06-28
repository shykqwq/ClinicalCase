import { Body, Controller, Delete, Get, Param, Post, Put, Query } from '@nestjs/common';
import { CurrentUser } from '../../common/current-user';
import { CurrentUserParam } from '../../common/decorators/current-user.decorator';
import { Roles } from '../../common/decorators/roles.decorator';
import { CreatePatientDto } from './dto/create-patient.dto';
import { PatientQueryDto } from './dto/patient-query.dto';
import { UpdatePatientDto } from './dto/update-patient.dto';
import { PatientsService } from './patients.service';

@Controller('patients')
export class PatientsController {
  constructor(private readonly patientsService: PatientsService) {}

  @Get()
  list(@CurrentUserParam() user: CurrentUser, @Query() query: PatientQueryDto) {
    return this.patientsService.list(user, query);
  }

  @Get('duplicate-check')
  duplicateCheck(
    @CurrentUserParam() user: CurrentUser,
    @Query() query: Record<string, string>,
  ) {
    return this.patientsService.duplicateCheck(user, query);
  }

  @Roles('admin', 'doctor', 'research_assistant')
  @Post()
  create(@CurrentUserParam() user: CurrentUser, @Body() body: CreatePatientDto) {
    return this.patientsService.create(user, body);
  }

  @Get(':patientId')
  detail(@CurrentUserParam() user: CurrentUser, @Param('patientId') patientId: string) {
    return this.patientsService.detail(user, patientId);
  }

  @Roles('admin', 'doctor', 'research_assistant')
  @Put(':patientId')
  update(
    @CurrentUserParam() user: CurrentUser,
    @Param('patientId') patientId: string,
    @Body() body: UpdatePatientDto,
  ) {
    return this.patientsService.update(user, patientId, body);
  }

  @Roles('admin')
  @Delete(':patientId')
  delete(@CurrentUserParam() user: CurrentUser, @Param('patientId') patientId: string) {
    return this.patientsService.delete(user, patientId);
  }
}
