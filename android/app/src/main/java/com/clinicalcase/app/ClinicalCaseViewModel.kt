package com.clinicalcase.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.clinicalcase.app.data.ClinicalCaseRepository
import com.clinicalcase.app.data.api.CaseDto
import com.clinicalcase.app.data.api.CreateCaseRequest
import com.clinicalcase.app.data.api.CreateFollowUpPlanRequest
import com.clinicalcase.app.data.api.CreateFollowUpRecordRequest
import com.clinicalcase.app.data.api.CreatePatientRequest
import com.clinicalcase.app.data.api.DashboardSummary
import com.clinicalcase.app.data.api.FollowUpPlanDto
import com.clinicalcase.app.data.api.FollowUpRecordDto
import com.clinicalcase.app.data.api.PatientDto
import com.clinicalcase.app.data.api.UpdateCaseRequest
import com.clinicalcase.app.data.api.UpdateFollowUpRecordRequest
import com.clinicalcase.app.data.api.UpdatePatientRequest
import com.clinicalcase.app.data.session.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppScreen {
    Dashboard,
    Patients,
    NewPatient,
    EditPatient,
    PatientDetail,
    NewCase,
    CaseDetail,
    EditCase,
    FollowUps,
    NewFollowUpPlan,
    NewFollowUpRecord,
    EditFollowUpRecord,
    ChangePassword,
}

data class PatientFormState(
    val name: String = "",
    val gender: String = "female",
    val birthDate: String = "",
    val phone: String = "",
    val outpatientNo: String = "",
    val inpatientNo: String = "",
    val firstVisitDate: String = "2026-06-19",
    val remark: String = "",
)

data class CaseFormState(
    val title: String = "",
    val visitType: String = "inpatient",
    val laterality: String = "left",
    val diseaseType: String = "breast_cancer",
    val confirmedDiagnosis: String = "",
    val pathologyType: String = "",
    val erStatus: String = "positive",
    val prStatus: String = "positive",
    val her2Status: String = "unknown",
    val ki67Percent: String = "",
    val molecularSubtype: String = "unknown",
    val surgeryDate: String = "",
    val surgeryType: String = "",
    val currentStatus: String = "treating",
    val summary: String = "",
    val caseStatus: String = "draft",
)

data class FollowUpPlanFormState(
    val followUpType: String = "routine",
    val plannedDate: String = "2026-06-19",
)

data class FollowUpRecordFormState(
    val actualDate: String = "2026-06-19",
    val method: String = "phone",
    val survivalStatus: String = "unknown",
    val hasRecurrence: String = "false",
    val recurrenceSite: String = "",
    val hasMetastasis: String = "false",
    val metastasisSite: String = "",
    val imagingSummary: String = "",
    val tumorMarkerSummary: String = "",
    val currentTreatment: String = "",
    val adverseReactions: String = "",
    val remark: String = "",
    val nextFollowUpDate: String = "",
)

data class ChangePasswordFormState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
)

data class ClinicalCaseUiState(
    val loadingSession: Boolean = true,
    val session: Session? = null,
    val authMode: String = "login",
    val username: String = "admin",
    val password: String = "ChangeMe123",
    val registerDisplayName: String = "",
    val registerConfirmPassword: String = "",
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val dashboard: DashboardSummary? = null,
    val screen: AppScreen = AppScreen.Dashboard,
    val patients: List<PatientDto> = emptyList(),
    val patientKeyword: String = "",
    val patientPage: Int = 1,
    val patientPageSize: Int = 20,
    val patientTotal: Int = 0,
    val patientForm: PatientFormState = PatientFormState(),
    val selectedPatient: PatientDto? = null,
    val patientCases: List<CaseDto> = emptyList(),
    val selectedCase: CaseDto? = null,
    val caseForm: CaseFormState = CaseFormState(),
    val followUpPlans: List<FollowUpPlanDto> = emptyList(),
    val followUpRecords: List<FollowUpRecordDto> = emptyList(),
    val selectedFollowUpPlan: FollowUpPlanDto? = null,
    val selectedFollowUpRecord: FollowUpRecordDto? = null,
    val followUpTitle: String = "随访计划",
    val followUpListMode: String = "case",
    val followUpPlanForm: FollowUpPlanFormState = FollowUpPlanFormState(),
    val followUpRecordForm: FollowUpRecordFormState = FollowUpRecordFormState(),
    val changePasswordForm: ChangePasswordFormState = ChangePasswordFormState(),
    val successMessage: String? = null,
)

