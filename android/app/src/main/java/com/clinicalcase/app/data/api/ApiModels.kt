package com.clinicalcase.app.data.api

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String? = null,
    val error: ApiError? = null,
)

data class ApiError(
    val code: String? = null,
    val message: String? = null,
)

data class LoginRequest(
    val username: String,
    val password: String,
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val displayName: String,
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
)

data class ChangePasswordResult(
    val changed: Boolean,
)

data class LoginData(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: UserDto,
)

data class UserDto(
    val id: String,
    val departmentId: String,
    val role: String,
    val displayName: String,
)

data class DashboardSummary(
    val todayFollowUpCount: Int,
    val overdueFollowUpCount: Int,
    val totalPatientCount: Int = 0,
    val totalCaseCount: Int,
    val recentCases: List<RecentCaseDto> = emptyList(),
)

data class RecentCaseDto(
    val id: String,
    val title: String,
    val diseaseType: String?,
    val currentStatus: String?,
    val caseStatus: String?,
    val createdAt: String?,
    val uploader: RecentUserDto? = null,
    val patient: RecentPatientDto?,
)

data class RecentUserDto(
    val id: String,
    val displayName: String? = null,
    val username: String? = null,
)

data class RecentPatientDto(
    val id: String,
    val name: String?,
    val inpatientNo: String?,
    val outpatientNo: String?,
)

data class PageData<T>(
    val items: List<T> = emptyList(),
    val page: Int,
    val pageSize: Int,
    val total: Int,
)

data class DeleteResult(
    val id: String,
    val deleted: Boolean,
)

data class PatientDto(
    val id: String,
    val name: String,
    val gender: String,
    val birthDate: String? = null,
    val ageAtFirstVisit: Int? = null,
    val phone: String? = null,
    val identityNo: String? = null,
    val outpatientNo: String? = null,
    val inpatientNo: String? = null,
    val firstVisitDate: String,
    val remark: String? = null,
)

data class CreatePatientRequest(
    val name: String,
    val gender: String,
    val birthDate: String? = null,
    val phone: String? = null,
    val outpatientNo: String? = null,
    val inpatientNo: String? = null,
    val firstVisitDate: String,
    val remark: String? = null,
)

data class UpdatePatientRequest(
    val name: String,
    val gender: String,
    val birthDate: String? = null,
    val phone: String? = null,
    val outpatientNo: String? = null,
    val inpatientNo: String? = null,
    val firstVisitDate: String,
    val remark: String? = null,
)

data class CaseDto(
    val id: String,
    val patientId: String,
    val title: String,
    val visitType: String,
    val laterality: String,
    val diseaseType: String,
    val preliminaryDiagnosis: String? = null,
    val confirmedDiagnosis: String? = null,
    val pathologyType: String? = null,
    val erStatus: String? = null,
    val prStatus: String? = null,
    val her2Status: String? = null,
    val ki67Percent: Double? = null,
    val molecularSubtype: String? = null,
    val clinicalTStage: String? = null,
    val clinicalNStage: String? = null,
    val clinicalMStage: String? = null,
    val clinicalStage: String? = null,
    val surgeryDate: String? = null,
    val surgeryType: String? = null,
    val currentStatus: String,
    val summary: String? = null,
    val caseStatus: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

data class CreateCaseRequest(
    val patientId: String,
    val title: String,
    val visitType: String,
    val laterality: String,
    val diseaseType: String,
    val confirmedDiagnosis: String? = null,
    val pathologyType: String? = null,
    val erStatus: String? = null,
    val prStatus: String? = null,
    val her2Status: String? = null,
    val ki67Percent: Double? = null,
    val molecularSubtype: String? = null,
    val surgeryDate: String? = null,
    val surgeryType: String? = null,
    val currentStatus: String,
    val summary: String? = null,
    val caseStatus: String = "draft",
)

data class UpdateCaseRequest(
    val title: String,
    val visitType: String,
    val laterality: String,
    val diseaseType: String,
    val confirmedDiagnosis: String? = null,
    val pathologyType: String? = null,
    val erStatus: String? = null,
    val prStatus: String? = null,
    val her2Status: String? = null,
    val ki67Percent: Double? = null,
    val molecularSubtype: String? = null,
    val surgeryDate: String? = null,
    val surgeryType: String? = null,
    val currentStatus: String,
    val summary: String? = null,
    val caseStatus: String = "draft",
)

data class FollowUpPlanDto(
    val id: String,
    val patientId: String,
    val caseId: String,
    val followUpType: String,
    val plannedDate: String,
    val assigneeId: String? = null,
    val status: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val clinicalCase: FollowUpCaseDto? = null,
)

data class FollowUpCaseDto(
    val id: String,
    val title: String,
    val diseaseType: String? = null,
    val currentStatus: String? = null,
    val caseStatus: String? = null,
    val patient: RecentPatientDto? = null,
)

data class CreateFollowUpPlanRequest(
    val patientId: String,
    val caseId: String,
    val followUpType: String,
    val plannedDate: String,
    val assigneeId: String? = null,
)

data class FollowUpRecordDto(
    val id: String,
    val followUpPlanId: String,
    val patientId: String,
    val caseId: String,
    val actualDate: String,
    val method: String,
    val survivalStatus: String,
    val hasRecurrence: Boolean? = null,
    val recurrenceSite: String? = null,
    val hasMetastasis: Boolean? = null,
    val metastasisSite: String? = null,
    val imagingSummary: String? = null,
    val tumorMarkerSummary: String? = null,
    val currentTreatment: String? = null,
    val adverseReactions: String? = null,
    val remark: String? = null,
    val summary: String? = null,
    val nextFollowUpDate: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

data class FollowUpRecordListData(
    val items: List<FollowUpRecordDto> = emptyList(),
)

data class CreateFollowUpRecordRequest(
    val followUpPlanId: String,
    val actualDate: String,
    val method: String,
    val survivalStatus: String,
    val hasRecurrence: Boolean? = null,
    val recurrenceSite: String? = null,
    val hasMetastasis: Boolean? = null,
    val metastasisSite: String? = null,
    val imagingSummary: String? = null,
    val tumorMarkerSummary: String? = null,
    val currentTreatment: String? = null,
    val adverseReactions: String? = null,
    val remark: String? = null,
    val nextFollowUpDate: String? = null,
)

data class UpdateFollowUpRecordRequest(
    val actualDate: String? = null,
    val method: String? = null,
    val survivalStatus: String? = null,
    val hasRecurrence: Boolean? = null,
    val recurrenceSite: String? = null,
    val hasMetastasis: Boolean? = null,
    val metastasisSite: String? = null,
    val imagingSummary: String? = null,
    val tumorMarkerSummary: String? = null,
    val currentTreatment: String? = null,
    val adverseReactions: String? = null,
    val remark: String? = null,
    val nextFollowUpDate: String? = null,
)

data class CreateFollowUpRecordData(
    val record: FollowUpRecordDto,
    val nextPlan: FollowUpPlanDto? = null,
)
