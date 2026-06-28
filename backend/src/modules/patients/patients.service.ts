import { Injectable, NotFoundException } from '@nestjs/common';
import { Patient, Prisma } from '@prisma/client';
import { ok, page } from '../../common/api-response';
import { CurrentUser } from '../../common/current-user';
import {
  canViewSensitivePatientData,
  maskIdentityNo,
  maskName,
  maskPhone,
} from '../../common/sensitive-data';
import { PrismaService } from '../../prisma/prisma.service';
import { AuditService } from '../audit/audit.service';
import { CreatePatientDto } from './dto/create-patient.dto';
import { PatientQueryDto } from './dto/patient-query.dto';
import { UpdatePatientDto } from './dto/update-patient.dto';

@Injectable()
export class PatientsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly audit: AuditService,
  ) {}

  async list(currentUser: CurrentUser, query: PatientQueryDto) {
    const skip = (query.page - 1) * query.pageSize;
    const where: Prisma.PatientWhereInput = {
      departmentId: currentUser.departmentId,
      deletedAt: null,
      ...(query.attendingDoctorId ? { attendingDoctorId: query.attendingDoctorId } : {}),
      ...(query.firstVisitStart || query.firstVisitEnd
        ? {
            firstVisitDate: {
              ...(query.firstVisitStart ? { gte: new Date(query.firstVisitStart) } : {}),
              ...(query.firstVisitEnd ? { lte: new Date(query.firstVisitEnd) } : {}),
            },
          }
        : {}),
      ...(query.keyword
        ? {
            OR: [
              { name: { contains: query.keyword, mode: 'insensitive' } },
              { phone: { contains: query.keyword, mode: 'insensitive' } },
              { outpatientNo: { contains: query.keyword, mode: 'insensitive' } },
              { inpatientNo: { contains: query.keyword, mode: 'insensitive' } },
            ],
          }
        : {}),
    };

    const [items, total] = await this.prisma.$transaction([
      this.prisma.patient.findMany({
        where,
        orderBy: { createdAt: 'desc' },
        skip,
        take: query.pageSize,
      }),
      this.prisma.patient.count({ where }),
    ]);

    return page(
      items.map((item) => this.serializePatient(currentUser, item)),
      query.page,
      query.pageSize,
      total,
    );
  }

  async duplicateCheck(currentUser: CurrentUser, query: Record<string, string>) {
    const conditions: Prisma.PatientWhereInput[] = [];
    if (query.outpatientNo) {
      conditions.push({ outpatientNo: query.outpatientNo });
    }
    if (query.inpatientNo) {
      conditions.push({ inpatientNo: query.inpatientNo });
    }
    if (query.phone) {
      conditions.push({ phone: query.phone });
    }

    if (!conditions.length) {
      return ok({ duplicated: false, matches: [] });
    }

    const matches = await this.prisma.patient.findMany({
      where: {
        departmentId: currentUser.departmentId,
        deletedAt: null,
        OR: conditions,
      },
      take: 10,
      orderBy: { createdAt: 'desc' },
    });

    return ok({
      duplicated: matches.length > 0,
      matches: matches.map((item) => this.serializePatient(currentUser, item)),
    });
  }

  async create(currentUser: CurrentUser, body: CreatePatientDto) {
    const patient = await this.prisma.patient.create({
      data: {
        departmentId: currentUser.departmentId,
        name: body.name,
        gender: body.gender,
        birthDate: body.birthDate ? new Date(body.birthDate) : undefined,
        ageAtFirstVisit: body.ageAtFirstVisit,
        phone: body.phone,
        identityNo: body.identityNo,
        outpatientNo: body.outpatientNo,
        inpatientNo: body.inpatientNo,
        firstVisitDate: new Date(body.firstVisitDate),
        attendingDoctorId: body.attendingDoctorId,
        remark: body.remark,
        createdBy: currentUser.id,
        updatedBy: currentUser.id,
      },
    });

    await this.audit.write({
      currentUser,
      action: 'PATIENT_CREATE',
      objectType: 'Patient',
      objectId: patient.id,
      afterSnapshot: patient,
    });

    return ok(this.serializePatient(currentUser, patient));
  }

  async detail(currentUser: CurrentUser, patientId: string) {
    const patient = await this.findPatientById(currentUser, patientId);
    return ok(this.serializePatient(currentUser, patient));
  }

  async update(currentUser: CurrentUser, patientId: string, body: UpdatePatientDto) {
    const before = await this.findPatientById(currentUser, patientId);
    const patient = await this.prisma.patient.update({
      where: { id: patientId },
      data: {
        ...body,
        birthDate: body.birthDate ? new Date(body.birthDate) : undefined,
        firstVisitDate: body.firstVisitDate ? new Date(body.firstVisitDate) : undefined,
        updatedBy: currentUser.id,
      },
    });

    await this.audit.write({
      currentUser,
      action: 'PATIENT_UPDATE',
      objectType: 'Patient',
      objectId: patient.id,
      beforeSnapshot: before,
      afterSnapshot: patient,
    });

    return ok(this.serializePatient(currentUser, patient));
  }

  async delete(currentUser: CurrentUser, patientId: string) {
    const before = await this.findPatientById(currentUser, patientId);
    const patient = await this.prisma.patient.update({
      where: { id: patientId },
      data: {
        deletedAt: new Date(),
        updatedBy: currentUser.id,
      },
    });

    await this.audit.write({
      currentUser,
      action: 'PATIENT_DELETE',
      objectType: 'Patient',
      objectId: patient.id,
      beforeSnapshot: before,
      afterSnapshot: { id: patient.id, deletedAt: patient.deletedAt },
    });

    return ok({ id: patient.id, deleted: true });
  }

  private async findPatientById(currentUser: CurrentUser, patientId: string) {
    const patient = await this.prisma.patient.findFirst({
      where: {
        id: patientId,
        departmentId: currentUser.departmentId,
        deletedAt: null,
      },
    });
    if (!patient) {
      throw new NotFoundException('Patient not found');
    }

    return patient;
  }

  private async ensurePatientExists(currentUser: CurrentUser, patientId: string) {
    await this.findPatientById(currentUser, patientId);
  }

  private serializePatient(currentUser: CurrentUser, patient: Patient) {
    const canViewSensitive = canViewSensitivePatientData(currentUser);
    return {
      ...patient,
      name: canViewSensitive ? patient.name : maskName(patient.name),
      phone: canViewSensitive ? patient.phone : maskPhone(patient.phone),
      identityNo: canViewSensitive
        ? patient.identityNo
        : maskIdentityNo(patient.identityNo),
    };
  }
}