class ClinicalCaseViewModel(
    private val repository: ClinicalCaseRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClinicalCaseUiState())
    val uiState: StateFlow<ClinicalCaseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.sessionFlow.collect { session ->
                _uiState.update {
                    it.copy(
                        loadingSession = false,
                        session = session,
                        loading = false,
                        dashboard = if (session == null) null else it.dashboard,
                        screen = if (session == null) AppScreen.Dashboard else it.screen,
                        selectedPatient = if (session == null) null else it.selectedPatient,
                        patientCases = if (session == null) emptyList() else it.patientCases,
                        selectedCase = if (session == null) null else it.selectedCase,
                        followUpPlans = if (session == null) emptyList() else it.followUpPlans,
                        followUpRecords = if (session == null) emptyList() else it.followUpRecords,
                        errorMessage = if (session == null) null else it.errorMessage,
                    )
                }
                if (session != null) {
                    loadDashboard(session.accessToken)
                }
            }
        }
    }

    fun updateUsername(value: String) {
        _uiState.update { it.copy(username = value, errorMessage = null) }
    }

    fun updatePassword(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun updateRegisterDisplayName(value: String) {
        _uiState.update { it.copy(registerDisplayName = value, errorMessage = null) }
    }

    fun updateRegisterConfirmPassword(value: String) {
        _uiState.update { it.copy(registerConfirmPassword = value, errorMessage = null) }
    }

    fun showLogin() {
        _uiState.update { it.copy(authMode = "login", errorMessage = null) }
    }

    fun showRegister() {
        _uiState.update {
            it.copy(
                authMode = "register",
                username = "",
                password = "",
                registerDisplayName = "",
                registerConfirmPassword = "",
                errorMessage = null,
            )
        }
    }

    fun login() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.login(state.username.trim(), state.password)
            }.onSuccess { session ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        session = session,
                    )
                }
                loadDashboard(session.accessToken)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "登录失败",
                    )
                }
            }
        }
    }

    fun register() {
        val state = _uiState.value
        if (state.username.isBlank() || state.registerDisplayName.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请填写账号、姓名和密码") }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "密码至少 6 位") }
            return
        }
        if (state.password != state.registerConfirmPassword) {
            _uiState.update { it.copy(errorMessage = "两次输入的密码不一致") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.register(
                    username = state.username.trim(),
                    password = state.password,
                    displayName = state.registerDisplayName.trim(),
                )
            }.onSuccess { session ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        session = session,
                        authMode = "login",
                        registerConfirmPassword = "",
                    )
                }
                loadDashboard(session.accessToken)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "注册失败",
                    )
                }
            }
        }
    }

    fun refreshDashboard() {
        val token = _uiState.value.session?.accessToken ?: return
        loadDashboard(token)
    }

    fun openDashboard() {
        _uiState.update { it.copy(screen = AppScreen.Dashboard, errorMessage = null) }
        refreshDashboard()
    }

    fun openChangePassword() {
        _uiState.update {
            it.copy(
                screen = AppScreen.ChangePassword,
                changePasswordForm = ChangePasswordFormState(),
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun updateChangePasswordForm(transform: (ChangePasswordFormState) -> ChangePasswordFormState) {
        _uiState.update {
            it.copy(
                changePasswordForm = transform(it.changePasswordForm),
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun changePassword() {
        val state = _uiState.value
        val token = state.session?.accessToken ?: return
        val form = state.changePasswordForm
        if (form.currentPassword.isBlank() || form.newPassword.isBlank() || form.confirmPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请填写旧密码、新密码和确认密码") }
            return
        }
        if (form.newPassword.length < 6) {
            _uiState.update { it.copy(errorMessage = "新密码至少 6 位") }
            return
        }
        if (form.newPassword != form.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "两次输入的新密码不一致") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null, successMessage = null) }
            runCatching {
                repository.changePassword(
                    accessToken = token,
                    currentPassword = form.currentPassword,
                    newPassword = form.newPassword,
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        loading = false,
                        screen = AppScreen.Dashboard,
                        changePasswordForm = ChangePasswordFormState(),
                        successMessage = "密码已修改",
                    )
                }
                refreshDashboard()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "修改密码失败",
                    )
                }
            }
        }
    }

    fun openPatients() {
        _uiState.update {
            it.copy(
                screen = AppScreen.Patients,
                selectedPatient = null,
                patientCases = emptyList(),
                selectedCase = null,
                followUpPlans = emptyList(),
                followUpRecords = emptyList(),
                selectedFollowUpPlan = null,
                selectedFollowUpRecord = null,
                errorMessage = null,
            )
        }
        loadPatients()
    }

    fun openNewPatient() {
        _uiState.update {
            it.copy(
                screen = AppScreen.NewPatient,
                errorMessage = null,
                patientForm = PatientFormState(),
            )
        }
    }

    fun openEditPatient() {
        val patient = _uiState.value.selectedPatient ?: return
        _uiState.update {
            it.copy(
                screen = AppScreen.EditPatient,
                errorMessage = null,
                patientForm = patient.toFormState(),
            )
        }
    }

    fun openPatientDetail(patient: PatientDto) {
        _uiState.update {
            it.copy(
                screen = AppScreen.PatientDetail,
                selectedPatient = patient,
                patientCases = emptyList(),
                selectedCase = null,
                followUpPlans = emptyList(),
                followUpRecords = emptyList(),
                selectedFollowUpPlan = null,
                selectedFollowUpRecord = null,
                errorMessage = null,
            )
        }
        loadCases(patient.id)
    }

    fun openCaseDetail(caseItem: CaseDto) {
        _uiState.update {
            it.copy(
                screen = AppScreen.CaseDetail,
                selectedCase = caseItem,
                followUpPlans = emptyList(),
                followUpRecords = emptyList(),
                selectedFollowUpPlan = null,
                selectedFollowUpRecord = null,
                errorMessage = null,
            )
        }
        loadCaseDetail(caseItem.id)
        loadFollowUpPlans(caseId = caseItem.id)
        loadFollowUpRecords(caseId = caseItem.id)
    }

    fun openFollowUpPlanCase(plan: FollowUpPlanDto) {
        val token = _uiState.value.session?.accessToken ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                val patient = repository.patientDetail(token, plan.patientId)
                val caseItem = repository.caseDetail(token, plan.caseId)
                patient to caseItem
            }.onSuccess { (patient, caseItem) ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        screen = AppScreen.CaseDetail,
                        selectedPatient = patient,
                        selectedCase = caseItem,
                        patientCases = emptyList(),
                        followUpPlans = emptyList(),
                        followUpRecords = emptyList(),
                        selectedFollowUpPlan = null,
                        selectedFollowUpRecord = null,
                    )
                }
                loadCases(patient.id)
                loadFollowUpPlans(caseId = caseItem.id)
                loadFollowUpRecords(caseId = caseItem.id)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "打开病例失败",
                    )
                }
            }
        }
    }

    fun openNewCase() {
        val patient = _uiState.value.selectedPatient ?: return
        _uiState.update {
            it.copy(
                screen = AppScreen.NewCase,
                errorMessage = null,
                caseForm = CaseFormState(title = "${patient.name} 病例"),
            )
        }
    }

    fun openEditCase() {
        val caseItem = _uiState.value.selectedCase ?: return
        val role = _uiState.value.session?.role
        if (caseItem.caseStatus == "archived" && role != "admin") {
            _uiState.update { it.copy(errorMessage = "已归档病例已锁定，仅管理员可修改") }
            return
        }
        _uiState.update {
            it.copy(
                screen = AppScreen.EditCase,
                errorMessage = null,
                caseForm = caseItem.toFormState(),
            )
        }
    }

    fun openTodayFollowUps() {
        _uiState.update {
            it.copy(
                screen = AppScreen.FollowUps,
                followUpTitle = "今日随访",
                followUpListMode = "today",
                followUpPlans = emptyList(),
                selectedFollowUpPlan = null,
                errorMessage = null,
            )
        }
        loadTodayFollowUps()
    }

    fun openOverdueFollowUps() {
        _uiState.update {
            it.copy(
                screen = AppScreen.FollowUps,
                followUpTitle = "逾期随访",
                followUpListMode = "overdue",
                followUpPlans = emptyList(),
                selectedFollowUpPlan = null,
                errorMessage = null,
            )
        }
        loadOverdueFollowUps()
    }

    fun openCaseFollowUps() {
        val caseItem = _uiState.value.selectedCase ?: return
        _uiState.update {
            it.copy(
                screen = AppScreen.FollowUps,
                followUpTitle = "全部随访记录",
                followUpListMode = "case_records",
                followUpPlans = emptyList(),
                followUpRecords = emptyList(),
                errorMessage = null,
            )
        }
        loadFollowUpRecords(caseId = caseItem.id)
    }

    fun openNewFollowUpPlan() {
        val state = _uiState.value
        val caseItem = state.selectedCase ?: return
        if (state.selectedPatient == null) return
        if (caseItem.isLockedFor(state.session?.role)) {
            _uiState.update { it.copy(errorMessage = "已归档病例已锁定，仅管理员可新增随访计划") }
            return
        }
        _uiState.update {
            it.copy(
                screen = AppScreen.NewFollowUpPlan,
                errorMessage = null,
                followUpPlanForm = FollowUpPlanFormState(),
            )
        }
    }

    fun openNewFollowUpRecord(plan: FollowUpPlanDto) {
        val state = _uiState.value
        if (state.selectedCase?.isLockedFor(state.session?.role) == true) {
            _uiState.update { it.copy(errorMessage = "已归档病例已锁定，仅管理员可填写随访记录") }
            return
        }
        if (plan.status == "completed") {
            _uiState.update { it.copy(errorMessage = "该随访计划已完成") }
            return
        }
        _uiState.update {
            it.copy(
                screen = AppScreen.NewFollowUpRecord,
                selectedFollowUpPlan = plan,
                errorMessage = null,
                followUpRecordForm = FollowUpRecordFormState(),
            )
        }
    }

    fun openEditFollowUpRecord(record: FollowUpRecordDto) {
        _uiState.update {
            it.copy(
                screen = AppScreen.EditFollowUpRecord,
                selectedFollowUpRecord = record,
                errorMessage = null,
                followUpRecordForm = record.toFormState(),
            )
        }
    }

    fun updatePatientKeyword(value: String) {
        _uiState.update { it.copy(patientKeyword = value, errorMessage = null) }
    }

    fun searchPatients() {
        _uiState.update { it.copy(patientPage = 1, errorMessage = null) }
        loadPatients()
    }

    fun previousPatientPage() {
        val state = _uiState.value
        if (state.patientPage <= 1) return
        _uiState.update { it.copy(patientPage = it.patientPage - 1, errorMessage = null) }
        loadPatients()
    }

    fun nextPatientPage() {
        val state = _uiState.value
        val totalPages = ((state.patientTotal + state.patientPageSize - 1) / state.patientPageSize).coerceAtLeast(1)
        if (state.patientPage >= totalPages) return
        _uiState.update { it.copy(patientPage = it.patientPage + 1, errorMessage = null) }
        loadPatients()
    }

    fun updatePatientForm(transform: (PatientFormState) -> PatientFormState) {
        _uiState.update { it.copy(patientForm = transform(it.patientForm), errorMessage = null) }
    }

    fun updateCaseForm(transform: (CaseFormState) -> CaseFormState) {
        _uiState.update { it.copy(caseForm = transform(it.caseForm), errorMessage = null) }
    }

    fun updateFollowUpPlanForm(transform: (FollowUpPlanFormState) -> FollowUpPlanFormState) {
        _uiState.update { it.copy(followUpPlanForm = transform(it.followUpPlanForm), errorMessage = null) }
    }

    fun updateFollowUpRecordForm(transform: (FollowUpRecordFormState) -> FollowUpRecordFormState) {
        _uiState.update { it.copy(followUpRecordForm = transform(it.followUpRecordForm), errorMessage = null) }
    }

    fun createPatient() {
        val state = _uiState.value
        val token = state.session?.accessToken ?: return
        val form = state.patientForm
        if (form.name.isBlank() || form.firstVisitDate.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请填写姓名和初诊日期") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.createPatient(
                    accessToken = token,
                    request = CreatePatientRequest(
                        name = form.name.trim(),
                        gender = form.gender.trim(),
                        birthDate = form.birthDate.takeIf { it.isNotBlank() },
                        phone = form.phone.takeIf { it.isNotBlank() },
                        outpatientNo = form.outpatientNo.takeIf { it.isNotBlank() },
                        inpatientNo = form.inpatientNo.takeIf { it.isNotBlank() },
                        firstVisitDate = form.firstVisitDate.trim(),
                        remark = form.remark.takeIf { it.isNotBlank() },
                    ),
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        loading = false,
                        screen = AppScreen.Patients,
                        patientKeyword = "",
                        patientPage = 1,
                        patientForm = PatientFormState(),
                    )
                }
                loadPatients()
                refreshDashboard()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "创建患者失败",
                    )
                }
            }
        }
    }

    fun updatePatient() {
        val state = _uiState.value
        val token = state.session?.accessToken ?: return
        val patient = state.selectedPatient ?: return
        val form = state.patientForm
        if (form.name.isBlank() || form.firstVisitDate.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请填写姓名和初诊日期") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.updatePatient(
                    accessToken = token,
                    patientId = patient.id,
                    request = UpdatePatientRequest(
                        name = form.name.trim(),
                        gender = form.gender.trim(),
                        birthDate = form.birthDate.takeIf { it.isNotBlank() },
                        phone = form.phone.takeIf { it.isNotBlank() },
                        outpatientNo = form.outpatientNo.takeIf { it.isNotBlank() },
                        inpatientNo = form.inpatientNo.takeIf { it.isNotBlank() },
                        firstVisitDate = form.firstVisitDate.trim(),
                        remark = form.remark.takeIf { it.isNotBlank() },
                    ),
                )
            }.onSuccess { updated ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        screen = AppScreen.PatientDetail,
                        selectedPatient = updated,
                        patients = it.patients.map { item ->
                            if (item.id == updated.id) updated else item
                        },
                        patientForm = PatientFormState(),
                    )
                }
                loadCases(updated.id)
                refreshDashboard()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "更新患者失败",
                    )
                }
            }
        }
    }

    fun deletePatient(patient: PatientDto) {
        val state = _uiState.value
        val token = state.session?.accessToken ?: return
        if (state.session.role != "admin") {
            _uiState.update { it.copy(errorMessage = "只有管理员可以删除患者") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.deletePatient(token, patient.id)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        loading = false,
                        screen = AppScreen.Patients,
                        patients = it.patients.filterNot { item -> item.id == patient.id },
                        selectedPatient = if (it.selectedPatient?.id == patient.id) null else it.selectedPatient,
                        patientCases = if (it.selectedPatient?.id == patient.id) emptyList() else it.patientCases,
                    )
                }
                loadPatients()
                refreshDashboard()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "删除患者失败",
                    )
                }
            }
        }
    }

    fun createCase() {
        val state = _uiState.value
        val token = state.session?.accessToken ?: return
        val patient = state.selectedPatient ?: return
        val form = state.caseForm
        if (form.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请填写病例标题") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.createCase(
                    accessToken = token,
                    request = CreateCaseRequest(
                        patientId = patient.id,
                        title = form.title.trim(),
                        visitType = form.visitType.trim(),
                        laterality = form.laterality.trim(),
                        diseaseType = form.diseaseType.trim(),
                        confirmedDiagnosis = form.confirmedDiagnosis.takeIf { it.isNotBlank() },
                        pathologyType = form.pathologyType.takeIf { it.isNotBlank() },
                        erStatus = form.erStatus.takeIf { it.isNotBlank() },
                        prStatus = form.prStatus.takeIf { it.isNotBlank() },
                        her2Status = form.her2Status.takeIf { it.isNotBlank() },
                        ki67Percent = form.ki67Percent.toDoubleOrNull(),
                        molecularSubtype = form.molecularSubtype.takeIf { it.isNotBlank() },
                        surgeryDate = form.surgeryDate.takeIf { it.isNotBlank() },
                        surgeryType = form.surgeryType.takeIf { it.isNotBlank() },
                        currentStatus = form.currentStatus.trim(),
                        summary = form.summary.takeIf { it.isNotBlank() },
                        caseStatus = form.caseStatus.trim(),
                    ),
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        loading = false,
                        screen = AppScreen.PatientDetail,
                        caseForm = CaseFormState(),
                    )
                }
                loadCases(patient.id)
                refreshDashboard()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "创建病例失败",
                    )
                }
            }
        }
    }

    fun updateCase() {
        val state = _uiState.value
        val token = state.session?.accessToken ?: return
        val caseItem = state.selectedCase ?: return
        val patientId = state.selectedPatient?.id ?: caseItem.patientId
        val form = state.caseForm
        if (form.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请填写病例标题") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.updateCase(
                    accessToken = token,
                    caseId = caseItem.id,
                    request = UpdateCaseRequest(
                        title = form.title.trim(),
                        visitType = form.visitType.trim(),
                        laterality = form.laterality.trim(),
                        diseaseType = form.diseaseType.trim(),
                        confirmedDiagnosis = form.confirmedDiagnosis.takeIf { it.isNotBlank() },
                        pathologyType = form.pathologyType.takeIf { it.isNotBlank() },
                        erStatus = form.erStatus.takeIf { it.isNotBlank() },
                        prStatus = form.prStatus.takeIf { it.isNotBlank() },
                        her2Status = form.her2Status.takeIf { it.isNotBlank() },
                        ki67Percent = form.ki67Percent.toDoubleOrNull(),
                        molecularSubtype = form.molecularSubtype.takeIf { it.isNotBlank() },
                        surgeryDate = form.surgeryDate.takeIf { it.isNotBlank() },
                        surgeryType = form.surgeryType.takeIf { it.isNotBlank() },
                        currentStatus = form.currentStatus.trim(),
                        summary = form.summary.takeIf { it.isNotBlank() },
                        caseStatus = form.caseStatus.trim(),
                    ),
                )
            }.onSuccess { updated ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        screen = AppScreen.CaseDetail,
                        selectedCase = updated,
                    )
                }
                loadCases(patientId)
                refreshDashboard()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "更新病例失败",
                    )
                }
            }
        }
    }

    fun deleteCase(caseItem: CaseDto) {
        val state = _uiState.value
        val token = state.session?.accessToken ?: return
        if (state.session.role != "admin") {
            _uiState.update { it.copy(errorMessage = "只有管理员可以删除病例") }
            return
        }
        val patientId = state.selectedPatient?.id ?: caseItem.patientId

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.deleteCase(token, caseItem.id)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        loading = false,
                        screen = AppScreen.PatientDetail,
                        selectedCase = if (it.selectedCase?.id == caseItem.id) null else it.selectedCase,
                        patientCases = it.patientCases.filterNot { item -> item.id == caseItem.id },
                    )
                }
                loadCases(patientId)
                refreshDashboard()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "删除病例失败",
                    )
                }
            }
        }
    }

    fun archiveCase() {
        val state = _uiState.value
        val token = state.session?.accessToken ?: return
        val caseItem = state.selectedCase ?: return
        val patientId = state.selectedPatient?.id ?: caseItem.patientId

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.archiveCase(token, caseItem.id)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        loading = false,
                        screen = AppScreen.CaseDetail,
                        selectedCase = it.selectedCase?.copy(caseStatus = "archived") ?: caseItem.copy(caseStatus = "archived"),
                        patientCases = it.patientCases.map { item ->
                            if (item.id == caseItem.id) item.copy(caseStatus = "archived") else item
                        },
                    )
                }
                loadCases(patientId)
                loadCaseDetail(caseItem.id)
                refreshDashboard()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "归档病例失败",
                    )
                }
            }
        }
    }

    fun createFollowUpPlan() {
        val state = _uiState.value
        val token = state.session?.accessToken ?: return
        val patient = state.selectedPatient ?: return
        val caseItem = state.selectedCase ?: return
        val form = state.followUpPlanForm
        if (form.plannedDate.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请填写计划随访日期") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.createFollowUpPlan(
                    accessToken = token,
                    request = CreateFollowUpPlanRequest(
                        patientId = patient.id,
                        caseId = caseItem.id,
                        followUpType = form.followUpType.trim(),
                        plannedDate = form.plannedDate.trim(),
                    ),
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        loading = false,
                        screen = AppScreen.CaseDetail,
                        followUpPlanForm = FollowUpPlanFormState(),
                    )
                }
                loadFollowUpPlans(caseId = caseItem.id)
                refreshDashboard()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "创建随访计划失败",
                    )
                }
            }
        }
    }

    fun deleteFollowUpPlan(plan: FollowUpPlanDto) {
        val token = _uiState.value.session?.accessToken ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.deleteFollowUpPlan(token, plan.id)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        loading = false,
                        followUpPlans = it.followUpPlans.filterNot { item -> item.id == plan.id },
                    )
                }
                loadFollowUpPlans(caseId = plan.caseId)
                loadFollowUpRecords(caseId = plan.caseId)
                refreshDashboard()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "删除随访计划失败",
                    )
                }
            }
        }
    }

    fun createFollowUpRecord() {
        val state = _uiState.value
        val token = state.session?.accessToken ?: return
        val plan = state.selectedFollowUpPlan ?: return
        val form = state.followUpRecordForm
        if (form.actualDate.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请填写实际随访日期") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.createFollowUpRecord(
                    accessToken = token,
                    request = CreateFollowUpRecordRequest(
                        followUpPlanId = plan.id,
                        actualDate = form.actualDate.trim(),
                        method = form.method.trim(),
                        survivalStatus = form.survivalStatus.trim(),
                        hasRecurrence = form.hasRecurrence.toBooleanStrictOrNull(),
                        recurrenceSite = form.recurrenceSite.takeIf { it.isNotBlank() },
                        hasMetastasis = form.hasMetastasis.toBooleanStrictOrNull(),
                        metastasisSite = form.metastasisSite.takeIf { it.isNotBlank() },
                        imagingSummary = form.imagingSummary.takeIf { it.isNotBlank() },
                        tumorMarkerSummary = form.tumorMarkerSummary.takeIf { it.isNotBlank() },
                        currentTreatment = form.currentTreatment.takeIf { it.isNotBlank() },
                        adverseReactions = form.adverseReactions.takeIf { it.isNotBlank() },
                        remark = form.remark.takeIf { it.isNotBlank() },
                        nextFollowUpDate = form.nextFollowUpDate.takeIf { it.isNotBlank() },
                    ),
                )
            }.onSuccess { result ->
                _uiState.update {
                    val updatedRecords = (listOf(result.record) + it.followUpRecords)
                        .distinctBy { record -> record.id }
                        .sortedByDescending { record -> record.actualDate }
                    it.copy(
                        loading = false,
                        screen = if (state.selectedCase != null) AppScreen.CaseDetail else AppScreen.FollowUps,
                        followUpRecordForm = FollowUpRecordFormState(),
                        selectedFollowUpPlan = null,
                        followUpRecords = updatedRecords,
                    )
                }
                if (state.selectedCase != null) {
                    loadFollowUpPlans(caseId = plan.caseId)
                } else {
                    reloadCurrentFollowUpPlans()
                }
                loadFollowUpRecords(caseId = plan.caseId)
                refreshDashboard()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "创建随访记录失败",
                    )
                }
            }
        }
    }

    fun updateFollowUpRecord() {
        val state = _uiState.value
        val token = state.session?.accessToken ?: return
        val record = state.selectedFollowUpRecord ?: return
        val form = state.followUpRecordForm
        if (form.actualDate.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请填写实际随访日期") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.updateFollowUpRecord(
                    accessToken = token,
                    recordId = record.id,
                    request = UpdateFollowUpRecordRequest(
                        actualDate = form.actualDate.trim(),
                        method = form.method.trim(),
                        survivalStatus = form.survivalStatus.trim(),
                        hasRecurrence = form.hasRecurrence.toBooleanStrictOrNull(),
                        recurrenceSite = form.recurrenceSite.takeIf { it.isNotBlank() },
                        hasMetastasis = form.hasMetastasis.toBooleanStrictOrNull(),
                        metastasisSite = form.metastasisSite.takeIf { it.isNotBlank() },
                        imagingSummary = form.imagingSummary.takeIf { it.isNotBlank() },
                        tumorMarkerSummary = form.tumorMarkerSummary.takeIf { it.isNotBlank() },
                        currentTreatment = form.currentTreatment.takeIf { it.isNotBlank() },
                        adverseReactions = form.adverseReactions.takeIf { it.isNotBlank() },
                        remark = form.remark.takeIf { it.isNotBlank() },
                        nextFollowUpDate = form.nextFollowUpDate.takeIf { it.isNotBlank() },
                    ),
                )
            }.onSuccess { updated ->
                _uiState.update {
                    val updatedRecords = (listOf(updated) + it.followUpRecords)
                        .distinctBy { item -> item.id }
                        .sortedByDescending { item -> item.actualDate }
                    it.copy(
                        loading = false,
                        screen = AppScreen.CaseDetail,
                        selectedFollowUpRecord = null,
                        followUpRecordForm = FollowUpRecordFormState(),
                        followUpRecords = updatedRecords,
                    )
                }
                loadFollowUpRecords(caseId = record.caseId)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "更新随访记录失败",
                    )
                }
            }
        }
    }

    fun deleteFollowUpRecord(record: FollowUpRecordDto) {
        val state = _uiState.value
        val token = state.session?.accessToken ?: return
        if (state.session.role != "admin") {
            _uiState.update { it.copy(errorMessage = "只有管理员可以删除随访记录") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.deleteFollowUpRecord(token, record.id)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        loading = false,
                        followUpRecords = it.followUpRecords.filterNot { item -> item.id == record.id },
                        selectedFollowUpRecord = if (it.selectedFollowUpRecord?.id == record.id) null else it.selectedFollowUpRecord,
                    )
                }
                loadFollowUpRecords(caseId = record.caseId)
                loadFollowUpPlans(caseId = record.caseId)
                refreshDashboard()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "删除随访记录失败",
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.update {
                it.copy(
                    session = null,
                    dashboard = null,
                    screen = AppScreen.Dashboard,
                    patients = emptyList(),
                    selectedPatient = null,
                    patientCases = emptyList(),
                    selectedCase = null,
                    followUpPlans = emptyList(),
                    followUpRecords = emptyList(),
                    selectedFollowUpPlan = null,
                    selectedFollowUpRecord = null,
                    errorMessage = null,
                )
            }
        }
    }

    private fun loadDashboard(accessToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.dashboardSummary(accessToken)
            }.onSuccess { dashboard ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        dashboard = dashboard,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "加载首页失败",
                    )
                }
            }
        }
    }

    private fun loadPatients() {
        val state = _uiState.value
        val token = state.session?.accessToken ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.patients(
                    accessToken = token,
                    keyword = state.patientKeyword,
                    page = state.patientPage,
                    pageSize = state.patientPageSize,
                )
            }.onSuccess { page ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        patients = page.items,
                        patientPage = page.page,
                        patientPageSize = page.pageSize,
                        patientTotal = page.total,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "加载患者列表失败",
                    )
                }
            }
        }
    }

    private fun loadCases(patientId: String) {
        val token = _uiState.value.session?.accessToken ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.cases(token, patientId)
            }.onSuccess { cases ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        patientCases = cases,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "加载病例列表失败",
                    )
                }
            }
        }
    }

    private fun loadCaseDetail(caseId: String) {
        val token = _uiState.value.session?.accessToken ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.caseDetail(token, caseId)
            }.onSuccess { caseItem ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        selectedCase = caseItem,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "加载病例详情失败",
                    )
                }
            }
        }
    }

    private fun loadFollowUpPlans(status: String? = null, caseId: String? = null) {
        val token = _uiState.value.session?.accessToken ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.followUpPlans(token, status = status, caseId = caseId)
            }.onSuccess { plans ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        followUpPlans = plans.toPendingPlans(it.followUpRecords),
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "加载随访计划失败",
                    )
                }
            }
        }
    }

    private fun reloadCurrentFollowUpPlans() {
        val state = _uiState.value
        when (state.followUpListMode) {
            "today" -> loadTodayFollowUps()
            "overdue" -> loadOverdueFollowUps()
            "case_records" -> loadFollowUpRecords(caseId = state.selectedCase?.id)
            else -> loadFollowUpPlans(caseId = state.selectedCase?.id)
        }
    }

    private fun loadFollowUpRecords(caseId: String? = null) {
        val token = _uiState.value.session?.accessToken ?: return
        viewModelScope.launch {
            runCatching {
                repository.followUpRecords(token, caseId = caseId)
            }.onSuccess { records ->
                _uiState.update {
                    val sortedRecords = records.sortedByDescending { record -> record.actualDate }
                    it.copy(
                        followUpRecords = sortedRecords,
                        followUpPlans = it.followUpPlans.toPendingPlans(sortedRecords),
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "加载随访记录失败")
                }
            }
        }
    }

    private fun loadTodayFollowUps() {
        val token = _uiState.value.session?.accessToken ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.todayFollowUpPlans(token)
            }.onSuccess { plans ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        followUpPlans = plans.toPendingPlans(it.followUpRecords),
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "加载今日随访失败",
                    )
                }
            }
        }
    }

    private fun loadOverdueFollowUps() {
        val token = _uiState.value.session?.accessToken ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                repository.overdueFollowUpPlans(token)
            }.onSuccess { plans ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        followUpPlans = plans.toPendingPlans(it.followUpRecords),
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "加载逾期随访失败",
                    )
                }
            }
        }
    }
}

