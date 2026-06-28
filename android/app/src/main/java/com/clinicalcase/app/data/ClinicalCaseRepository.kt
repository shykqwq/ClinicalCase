package com.clinicalcase.app.data

import com.clinicalcase.app.data.api.CaseDto
import com.clinicalcase.app.data.api.ApiResponse
import com.clinicalcase.app.data.api.ChangePasswordRequest
import com.clinicalcase.app.data.api.ClinicalCaseApi
import com.clinicalcase.app.data.api.CreateCaseRequest
import com.clinicalcase.app.data.api.CreateFollowUpPlanRequest
import com.clinicalcase.app.data.api.CreateFollowUpRecordData
import com.clinicalcase.app.data.api.CreateFollowUpRecordRequest
import com.clinicalcase.app.data.api.CreatePatientRequest
import com.clinicalcase.app.data.api.DashboardSummary
import com.clinicalcase.app.data.api.DeleteResult
import com.clinicalcase.app.data.api.LoginRequest
import com.clinicalcase.app.data.api.PageData
import com.clinicalcase.app.data.api.PatientDto
import com.clinicalcase.app.data.api.RegisterRequest
import com.clinicalcase.app.data.api.FollowUpPlanDto
import com.clinicalcase.app.data.api.FollowUpRecordDto
import com.clinicalcase.app.data.api.UpdateCaseRequest
import com.clinicalcase.app.data.api.UpdateFollowUpRecordRequest
import com.clinicalcase.app.data.api.UpdatePatientRequest
import com.clinicalcase.app.data.session.Session
import com.clinicalcase.app.data.session.SessionStore
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class ClinicalCaseRepository(
    private val api: ClinicalCaseApi,
    private val sessionStore: SessionStore,
) {
    val sessionFlow: Flow<Session?> = sessionStore.sessionFlow

    suspend fun login(username: String, password: String): Session {
        val response = api.login(LoginRequest(username = username, password = password))
        return saveSessionFromLoginResponse(response, "登录失败")
    }

    suspend fun register(username: String, password: String, displayName: String): Session {
        val response = api.register(
            RegisterRequest(
                username = username,
                password = password,
                displayName = displayName,
            ),
        )
        return saveSessionFromLoginResponse(response, "注册失败")
    }

    private suspend fun saveSessionFromLoginResponse(
        response: ApiResponse<com.clinicalcase.app.data.api.LoginData>,
        fallbackMessage: String,
    ): Session {
        val data = response.data
        if (!response.success || data == null) {
            error(response.error?.message ?: response.message ?: fallbackMessage)
        }

        val session = Session(
            accessToken = data.accessToken,
            refreshToken = data.refreshToken,
            displayName = data.user.displayName,
            role = data.user.role,
        )
        sessionStore.save(session)
        return session
    }

    suspend fun dashboardSummary(accessToken: String): DashboardSummary {
        return authorizedRequest("加载首页失败") {
            api.dashboardSummary("Bearer $accessToken")
        }
    }

    suspend fun changePassword(
        accessToken: String,
        currentPassword: String,
        newPassword: String,
    ): Boolean {
        return authorizedRequest("修改密码失败") {
            api.changePassword(
                authorization = "Bearer $accessToken",
                request = ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                ),
            )
        }.changed
    }

    suspend fun patients(
        accessToken: String,
        keyword: String? = null,
        page: Int = 1,
        pageSize: Int = 20,
    ): PageData<PatientDto> {
        return authorizedRequest("加载患者列表失败") {
            api.patients(
                authorization = "Bearer $accessToken",
                page = page,
                pageSize = pageSize,
                keyword = keyword?.takeIf { it.isNotBlank() },
            )
        }
    }

    suspend fun createPatient(accessToken: String, request: CreatePatientRequest): PatientDto {
        return authorizedRequest("创建患者失败") {
            api.createPatient("Bearer $accessToken", request)
        }
    }

    suspend fun patientDetail(accessToken: String, patientId: String): PatientDto {
        return authorizedRequest("加载患者详情失败") {
            api.patientDetail("Bearer $accessToken", patientId)
        }
    }

    suspend fun updatePatient(
        accessToken: String,
        patientId: String,
        request: UpdatePatientRequest,
    ): PatientDto {
        return authorizedRequest("更新患者失败") {
            api.updatePatient("Bearer $accessToken", patientId, request)
        }
    }

    suspend fun deletePatient(accessToken: String, patientId: String): DeleteResult {
        return authorizedRequest("删除患者失败") {
            api.deletePatient("Bearer $accessToken", patientId)
        }
    }

    suspend fun cases(accessToken: String, patientId: String): List<CaseDto> {
        return authorizedRequest("加载病例列表失败") {
            api.cases(
                authorization = "Bearer $accessToken",
                patientId = patientId,
            )
        }.items
    }

    suspend fun createCase(accessToken: String, request: CreateCaseRequest): CaseDto {
        return authorizedRequest("创建病例失败") {
            api.createCase("Bearer $accessToken", request)
        }
    }

    suspend fun caseDetail(accessToken: String, caseId: String): CaseDto {
        return authorizedRequest("加载病例详情失败") {
            api.caseDetail("Bearer $accessToken", caseId)
        }
    }

    suspend fun updateCase(accessToken: String, caseId: String, request: UpdateCaseRequest): CaseDto {
        return authorizedRequest("更新病例失败") {
            api.updateCase("Bearer $accessToken", caseId, request)
        }
    }

    suspend fun archiveCase(accessToken: String, caseId: String): CaseDto {
        return authorizedRequest("归档病例失败") {
            api.archiveCase("Bearer $accessToken", caseId)
        }
    }

    suspend fun deleteCase(accessToken: String, caseId: String): DeleteResult {
        return authorizedRequest("删除病例失败") {
            api.deleteCase("Bearer $accessToken", caseId)
        }
    }

    suspend fun followUpPlans(
        accessToken: String,
        status: String? = null,
        patientId: String? = null,
        caseId: String? = null,
    ): List<FollowUpPlanDto> {
        return authorizedRequest("加载随访计划失败") {
            api.followUpPlans(
                authorization = "Bearer $accessToken",
                status = status?.takeIf { it.isNotBlank() },
                patientId = patientId?.takeIf { it.isNotBlank() },
                caseId = caseId?.takeIf { it.isNotBlank() },
            )
        }.items
    }

    suspend fun todayFollowUpPlans(accessToken: String): List<FollowUpPlanDto> {
        return authorizedRequest("加载今日随访失败") {
            api.todayFollowUpPlans("Bearer $accessToken")
        }
    }

    suspend fun overdueFollowUpPlans(accessToken: String): List<FollowUpPlanDto> {
        return authorizedRequest("加载逾期随访失败") {
            api.overdueFollowUpPlans("Bearer $accessToken")
        }
    }

    suspend fun createFollowUpPlan(
        accessToken: String,
        request: CreateFollowUpPlanRequest,
    ): FollowUpPlanDto {
        return authorizedRequest("创建随访计划失败") {
            api.createFollowUpPlan("Bearer $accessToken", request)
        }
    }

    suspend fun deleteFollowUpPlan(accessToken: String, planId: String): DeleteResult {
        return authorizedRequest("删除随访计划失败") {
            api.deleteFollowUpPlan("Bearer $accessToken", planId)
        }
    }

    suspend fun followUpRecords(
        accessToken: String,
        patientId: String? = null,
        caseId: String? = null,
    ): List<FollowUpRecordDto> {
        return authorizedRequest("加载随访记录失败") {
            api.followUpRecords(
                authorization = "Bearer $accessToken",
                patientId = patientId?.takeIf { it.isNotBlank() },
                caseId = caseId?.takeIf { it.isNotBlank() },
            )
        }.items
    }

    suspend fun createFollowUpRecord(
        accessToken: String,
        request: CreateFollowUpRecordRequest,
    ): CreateFollowUpRecordData {
        return authorizedRequest("创建随访记录失败") {
            api.createFollowUpRecord("Bearer $accessToken", request)
        }
    }

    suspend fun updateFollowUpRecord(
        accessToken: String,
        recordId: String,
        request: UpdateFollowUpRecordRequest,
    ): FollowUpRecordDto {
        return authorizedRequest("更新随访记录失败") {
            api.updateFollowUpRecord("Bearer $accessToken", recordId, request)
        }
    }

    suspend fun deleteFollowUpRecord(accessToken: String, recordId: String): DeleteResult {
        return authorizedRequest("删除随访记录失败") {
            api.deleteFollowUpRecord("Bearer $accessToken", recordId)
        }
    }

    suspend fun logout() {
        sessionStore.clear()
    }

    private suspend fun <T> authorizedRequest(
        fallbackMessage: String,
        request: suspend () -> ApiResponse<T>,
    ): T {
        try {
            val response = request()
            val data = response.data
            if (!response.success || data == null) {
                error(response.error?.message ?: response.message ?: fallbackMessage)
            }
            return data
        } catch (exception: HttpException) {
            if (exception.code() == 401) {
                sessionStore.clear()
                error("登录已过期，请重新登录")
            }
            error("HTTP ${exception.code()} ${exception.message()}")
        }
    }
}
