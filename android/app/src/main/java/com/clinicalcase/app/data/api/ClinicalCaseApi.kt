package com.clinicalcase.app.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ClinicalCaseApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginData>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<LoginData>

    @POST("auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") authorization: String,
        @Body request: ChangePasswordRequest,
    ): ApiResponse<ChangePasswordResult>

    @GET("dashboard/summary")
    suspend fun dashboardSummary(
        @Header("Authorization") authorization: String,
    ): ApiResponse<DashboardSummary>

    @GET("patients")
    suspend fun patients(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("keyword") keyword: String? = null,
    ): ApiResponse<PageData<PatientDto>>

    @POST("patients")
    suspend fun createPatient(
        @Header("Authorization") authorization: String,
        @Body request: CreatePatientRequest,
    ): ApiResponse<PatientDto>

    @GET("patients/{patientId}")
    suspend fun patientDetail(
        @Header("Authorization") authorization: String,
        @Path("patientId") patientId: String,
    ): ApiResponse<PatientDto>

    @PUT("patients/{patientId}")
    suspend fun updatePatient(
        @Header("Authorization") authorization: String,
        @Path("patientId") patientId: String,
        @Body request: UpdatePatientRequest,
    ): ApiResponse<PatientDto>

    @DELETE("patients/{patientId}")
    suspend fun deletePatient(
        @Header("Authorization") authorization: String,
        @Path("patientId") patientId: String,
    ): ApiResponse<DeleteResult>

    @GET("cases")
    suspend fun cases(
        @Header("Authorization") authorization: String,
        @Query("patientId") patientId: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
        @Query("caseStatus") caseStatus: String? = null,
    ): ApiResponse<PageData<CaseDto>>

    @POST("cases")
    suspend fun createCase(
        @Header("Authorization") authorization: String,
        @Body request: CreateCaseRequest,
    ): ApiResponse<CaseDto>

    @GET("cases/{caseId}")
    suspend fun caseDetail(
        @Header("Authorization") authorization: String,
        @Path("caseId") caseId: String,
    ): ApiResponse<CaseDto>

    @PUT("cases/{caseId}")
    suspend fun updateCase(
        @Header("Authorization") authorization: String,
        @Path("caseId") caseId: String,
        @Body request: UpdateCaseRequest,
    ): ApiResponse<CaseDto>

    @POST("cases/{caseId}/archive")
    suspend fun archiveCase(
        @Header("Authorization") authorization: String,
        @Path("caseId") caseId: String,
    ): ApiResponse<CaseDto>

    @DELETE("cases/{caseId}")
    suspend fun deleteCase(
        @Header("Authorization") authorization: String,
        @Path("caseId") caseId: String,
    ): ApiResponse<DeleteResult>

    @GET("follow-up-plans")
    suspend fun followUpPlans(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
        @Query("status") status: String? = null,
        @Query("patientId") patientId: String? = null,
        @Query("caseId") caseId: String? = null,
    ): ApiResponse<PageData<FollowUpPlanDto>>

    @GET("follow-up-plans/today")
    suspend fun todayFollowUpPlans(
        @Header("Authorization") authorization: String,
    ): ApiResponse<List<FollowUpPlanDto>>

    @GET("follow-up-plans/overdue")
    suspend fun overdueFollowUpPlans(
        @Header("Authorization") authorization: String,
    ): ApiResponse<List<FollowUpPlanDto>>

    @POST("follow-up-plans")
    suspend fun createFollowUpPlan(
        @Header("Authorization") authorization: String,
        @Body request: CreateFollowUpPlanRequest,
    ): ApiResponse<FollowUpPlanDto>

    @DELETE("follow-up-plans/{planId}")
    suspend fun deleteFollowUpPlan(
        @Header("Authorization") authorization: String,
        @Path("planId") planId: String,
    ): ApiResponse<DeleteResult>

    @GET("follow-up-records")
    suspend fun followUpRecords(
        @Header("Authorization") authorization: String,
        @Query("patientId") patientId: String? = null,
        @Query("caseId") caseId: String? = null,
    ): ApiResponse<FollowUpRecordListData>

    @POST("follow-up-records")
    suspend fun createFollowUpRecord(
        @Header("Authorization") authorization: String,
        @Body request: CreateFollowUpRecordRequest,
    ): ApiResponse<CreateFollowUpRecordData>

    @PUT("follow-up-records/{recordId}")
    suspend fun updateFollowUpRecord(
        @Header("Authorization") authorization: String,
        @Path("recordId") recordId: String,
        @Body request: UpdateFollowUpRecordRequest,
    ): ApiResponse<FollowUpRecordDto>

    @DELETE("follow-up-records/{recordId}")
    suspend fun deleteFollowUpRecord(
        @Header("Authorization") authorization: String,
        @Path("recordId") recordId: String,
    ): ApiResponse<DeleteResult>
}