private fun CaseDto.isLockedFor(role: String?): Boolean = caseStatus == "archived" && role != "admin"

private fun List<FollowUpPlanDto>.toPendingPlans(records: List<FollowUpRecordDto>): List<FollowUpPlanDto> {
    val completedPlanIds = records.map { it.followUpPlanId }.toSet()
    return filterNot { plan -> plan.status == "completed" || plan.id in completedPlanIds }
        .sortedBy { plan -> plan.plannedDate }
}

private fun PatientDto.toFormState(): PatientFormState = PatientFormState(
    name = name,
    gender = gender,
    birthDate = birthDate?.take(10).orEmpty(),
    phone = phone.orEmpty(),
    outpatientNo = outpatientNo.orEmpty(),
    inpatientNo = inpatientNo.orEmpty(),
    firstVisitDate = firstVisitDate.take(10),
    remark = remark.orEmpty(),
)

private fun CaseDto.toFormState(): CaseFormState = CaseFormState(
    title = title,
    visitType = visitType,
    laterality = laterality,
    diseaseType = diseaseType,
    confirmedDiagnosis = confirmedDiagnosis.orEmpty(),
    pathologyType = pathologyType.orEmpty(),
    erStatus = erStatus.orEmpty(),
    prStatus = prStatus.orEmpty(),
    her2Status = her2Status.orEmpty(),
    ki67Percent = ki67Percent?.toString().orEmpty(),
    molecularSubtype = molecularSubtype.orEmpty(),
    surgeryDate = surgeryDate?.take(10).orEmpty(),
    surgeryType = surgeryType.orEmpty(),
    currentStatus = currentStatus,
    summary = summary.orEmpty(),
    caseStatus = caseStatus,
)

private fun FollowUpRecordDto.toFormState(): FollowUpRecordFormState = FollowUpRecordFormState(
    actualDate = actualDate.take(10),
    method = method,
    survivalStatus = survivalStatus,
    hasRecurrence = hasRecurrence?.toString() ?: "false",
    recurrenceSite = recurrenceSite.orEmpty(),
    hasMetastasis = hasMetastasis?.toString() ?: "false",
    metastasisSite = metastasisSite.orEmpty(),
    imagingSummary = imagingSummary.orEmpty(),
    tumorMarkerSummary = tumorMarkerSummary.orEmpty(),
    currentTreatment = currentTreatment.orEmpty(),
    adverseReactions = adverseReactions.orEmpty(),
    remark = remark.orEmpty(),
    nextFollowUpDate = nextFollowUpDate?.take(10).orEmpty(),
)

class ClinicalCaseViewModelFactory(
    private val repository: ClinicalCaseRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClinicalCaseViewModel::class.java)) {
            return ClinicalCaseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
