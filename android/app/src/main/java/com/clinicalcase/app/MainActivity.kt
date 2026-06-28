package com.clinicalcase.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clinicalcase.app.data.ClinicalCaseRepository
import com.clinicalcase.app.data.api.ApiClient
import com.clinicalcase.app.data.api.CaseDto
import com.clinicalcase.app.data.api.DashboardSummary
import com.clinicalcase.app.data.api.FollowUpPlanDto
import com.clinicalcase.app.data.api.PatientDto
import com.clinicalcase.app.data.api.RecentCaseDto
import com.clinicalcase.app.data.session.SessionStore
import com.clinicalcase.app.ui.theme.ClinicalCaseTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private val repository by lazy {
        ClinicalCaseRepository(
            api = ApiClient.create(),
            sessionStore = SessionStore(applicationContext),
        )
    }

    private val viewModelFactory by lazy {
        ClinicalCaseViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClinicalCaseTheme {
                val viewModel: ClinicalCaseViewModel = viewModel(factory = viewModelFactory)
                val state by viewModel.uiState.collectAsState()
                ClinicalCaseApp(
                    state = state,
                    onUsernameChange = viewModel::updateUsername,
                    onPasswordChange = viewModel::updatePassword,
                    onRegisterDisplayNameChange = viewModel::updateRegisterDisplayName,
                    onRegisterConfirmPasswordChange = viewModel::updateRegisterConfirmPassword,
                    onLogin = viewModel::login,
                    onRegister = viewModel::register,
                    onShowLogin = viewModel::showLogin,
                    onShowRegister = viewModel::showRegister,
                    onLogout = viewModel::logout,
                    onRefresh = viewModel::refreshDashboard,
                    onOpenDashboard = viewModel::openDashboard,
                    onOpenChangePassword = viewModel::openChangePassword,
                    onOpenPatients = viewModel::openPatients,
                    onOpenNewPatient = viewModel::openNewPatient,
                    onOpenEditPatient = viewModel::openEditPatient,
                    onOpenPatientDetail = viewModel::openPatientDetail,
                    onOpenNewCase = viewModel::openNewCase,
                    onOpenCaseDetail = viewModel::openCaseDetail,
                    onOpenEditCase = viewModel::openEditCase,
                    onArchiveCase = viewModel::archiveCase,
                    onOpenTodayFollowUps = viewModel::openTodayFollowUps,
                    onOpenOverdueFollowUps = viewModel::openOverdueFollowUps,
                    onOpenCaseFollowUps = viewModel::openCaseFollowUps,
                    onOpenFollowUpPlanCase = viewModel::openFollowUpPlanCase,
                    onOpenNewFollowUpPlan = viewModel::openNewFollowUpPlan,
                    onOpenNewFollowUpRecord = viewModel::openNewFollowUpRecord,
                    onOpenEditFollowUpRecord = viewModel::openEditFollowUpRecord,
                    onPatientKeywordChange = viewModel::updatePatientKeyword,
                    onSearchPatients = viewModel::searchPatients,
                    onPreviousPatientPage = viewModel::previousPatientPage,
                    onNextPatientPage = viewModel::nextPatientPage,
                    onPatientFormChange = viewModel::updatePatientForm,
                    onCreatePatient = viewModel::createPatient,
                    onUpdatePatient = viewModel::updatePatient,
                    onDeletePatient = viewModel::deletePatient,
                    onCaseFormChange = viewModel::updateCaseForm,
                    onCreateCase = viewModel::createCase,
                    onUpdateCase = viewModel::updateCase,
                    onDeleteCase = viewModel::deleteCase,
                    onFollowUpPlanFormChange = viewModel::updateFollowUpPlanForm,
                    onCreateFollowUpPlan = viewModel::createFollowUpPlan,
                    onDeleteFollowUpPlan = viewModel::deleteFollowUpPlan,
                    onFollowUpRecordFormChange = viewModel::updateFollowUpRecordForm,
                    onCreateFollowUpRecord = viewModel::createFollowUpRecord,
                    onUpdateFollowUpRecord = viewModel::updateFollowUpRecord,
                    onDeleteFollowUpRecord = viewModel::deleteFollowUpRecord,
                    onChangePasswordFormChange = viewModel::updateChangePasswordForm,
                    onChangePassword = viewModel::changePassword,
                )
            }
        }
    }
}

@Composable
private fun ClinicalCaseApp(
    state: ClinicalCaseUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegisterDisplayNameChange: (String) -> Unit,
    onRegisterConfirmPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onShowLogin: () -> Unit,
    onShowRegister: () -> Unit,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenChangePassword: () -> Unit,
    onOpenPatients: () -> Unit,
    onOpenNewPatient: () -> Unit,
    onOpenEditPatient: () -> Unit,
    onOpenPatientDetail: (PatientDto) -> Unit,
    onOpenNewCase: () -> Unit,
    onOpenCaseDetail: (CaseDto) -> Unit,
    onOpenEditCase: () -> Unit,
    onArchiveCase: () -> Unit,
    onOpenTodayFollowUps: () -> Unit,
    onOpenOverdueFollowUps: () -> Unit,
    onOpenCaseFollowUps: () -> Unit,
    onOpenFollowUpPlanCase: (FollowUpPlanDto) -> Unit,
    onOpenNewFollowUpPlan: () -> Unit,
    onOpenNewFollowUpRecord: (FollowUpPlanDto) -> Unit,
    onOpenEditFollowUpRecord: (com.clinicalcase.app.data.api.FollowUpRecordDto) -> Unit,
    onPatientKeywordChange: (String) -> Unit,
    onSearchPatients: () -> Unit,
    onPreviousPatientPage: () -> Unit,
    onNextPatientPage: () -> Unit,
    onPatientFormChange: ((PatientFormState) -> PatientFormState) -> Unit,
    onCreatePatient: () -> Unit,
    onUpdatePatient: () -> Unit,
    onDeletePatient: (PatientDto) -> Unit,
    onCaseFormChange: ((CaseFormState) -> CaseFormState) -> Unit,
    onCreateCase: () -> Unit,
    onUpdateCase: () -> Unit,
    onDeleteCase: (CaseDto) -> Unit,
    onFollowUpPlanFormChange: ((FollowUpPlanFormState) -> FollowUpPlanFormState) -> Unit,
    onCreateFollowUpPlan: () -> Unit,
    onDeleteFollowUpPlan: (FollowUpPlanDto) -> Unit,
    onFollowUpRecordFormChange: ((FollowUpRecordFormState) -> FollowUpRecordFormState) -> Unit,
    onCreateFollowUpRecord: () -> Unit,
    onUpdateFollowUpRecord: () -> Unit,
    onDeleteFollowUpRecord: (com.clinicalcase.app.data.api.FollowUpRecordDto) -> Unit,
    onChangePasswordFormChange: ((ChangePasswordFormState) -> ChangePasswordFormState) -> Unit,
    onChangePassword: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        when {
            state.loadingSession -> LoadingScreen()
            state.session == null -> LoginScreen(
                state = state,
                onUsernameChange = onUsernameChange,
                onPasswordChange = onPasswordChange,
                onRegisterDisplayNameChange = onRegisterDisplayNameChange,
                onRegisterConfirmPasswordChange = onRegisterConfirmPasswordChange,
                onLogin = onLogin,
                onRegister = onRegister,
                onShowLogin = onShowLogin,
                onShowRegister = onShowRegister,
            )
            state.screen == AppScreen.Dashboard -> DashboardScreen(
                state = state,
                onLogout = onLogout,
                onRefresh = onRefresh,
                onOpenChangePassword = onOpenChangePassword,
                onOpenPatients = onOpenPatients,
                onOpenTodayFollowUps = onOpenTodayFollowUps,
                onOpenOverdueFollowUps = onOpenOverdueFollowUps,
            )
            state.screen == AppScreen.Patients -> PatientListScreen(
                state = state,
                onBack = onOpenDashboard,
                onOpenNewPatient = onOpenNewPatient,
                onOpenPatientDetail = onOpenPatientDetail,
                onKeywordChange = onPatientKeywordChange,
                onSearch = onSearchPatients,
                onPreviousPage = onPreviousPatientPage,
                onNextPage = onNextPatientPage,
                onDeletePatient = onDeletePatient,
            )
            state.screen == AppScreen.NewPatient -> NewPatientScreen(
                state = state,
                onBack = onOpenPatients,
                onFormChange = onPatientFormChange,
                onCreate = onCreatePatient,
            )
            state.screen == AppScreen.EditPatient -> NewPatientScreen(
                state = state,
                title = "编辑患者",
                submitText = "保存修改",
                onBack = { state.selectedPatient?.let(onOpenPatientDetail) ?: onOpenPatients() },
                onFormChange = onPatientFormChange,
                onCreate = onUpdatePatient,
            )
            state.screen == AppScreen.PatientDetail -> PatientDetailScreen(
                state = state,
                onBack = onOpenPatients,
                onEdit = onOpenEditPatient,
                onOpenNewCase = onOpenNewCase,
                onOpenCaseDetail = onOpenCaseDetail,
                onDeleteCase = onDeleteCase,
            )
            state.screen == AppScreen.NewCase -> NewCaseScreen(
                state = state,
                onBack = { state.selectedPatient?.let(onOpenPatientDetail) ?: onOpenPatients() },
                onFormChange = onCaseFormChange,
                onCreate = onCreateCase,
            )
            state.screen == AppScreen.CaseDetail -> CaseDetailScreen(
                state = state,
                onBack = { state.selectedPatient?.let(onOpenPatientDetail) ?: onOpenPatients() },
                onEdit = onOpenEditCase,
                onArchive = onArchiveCase,
                onOpenCaseFollowUps = onOpenCaseFollowUps,
                onOpenNewFollowUpPlan = onOpenNewFollowUpPlan,
                onOpenNewFollowUpRecord = onOpenNewFollowUpRecord,
                onOpenEditFollowUpRecord = onOpenEditFollowUpRecord,
                onDeleteFollowUpPlan = onDeleteFollowUpPlan,
                onDeleteFollowUpRecord = onDeleteFollowUpRecord,
            )
            state.screen == AppScreen.EditCase -> CaseFormScreen(
                state = state,
                title = "编辑病例",
                onBack = { state.selectedCase?.let(onOpenCaseDetail) ?: onOpenPatients() },
                onFormChange = onCaseFormChange,
                onSubmit = onUpdateCase,
                submitText = "保存修改",
            )
            state.screen == AppScreen.FollowUps -> FollowUpPlanListScreen(
                state = state,
                onBack = {
                    state.selectedCase?.let(onOpenCaseDetail)
                        ?: onOpenDashboard()
                },
                onOpenNewFollowUpRecord = onOpenNewFollowUpRecord,
                onOpenFollowUpPlanCase = onOpenFollowUpPlanCase,
                onOpenEditFollowUpRecord = onOpenEditFollowUpRecord,
                onDeleteFollowUpPlan = onDeleteFollowUpPlan,
                onDeleteFollowUpRecord = onDeleteFollowUpRecord,
            )
            state.screen == AppScreen.NewFollowUpPlan -> NewFollowUpPlanScreen(
                state = state,
                onBack = { state.selectedCase?.let(onOpenCaseDetail) ?: onOpenDashboard() },
                onFormChange = onFollowUpPlanFormChange,
                onCreate = onCreateFollowUpPlan,
            )
            state.screen == AppScreen.NewFollowUpRecord -> NewFollowUpRecordScreen(
                state = state,
                onBack = {
                    state.selectedCase?.let(onOpenCaseDetail)
                        ?: onOpenDashboard()
                },
                onFormChange = onFollowUpRecordFormChange,
                onCreate = onCreateFollowUpRecord,
            )
            state.screen == AppScreen.EditFollowUpRecord -> NewFollowUpRecordScreen(
                state = state,
                title = if (state.selectedCase?.caseStatus == "archived" && state.session?.role != "admin") {
                    "查看随访记录"
                } else {
                    "修改随访记录"
                },
                submitText = "保存修改",
                readOnly = state.selectedCase?.caseStatus == "archived" && state.session?.role != "admin",
                onBack = {
                    state.selectedCase?.let(onOpenCaseDetail)
                        ?: onOpenDashboard()
                },
                onFormChange = onFollowUpRecordFormChange,
                onCreate = onUpdateFollowUpRecord,
            )
            state.screen == AppScreen.ChangePassword -> ChangePasswordScreen(
                state = state,
                onBack = onOpenDashboard,
                onFormChange = onChangePasswordFormChange,
                onSubmit = onChangePassword,
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun LoginScreen(
    state: ClinicalCaseUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegisterDisplayNameChange: (String) -> Unit,
    onRegisterConfirmPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onShowLogin: () -> Unit,
    onShowRegister: () -> Unit,
) {
    val isRegister = state.authMode == "register"
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("乳腺外科病例库", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "临床病例与科研随访",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(32.dp))
        if (isRegister) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.registerDisplayName,
                onValueChange = onRegisterDisplayNameChange,
                label = { Text("姓名") },
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.username,
            onValueChange = onUsernameChange,
            label = { Text("账号") },
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.password,
            onValueChange = onPasswordChange,
            label = { Text("密码") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
        )
        if (isRegister) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.registerConfirmPassword,
                onValueChange = onRegisterConfirmPasswordChange,
                label = { Text("确认密码") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )
        }
        ErrorText(state.errorMessage)
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.loading,
            onClick = if (isRegister) onRegister else onLogin,
        ) {
            Text(
                when {
                    state.loading && isRegister -> "注册中..."
                    state.loading -> "登录中..."
                    isRegister -> "注册并登录"
                    else -> "登录"
                },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = if (isRegister) onShowLogin else onShowRegister,
        ) {
            Text(if (isRegister) "已有账号，返回登录" else "注册新账号")
        }
    }
}

@Composable
private fun ChangePasswordScreen(
    state: ClinicalCaseUiState,
    onBack: () -> Unit,
    onFormChange: ((ChangePasswordFormState) -> ChangePasswordFormState) -> Unit,
    onSubmit: () -> Unit,
) {
    val form = state.changePasswordForm
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("修改密码", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = state.session?.displayName.orEmpty(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = onBack) {
                        Text("返回")
                    }
                }
            }
            item {
                PasswordTextField(
                    value = form.currentPassword,
                    onValueChange = { value ->
                        onFormChange { it.copy(currentPassword = value) }
                    },
                    label = "旧密码",
                )
            }
            item {
                PasswordTextField(
                    value = form.newPassword,
                    onValueChange = { value ->
                        onFormChange { it.copy(newPassword = value) }
                    },
                    label = "新密码",
                )
            }
            item {
                PasswordTextField(
                    value = form.confirmPassword,
                    onValueChange = { value ->
                        onFormChange { it.copy(confirmPassword = value) }
                    },
                    label = "确认新密码",
                )
            }
            item { ErrorText(state.errorMessage) }
            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.loading,
                    onClick = onSubmit,
                ) {
                    Text(if (state.loading) "保存中..." else "保存新密码")
                }
            }
        }
    }
}

@Composable
private fun DashboardScreen(
    state: ClinicalCaseUiState,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onOpenChangePassword: () -> Unit,
    onOpenPatients: () -> Unit,
    onOpenTodayFollowUps: () -> Unit,
    onOpenOverdueFollowUps: () -> Unit,
) {
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("工作台", style = MaterialTheme.typography.headlineSmall)
                            Text(
                                text = state.session?.displayName.orEmpty(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            TextButton(onClick = onOpenChangePassword) {
                                Text("修改密码")
                            }
                            TextButton(onClick = onLogout) {
                                Text("退出登录")
                            }
                        }
                    }
                    DashboardActionRow(
                        onOpenPatients = onOpenPatients,
                        onRefresh = onRefresh,
                    )
                }
            }

            item { ErrorText(state.errorMessage) }
            item { SuccessText(state.successMessage) }
            item { DashboardCards(summary = state.dashboard) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onOpenTodayFollowUps,
                    ) {
                        Text("今日随访")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onOpenOverdueFollowUps,
                    ) {
                        Text("逾期随访")
                    }
                }
            }
            item { Text("最近病例", style = MaterialTheme.typography.titleMedium) }

            val recentCases = state.dashboard?.recentCases.orEmpty().take(3)
            if (recentCases.isEmpty()) {
                item {
                    Text(
                        text = "暂无病例",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(recentCases) { item ->
                    RecentCaseCard(item)
                }
            }
        }
    }
}

@Composable
private fun DashboardActionRow(
    onOpenPatients: () -> Unit,
    onRefresh: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            modifier = Modifier.weight(1f),
            onClick = onOpenPatients,
        ) {
            Text("患者")
        }
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onRefresh,
        ) {
            Text("刷新")
        }
    }
}

@Composable
private fun PatientListScreen(
    state: ClinicalCaseUiState,
    onBack: () -> Unit,
    onOpenNewPatient: () -> Unit,
    onOpenPatientDetail: (PatientDto) -> Unit,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onDeletePatient: (PatientDto) -> Unit,
) {
    val isAdmin = state.session?.role == "admin"
    val totalPages = ((state.patientTotal + state.patientPageSize - 1) / state.patientPageSize).coerceAtLeast(1)
    var patientToDelete by remember { mutableStateOf<PatientDto?>(null) }
    patientToDelete?.let { patient ->
        AlertDialog(
            onDismissRequest = { patientToDelete = null },
            title = { Text("确认删除患者") },
            text = { Text("删除后该患者将从当前病例库中移除。确定删除“${patient.name}”吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        patientToDelete = null
                        onDeletePatient(patient)
                    },
                ) {
                    Text("确认删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { patientToDelete = null }) {
                    Text("取消")
                }
            },
        )
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("患者列表", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = "共 ${state.patientTotal} 条",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onBack) {
                            Text("返回")
                        }
                        Button(onClick = onOpenNewPatient) {
                            Text("新建")
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = state.patientKeyword,
                        onValueChange = onKeywordChange,
                        label = { Text("姓名/住院号/门诊号") },
                        singleLine = true,
                    )
                    Button(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        onClick = onSearch,
                    ) {
                        Text("搜索")
                    }
                }
            }

            item { ErrorText(state.errorMessage) }

            if (state.patients.isEmpty()) {
                item {
                    Text(
                        text = if (state.loading) "加载中..." else "暂无患者",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(state.patients) { patient ->
                    PatientCard(
                        patient = patient,
                        isAdmin = isAdmin,
                        onClick = { onOpenPatientDetail(patient) },
                        onDelete = { patientToDelete = patient },
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "第 ${state.patientPage} / $totalPages 页",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            enabled = state.patientPage > 1 && !state.loading,
                            onClick = onPreviousPage,
                        ) {
                            Text("上一页")
                        }
                        OutlinedButton(
                            enabled = state.patientPage < totalPages && !state.loading,
                            onClick = onNextPage,
                        ) {
                            Text("下一页")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PatientCard(
    patient: PatientDto,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = patient.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (isAdmin) {
                    TextButton(onClick = onDelete) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = listOfNotNull(
                    patient.gender,
                    patient.inpatientNo?.let { "住院号 $it" },
                    patient.outpatientNo?.let { "门诊号 $it" },
                ).joinToString(" · "),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "初诊：${patient.firstVisitDate.take(10)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PatientDetailScreen(
    state: ClinicalCaseUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onOpenNewCase: () -> Unit,
    onOpenCaseDetail: (CaseDto) -> Unit,
    onDeleteCase: (CaseDto) -> Unit,
) {
    val patient = state.selectedPatient
    val isAdmin = state.session?.role == "admin"
    var caseToDelete by remember { mutableStateOf<CaseDto?>(null) }
    caseToDelete?.let { caseItem ->
        AlertDialog(
            onDismissRequest = { caseToDelete = null },
            title = { Text("确认删除病例") },
            text = { Text("删除后该病例将从患者病例列表中移除。确定删除“${caseItem.title}”吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        caseToDelete = null
                        onDeleteCase(caseItem)
                    },
                ) {
                    Text("确认删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { caseToDelete = null }) {
                    Text("取消")
                }
            },
        )
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(patient?.name ?: "患者详情", style = MaterialTheme.typography.headlineSmall)
                        patient?.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                            Text(
                                text = "电话 $phone",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    TextButton(onClick = onBack) {
                        Text("返回")
                    }
                }
            }

            if (patient != null) {
                item {
                    PatientInfoCard(
                        patient = patient,
                        onEdit = onEdit,
                    )
                }
            }

            item { ErrorText(state.errorMessage) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("病例列表", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = onOpenNewCase, enabled = patient != null) {
                        Text("新建病例")
                    }
                }
            }

            if (state.patientCases.isEmpty()) {
                item {
                    Text(
                        text = if (state.loading) "加载中..." else "暂无病例",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(state.patientCases) { caseItem ->
                    CaseCard(
                        caseItem = caseItem,
                        isAdmin = isAdmin,
                        onClick = { onOpenCaseDetail(caseItem) },
                        onDelete = { caseToDelete = caseItem },
                    )
                }
            }
        }
    }
}

@Composable
private fun PatientInfoCard(patient: PatientDto, onEdit: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("患者信息", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = onEdit) {
                    Text("编辑")
                }
            }
            InfoLine("性别", patient.gender)
            InfoLine("出生日期", patient.birthDate?.take(10).orEmpty())
            InfoLine("初诊日期", patient.firstVisitDate.take(10))
            InfoLine("门诊号", patient.outpatientNo.orEmpty())
            InfoLine("住院号", patient.inpatientNo.orEmpty())
            InfoLine("备注", patient.remark.orEmpty())
        }
    }
}

@Composable
private fun CaseCard(
    caseItem: CaseDto,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val isArchived = caseItem.caseStatus == "archived"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = caseItem.title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isAdmin) {
                        TextButton(onClick = onDelete) {
                            Text("删除", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            if (isArchived) {
                Text("已归档 · 锁定", color = MaterialTheme.colorScheme.error)
            }
            Text(
                text = listOf(caseItem.diseaseType, caseItem.currentStatus)
                    .joinToString(" · "),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            InfoLine("侧别", caseItem.laterality)
            InfoLine("确诊", caseItem.confirmedDiagnosis.orEmpty())
            InfoLine("病理", caseItem.pathologyType.orEmpty())
            InfoLine("分子分型", caseItem.molecularSubtype.orEmpty())
            InfoLine("手术日期", caseItem.surgeryDate?.take(10).orEmpty())
        }
    }
}

@Composable
private fun CaseDetailScreen(
    state: ClinicalCaseUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onOpenCaseFollowUps: () -> Unit,
    onOpenNewFollowUpPlan: () -> Unit,
    onOpenNewFollowUpRecord: (FollowUpPlanDto) -> Unit,
    onOpenEditFollowUpRecord: (com.clinicalcase.app.data.api.FollowUpRecordDto) -> Unit,
    onDeleteFollowUpPlan: (FollowUpPlanDto) -> Unit,
    onDeleteFollowUpRecord: (com.clinicalcase.app.data.api.FollowUpRecordDto) -> Unit,
) {
    val caseItem = state.selectedCase
    val isArchived = caseItem?.caseStatus == "archived"
    val isAdmin = state.session?.role == "admin"
    val isLocked = isArchived && !isAdmin
    var showArchiveDialog by remember { mutableStateOf(false) }
    var planToDelete by remember { mutableStateOf<FollowUpPlanDto?>(null) }
    var recordToDelete by remember { mutableStateOf<com.clinicalcase.app.data.api.FollowUpRecordDto?>(null) }

    if (showArchiveDialog && caseItem != null) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            title = { Text("确认归档病例？") },
            text = {
                Text("归档后病例会进入锁定状态，普通用户不能再修改，只有管理员可以修改。病例仍会显示在当前列表中，并标记为已归档。")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showArchiveDialog = false
                        onArchive()
                    },
                ) {
                    Text("确认归档")
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) {
                    Text("取消")
                }
            },
        )
    }
    planToDelete?.let { plan ->
        AlertDialog(
            onDismissRequest = { planToDelete = null },
            title = { Text("确认删除随访计划") },
            text = { Text("确定删除 ${plan.plannedDate.take(10)} 的随访计划吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        planToDelete = null
                        onDeleteFollowUpPlan(plan)
                    },
                ) {
                    Text("确认删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { planToDelete = null }) {
                    Text("取消")
                }
            },
        )
    }
    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("确认删除随访记录") },
            text = { Text("确定删除 ${record.actualDate.take(10)} 的随访记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        recordToDelete = null
                        onDeleteFollowUpRecord(record)
                    },
                ) {
                    Text("确认删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("取消")
                }
            },
        )
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(caseItem?.title ?: "病例详情", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = state.selectedPatient?.name.orEmpty(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onBack) {
                            Text("返回")
                        }
                        Button(
                            onClick = onEdit,
                            enabled = caseItem != null && (!isArchived || isAdmin),
                        ) {
                            Text("编辑")
                        }
                        OutlinedButton(
                            onClick = { showArchiveDialog = true },
                            enabled = caseItem != null && caseItem.caseStatus != "archived" && !state.loading,
                        ) {
                            Text("归档")
                        }
                    }
                }
            }

            item { ErrorText(state.errorMessage) }

            if (caseItem == null) {
                item {
                    Text(
                        text = if (state.loading) "加载中..." else "暂无病例详情",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("基础信息", style = MaterialTheme.typography.titleMedium)
                            InfoLine("就诊类型", caseItem.visitType)
                            InfoLine("侧别", caseItem.laterality)
                            InfoLine("疾病类型", caseItem.diseaseType)
                            InfoLine("当前状态", caseItem.currentStatus)
                            InfoLine("病例状态", caseItem.caseStatus)
                            if (caseItem.caseStatus == "archived") {
                                InfoLine("锁定", "已归档病例，仅管理员可修改")
                            }
                        }
                    }
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("诊断与病理", style = MaterialTheme.typography.titleMedium)
                            InfoLine("初步诊断", caseItem.preliminaryDiagnosis.orEmpty())
                            InfoLine("确诊诊断", caseItem.confirmedDiagnosis.orEmpty())
                            InfoLine("病理类型", caseItem.pathologyType.orEmpty())
                            InfoLine("ER", caseItem.erStatus.orEmpty())
                            InfoLine("PR", caseItem.prStatus.orEmpty())
                            InfoLine("HER2", caseItem.her2Status.orEmpty())
                            InfoLine("Ki-67", caseItem.ki67Percent?.toString().orEmpty())
                            InfoLine("分子分型", caseItem.molecularSubtype.orEmpty())
                        }
                    }
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("分期与治疗", style = MaterialTheme.typography.titleMedium)
                            InfoLine("临床 T", caseItem.clinicalTStage.orEmpty())
                            InfoLine("临床 N", caseItem.clinicalNStage.orEmpty())
                            InfoLine("临床 M", caseItem.clinicalMStage.orEmpty())
                            InfoLine("临床分期", caseItem.clinicalStage.orEmpty())
                            InfoLine("手术日期", caseItem.surgeryDate?.take(10).orEmpty())
                            InfoLine("手术方式", caseItem.surgeryType.orEmpty())
                        }
                    }
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("摘要", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = caseItem.summary.orEmpty().ifBlank { "暂无摘要" },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("近期随访计划（点击填写记录）", style = MaterialTheme.typography.titleMedium)
                        Button(
                            onClick = onOpenNewFollowUpPlan,
                            enabled = !isLocked,
                        ) {
                            Text("新建随访计划")
                        }
                    }
                }
                if (state.followUpPlans.isEmpty()) {
                    item {
                        Text(
                            text = if (state.loading) "加载中..." else "暂无随访计划",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(state.followUpPlans.take(5)) { plan ->
                        FollowUpPlanCard(
                            plan = plan,
                            enabled = !isLocked,
                            onClick = { onOpenNewFollowUpRecord(plan) },
                            onDelete = { planToDelete = plan },
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("最近随访记录", style = MaterialTheme.typography.titleMedium)
                        OutlinedButton(onClick = onOpenCaseFollowUps) {
                            Text("全部随访")
                        }
                    }
                }
                if (state.followUpRecords.isEmpty()) {
                    item {
                        Text(
                            text = "暂无随访记录",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(state.followUpRecords.take(1)) { record ->
                        FollowUpRecordCard(
                            record = record,
                            enabled = true,
                            isAdmin = isAdmin,
                            readOnly = isLocked,
                            onClick = { onOpenEditFollowUpRecord(record) },
                            onDelete = { recordToDelete = record },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FollowUpPlanListScreen(
    state: ClinicalCaseUiState,
    onBack: () -> Unit,
    onOpenNewFollowUpRecord: (FollowUpPlanDto) -> Unit,
    onOpenFollowUpPlanCase: (FollowUpPlanDto) -> Unit,
    onOpenEditFollowUpRecord: (com.clinicalcase.app.data.api.FollowUpRecordDto) -> Unit,
    onDeleteFollowUpPlan: (FollowUpPlanDto) -> Unit,
    onDeleteFollowUpRecord: (com.clinicalcase.app.data.api.FollowUpRecordDto) -> Unit,
) {
    val isRecordMode = state.followUpListMode == "case_records"
    val isLocked = state.selectedCase?.caseStatus == "archived" && state.session?.role != "admin"
    val isAdmin = state.session?.role == "admin"
    var planToDelete by remember { mutableStateOf<FollowUpPlanDto?>(null) }
    var recordToDelete by remember { mutableStateOf<com.clinicalcase.app.data.api.FollowUpRecordDto?>(null) }
    planToDelete?.let { plan ->
        AlertDialog(
            onDismissRequest = { planToDelete = null },
            title = { Text("确认删除随访计划") },
            text = { Text("确定删除 ${plan.plannedDate.take(10)} 的随访计划吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        planToDelete = null
                        onDeleteFollowUpPlan(plan)
                    },
                ) {
                    Text("确认删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { planToDelete = null }) {
                    Text("取消")
                }
            },
        )
    }
    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("确认删除随访记录") },
            text = { Text("确定删除 ${record.actualDate.take(10)} 的随访记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        recordToDelete = null
                        onDeleteFollowUpRecord(record)
                    },
                ) {
                    Text("确认删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("取消")
                }
            },
        )
    }
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(state.followUpTitle, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = "共 ${if (isRecordMode) state.followUpRecords.size else state.followUpPlans.size} 条",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = onBack) {
                        Text("返回")
                    }
                }
            }
            item { ErrorText(state.errorMessage) }
            if (isRecordMode) {
                if (state.followUpRecords.isEmpty()) {
                    item {
                        Text(
                            text = if (state.loading) "加载中..." else "暂无随访记录",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(state.followUpRecords) { record ->
                        FollowUpRecordCard(
                            record = record,
                            enabled = true,
                            isAdmin = isAdmin,
                            readOnly = isLocked,
                            onClick = { onOpenEditFollowUpRecord(record) },
                            onDelete = { recordToDelete = record },
                        )
                    }
                }
            } else if (state.followUpPlans.isEmpty()) {
                item {
                    Text(
                        text = if (state.loading) "加载中..." else "暂无随访计划",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(state.followUpPlans) { plan ->
                    FollowUpPlanCard(
                        plan = plan,
                        enabled = !isLocked,
                        showPatientName = true,
                        actionText = "点击查看患者病例",
                        onClick = { onOpenFollowUpPlanCase(plan) },
                        onDelete = { planToDelete = plan },
                    )
                }
            }
        }
    }
}

@Composable
private fun NewFollowUpRecordScreen(
    state: ClinicalCaseUiState,
    title: String = "填写随访记录",
    submitText: String = "保存随访记录",
    readOnly: Boolean = false,
    onBack: () -> Unit,
    onFormChange: ((FollowUpRecordFormState) -> FollowUpRecordFormState) -> Unit,
    onCreate: () -> Unit,
) {
    val form = state.followUpRecordForm
    val plan = state.selectedFollowUpPlan
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = listOfNotNull(plan?.followUpType, plan?.plannedDate?.take(10))
                                .joinToString(" · "),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = onBack) {
                        Text("返回")
                    }
                }
            }
            item { DateField(form.actualDate, { onFormChange { state -> state.copy(actualDate = it) } }, "实际随访日期 *", readOnly = readOnly) }
            item {
                ChoiceButtons(
                    label = "随访方式",
                    value = form.method,
                    enabled = !readOnly,
                    options = listOf(
                        "phone" to "电话",
                        "wechat" to "微信",
                        "outpatient" to "门诊",
                        "inpatient_review" to "住院复查",
                        "other" to "其他",
                    ),
                    onValueChange = { value -> onFormChange { state -> state.copy(method = value) } },
                )
            }
            item {
                ChoiceButtons(
                    label = "生存状态",
                    value = form.survivalStatus,
                    enabled = !readOnly,
                    options = listOf(
                        "disease_free" to "无病生存",
                        "alive_with_disease" to "带病生存",
                        "recurrence" to "复发",
                        "metastasis" to "转移",
                        "deceased" to "死亡",
                        "lost" to "失访",
                        "unknown" to "未知",
                    ),
                    onValueChange = { value -> onFormChange { state -> state.copy(survivalStatus = value) } },
                )
            }
            item {
                ChoiceButtons(
                    label = "是否复发",
                    value = form.hasRecurrence,
                    enabled = !readOnly,
                    options = listOf("false" to "否", "true" to "是", "unknown" to "未知"),
                    onValueChange = { value -> onFormChange { state -> state.copy(hasRecurrence = value) } },
                )
            }
            item { FormTextField(form.recurrenceSite, { onFormChange { state -> state.copy(recurrenceSite = it) } }, "复发部位", readOnly = readOnly) }
            item {
                ChoiceButtons(
                    label = "是否转移",
                    value = form.hasMetastasis,
                    enabled = !readOnly,
                    options = listOf("false" to "否", "true" to "是", "unknown" to "未知"),
                    onValueChange = { value -> onFormChange { state -> state.copy(hasMetastasis = value) } },
                )
            }
            item { FormTextField(form.metastasisSite, { onFormChange { state -> state.copy(metastasisSite = it) } }, "转移部位", readOnly = readOnly) }
            item { FormTextField(form.imagingSummary, { onFormChange { state -> state.copy(imagingSummary = it) } }, "影像摘要", minLines = 3, readOnly = readOnly) }
            item { FormTextField(form.tumorMarkerSummary, { onFormChange { state -> state.copy(tumorMarkerSummary = it) } }, "肿瘤标志物摘要", minLines = 3, readOnly = readOnly) }
            item { FormTextField(form.currentTreatment, { onFormChange { state -> state.copy(currentTreatment = it) } }, "当前治疗", readOnly = readOnly) }
            item { FormTextField(form.adverseReactions, { onFormChange { state -> state.copy(adverseReactions = it) } }, "不良反应", readOnly = readOnly) }
            item { DateField(form.nextFollowUpDate, { onFormChange { state -> state.copy(nextFollowUpDate = it) } }, "下次随访日期，可空", allowClear = true, readOnly = readOnly) }
            item { FormTextField(form.remark, { onFormChange { state -> state.copy(remark = it) } }, "备注", minLines = 3, readOnly = readOnly) }
            item { ErrorText(state.errorMessage) }
            if (!readOnly) item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.loading && (plan != null || state.selectedFollowUpRecord != null),
                    onClick = onCreate,
                ) {
                    Text(if (state.loading) "保存中..." else submitText)
                }
            }
        }
    }
}

@Composable
private fun NewFollowUpPlanScreen(
    state: ClinicalCaseUiState,
    onBack: () -> Unit,
    onFormChange: ((FollowUpPlanFormState) -> FollowUpPlanFormState) -> Unit,
    onCreate: () -> Unit,
) {
    val form = state.followUpPlanForm
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("新建随访计划", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = state.selectedCase?.title.orEmpty(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = onBack) {
                        Text("返回")
                    }
                }
            }
            item {
                FormTextField(
                    value = form.followUpType,
                    onValueChange = { onFormChange { state -> state.copy(followUpType = it) } },
                    label = "随访类型 postoperative/chemotherapy/endocrine/imaging_review/routine/other",
                )
            }
            item {
                DateField(
                    value = form.plannedDate,
                    onValueChange = { onFormChange { state -> state.copy(plannedDate = it) } },
                    label = "计划日期 *",
                )
            }
            item { ErrorText(state.errorMessage) }
            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.loading,
                    onClick = onCreate,
                ) {
                    Text(if (state.loading) "保存中..." else "保存随访计划")
                }
            }
        }
    }
}

@Composable
private fun FollowUpPlanCard(
    plan: FollowUpPlanDto,
    enabled: Boolean = true,
    showPatientName: Boolean = false,
    actionText: String = "点击填写随访记录",
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (showPatientName) {
                Text(
                    text = plan.clinicalCase?.patient?.name ?: "未知患者",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text("计划随访时间：${plan.plannedDate.take(10)}", style = MaterialTheme.typography.titleMedium)
            Text(
                text = if (enabled) actionText else "已锁定，无法填写随访记录",
                color = MaterialTheme.colorScheme.primary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDelete) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun FollowUpRecordCard(
    record: com.clinicalcase.app.data.api.FollowUpRecordDto,
    enabled: Boolean = true,
    isAdmin: Boolean = false,
    readOnly: Boolean = false,
    onClick: () -> Unit,
    onDelete: () -> Unit = {},
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("随访时间：${record.actualDate.take(10)}", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "摘要：${record.displaySummary()}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (readOnly) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    OutlinedButton(onClick = onClick) {
                        Text("查看")
                    }
                }
            } else {
                Text(
                    text = if (enabled) "点击修改随访记录" else "已锁定，无法修改随访记录",
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (isAdmin) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDelete) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

private fun com.clinicalcase.app.data.api.FollowUpRecordDto.displaySummary(): String =
    summary?.takeIf { it.isNotBlank() }
        ?: listOfNotNull(
            currentTreatment?.takeIf { it.isNotBlank() }?.let { "当前治疗：$it" },
            imagingSummary?.takeIf { it.isNotBlank() }?.let { "影像：$it" },
            tumorMarkerSummary?.takeIf { it.isNotBlank() }?.let { "肿瘤标志物：$it" },
            adverseReactions?.takeIf { it.isNotBlank() }?.let { "不良反应：$it" },
            remark?.takeIf { it.isNotBlank() },
        ).joinToString("；").ifBlank { "待生成 AI 摘要" }

@Composable
private fun NewPatientScreen(
    state: ClinicalCaseUiState,
    title: String = "新建患者",
    submitText: String = "保存患者",
    onBack: () -> Unit,
    onFormChange: ((PatientFormState) -> PatientFormState) -> Unit,
    onCreate: () -> Unit,
) {
    val form = state.patientForm
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(title, style = MaterialTheme.typography.headlineSmall)
                    TextButton(onClick = onBack) {
                        Text("返回")
                    }
                }
            }
            item { FormTextField(form.name, { value -> onFormChange { state -> state.copy(name = value) } }, "姓名 *") }
            item {
                ChoiceButtons(
                    label = "性别",
                    value = form.gender,
                    options = listOf(
                        "female" to "女",
                        "male" to "男",
                        "other" to "其他",
                        "unknown" to "未知",
                    ),
                    onValueChange = { value -> onFormChange { state -> state.copy(gender = value) } },
                )
            }
            item { DateField(form.birthDate, { onFormChange { state -> state.copy(birthDate = it) } }, "出生日期，可空", allowClear = true) }
            item { FormTextField(form.phone, { onFormChange { state -> state.copy(phone = it) } }, "手机号") }
            item { FormTextField(form.outpatientNo, { onFormChange { state -> state.copy(outpatientNo = it) } }, "门诊号") }
            item { FormTextField(form.inpatientNo, { onFormChange { state -> state.copy(inpatientNo = it) } }, "住院号") }
            item { DateField(form.firstVisitDate, { onFormChange { state -> state.copy(firstVisitDate = it) } }, "初诊日期 *") }
            item { FormTextField(form.remark, { onFormChange { state -> state.copy(remark = it) } }, "备注", minLines = 3) }
            item { ErrorText(state.errorMessage) }
            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.loading,
                    onClick = onCreate,
                ) {
                    Text(if (state.loading) "保存中..." else submitText)
                }
            }
        }
    }
}

@Composable
private fun NewCaseScreen(
    state: ClinicalCaseUiState,
    onBack: () -> Unit,
    onFormChange: ((CaseFormState) -> CaseFormState) -> Unit,
    onCreate: () -> Unit,
) {
    val form = state.caseForm
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("新建病例", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = state.selectedPatient?.name.orEmpty(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = onBack) {
                        Text("返回")
                    }
                }
            }
            item { FormTextField(form.title, { onFormChange { state -> state.copy(title = it) } }, "病例标题 *") }
            item {
                CaseChoiceButtons(
                    form = form,
                    onFormChange = onFormChange,
                    includePostoperativeReview = false,
                )
            }
            item { FormTextField(form.confirmedDiagnosis, { onFormChange { state -> state.copy(confirmedDiagnosis = it) } }, "确诊诊断") }
            item { FormTextField(form.pathologyType, { onFormChange { state -> state.copy(pathologyType = it) } }, "病理类型") }
            item { ReceptorChoiceButtons(form = form, onFormChange = onFormChange) }
            item {
                ExternalLabelTextField(
                    value = form.ki67Percent,
                    onValueChange = { onFormChange { state -> state.copy(ki67Percent = it) } },
                    label = "Ki-67 百分比",
                    placeholder = "例如 30",
                )
            }
            item {
                ChoiceButtons(
                    label = "分子分型",
                    value = form.molecularSubtype,
                    options = molecularSubtypeOptions,
                    onValueChange = { value -> onFormChange { state -> state.copy(molecularSubtype = value) } },
                )
            }
            item { DateField(form.surgeryDate, { onFormChange { state -> state.copy(surgeryDate = it) } }, "手术日期，可空", allowClear = true) }
            item { FormTextField(form.surgeryType, { onFormChange { state -> state.copy(surgeryType = it) } }, "手术方式") }
            item {
                ChoiceButtons(
                    label = "当前状态",
                    value = form.currentStatus,
                    options = currentStatusOptions,
                    onValueChange = { value -> onFormChange { state -> state.copy(currentStatus = value) } },
                )
            }
            item { FormTextField(form.summary, { onFormChange { state -> state.copy(summary = it) } }, "病例摘要", minLines = 3) }
            item { ErrorText(state.errorMessage) }
            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.loading,
                    onClick = onCreate,
                ) {
                    Text(if (state.loading) "保存中..." else "保存病例")
                }
            }
        }
    }
}

@Composable
private fun CaseFormScreen(
    state: ClinicalCaseUiState,
    title: String,
    onBack: () -> Unit,
    onFormChange: ((CaseFormState) -> CaseFormState) -> Unit,
    onSubmit: () -> Unit,
    submitText: String,
) {
    val form = state.caseForm
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(title, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = state.selectedPatient?.name.orEmpty(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = onBack) {
                        Text("返回")
                    }
                }
            }
            item { FormTextField(form.title, { onFormChange { state -> state.copy(title = it) } }, "病例标题 *") }
            item {
                CaseChoiceButtons(
                    form = form,
                    onFormChange = onFormChange,
                    includePostoperativeReview = true,
                )
            }
            item { FormTextField(form.confirmedDiagnosis, { onFormChange { state -> state.copy(confirmedDiagnosis = it) } }, "确诊诊断") }
            item { FormTextField(form.pathologyType, { onFormChange { state -> state.copy(pathologyType = it) } }, "病理类型") }
            item { ReceptorChoiceButtons(form = form, onFormChange = onFormChange) }
            item {
                ExternalLabelTextField(
                    value = form.ki67Percent,
                    onValueChange = { onFormChange { state -> state.copy(ki67Percent = it) } },
                    label = "Ki-67 百分比",
                    placeholder = "例如 30",
                )
            }
            item {
                ChoiceButtons(
                    label = "分子分型",
                    value = form.molecularSubtype,
                    options = molecularSubtypeOptions,
                    onValueChange = { value -> onFormChange { state -> state.copy(molecularSubtype = value) } },
                )
            }
            item { DateField(form.surgeryDate, { onFormChange { state -> state.copy(surgeryDate = it) } }, "手术日期，可空", allowClear = true) }
            item { FormTextField(form.surgeryType, { onFormChange { state -> state.copy(surgeryType = it) } }, "手术方式") }
            item {
                ChoiceButtons(
                    label = "当前状态",
                    value = form.currentStatus,
                    options = currentStatusOptions,
                    onValueChange = { value -> onFormChange { state -> state.copy(currentStatus = value) } },
                )
            }
            item { CaseStatusLabel(form.caseStatus) }
            item { FormTextField(form.summary, { onFormChange { state -> state.copy(summary = it) } }, "病例摘要", minLines = 3) }
            item { ErrorText(state.errorMessage) }
            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.loading,
                    onClick = onSubmit,
                ) {
                    Text(if (state.loading) "保存中..." else submitText)
                }
            }
        }
    }
}

@Composable
private fun DashboardCards(summary: DashboardSummary?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("今日待随访", summary?.todayFollowUpCount?.toString() ?: "-", Modifier.weight(1f))
            MetricCard("逾期随访", summary?.overdueFollowUpCount?.toString() ?: "-", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("患者总数", summary?.totalPatientCount?.toString() ?: "-", Modifier.weight(1f))
            MetricCard("病例总数", summary?.totalCaseCount?.toString() ?: "-", Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun RecentCaseCard(item: RecentCaseDto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = listOfNotNull(item.diseaseType, item.currentStatus, item.caseStatus)
                    .joinToString(" · "),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            item.patient?.let { patient ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "患者：${patient.name.orEmpty()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item.uploader?.let { uploader ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "上传者：${uploader.displayName ?: uploader.username.orEmpty()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    if (value.isBlank()) return
    Text(
        text = "$label：$value",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private val visitTypeOptions = listOf(
    "inpatient" to "住院",
    "outpatient" to "门诊",
)

private val lateralityOptions = listOf(
    "left" to "左侧",
    "right" to "右侧",
    "bilateral" to "双侧",
    "unknown" to "未知",
)

private val diseaseTypeOptions = listOf(
    "breast_cancer" to "乳腺癌",
    "benign_tumor" to "良性肿瘤",
    "mastitis" to "乳腺炎",
    "other" to "其他",
)

private val receptorStatusOptions = listOf(
    "positive" to "阳性",
    "negative" to "阴性",
    "unknown" to "未知",
)

private val her2StatusOptions = listOf(
    "zero" to "0",
    "one_plus" to "1+",
    "two_plus" to "2+",
    "three_plus" to "3+",
    "fish_positive" to "FISH+",
    "fish_negative" to "FISH-",
    "unknown" to "未知",
)

private val molecularSubtypeOptions = listOf(
    "luminal_a" to "Luminal A",
    "luminal_b_her2_negative" to "Luminal B / HER2阴性",
    "luminal_b_her2_positive" to "Luminal B / HER2阳性",
    "her2_positive" to "HER2 阳性",
    "triple_negative" to "三阴性",
    "unknown" to "未知",
)

private val currentStatusOptions = listOf(
    "treating" to "治疗中",
    "follow_up" to "随访中",
    "recurrence" to "复发",
    "deceased" to "死亡",
    "lost" to "失访",
)

@Composable
private fun CaseStatusLabel(status: String) {
    val label = when (status) {
        "archived" -> "已归档"
        "draft" -> "草稿"
        else -> status.ifBlank { "草稿" }
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "病例状态",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                text = label,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun CaseChoiceButtons(
    form: CaseFormState,
    onFormChange: ((CaseFormState) -> CaseFormState) -> Unit,
    includePostoperativeReview: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ChoiceButtons(
            label = "就诊类型",
            value = form.visitType,
            options = if (includePostoperativeReview) {
                visitTypeOptions + ("postoperative_review" to "术后复查")
            } else {
                visitTypeOptions
            },
            onValueChange = { value -> onFormChange { state -> state.copy(visitType = value) } },
        )
        ChoiceButtons(
            label = "侧别",
            value = form.laterality,
            options = lateralityOptions,
            onValueChange = { value -> onFormChange { state -> state.copy(laterality = value) } },
        )
        ChoiceButtons(
            label = "疾病类型",
            value = form.diseaseType,
            options = diseaseTypeOptions,
            onValueChange = { value -> onFormChange { state -> state.copy(diseaseType = value) } },
        )
    }
}

@Composable
private fun ReceptorChoiceButtons(
    form: CaseFormState,
    onFormChange: ((CaseFormState) -> CaseFormState) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ChoiceButtons(
            label = "ER 状态",
            value = form.erStatus,
            options = receptorStatusOptions,
            onValueChange = { value -> onFormChange { state -> state.copy(erStatus = value) } },
        )
        ChoiceButtons(
            label = "PR 状态",
            value = form.prStatus,
            options = receptorStatusOptions,
            onValueChange = { value -> onFormChange { state -> state.copy(prStatus = value) } },
        )
        ChoiceButtons(
            label = "HER2 状态",
            value = form.her2Status,
            options = her2StatusOptions,
            onValueChange = { value -> onFormChange { state -> state.copy(her2Status = value) } },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChoiceButtons(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { (optionValue, optionLabel) ->
                if (value == optionValue) {
                    Button(
                        enabled = enabled,
                        onClick = { onValueChange(optionValue) },
                    ) {
                        Text(optionLabel)
                    }
                } else {
                    OutlinedButton(
                        enabled = enabled,
                        onClick = { onValueChange(optionValue) },
                    ) {
                        Text(optionLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    minLines: Int = 1,
    readOnly: Boolean = false,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = minLines == 1,
        minLines = minLines,
        readOnly = readOnly,
    )
}

@Composable
private fun DateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    allowClear: Boolean = false,
    readOnly: Boolean = false,
) {
    val selectedDate = remember(value) { value.toLocalDateOrToday() }
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        ChineseDatePickerDialog(
            initialDate = selectedDate,
            allowClear = allowClear && value.isNotBlank(),
            onDismiss = { showPicker = false },
            onClear = {
                showPicker = false
                onValueChange("")
            },
            onConfirm = { date ->
                showPicker = false
                onValueChange(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
            },
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = {},
                label = { Text(label) },
                singleLine = true,
                readOnly = true,
            )
            if (!readOnly) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showPicker = true },
                )
            }
        }
        if (allowClear && value.isNotBlank() && !readOnly) {
            TextButton(onClick = { onValueChange("") }) {
                Text("清空日期")
            }
        }
    }
}

@Composable
private fun ChineseDatePickerDialog(
    initialDate: LocalDate,
    allowClear: Boolean,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    var year by remember(initialDate) { mutableStateOf(initialDate.year) }
    var month by remember(initialDate) { mutableStateOf(initialDate.monthValue) }
    var day by remember(initialDate) { mutableStateOf(initialDate.dayOfMonth) }

    val currentYear = LocalDate.now().year
    val years = remember(currentYear, initialDate.year) {
        ((currentYear - 100)..(currentYear + 20))
            .toMutableSet()
            .apply { add(initialDate.year) }
            .sorted()
            .reversed()
    }
    val months = remember { (1..12).toList() }
    val maxDay = YearMonth.of(year, month).lengthOfMonth()
    if (day > maxDay) day = maxDay
    val days = remember(year, month) { (1..maxDay).toList() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择日期") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "当前选择：${year}年${month}月${day}日",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DatePartDropdown(
                        modifier = Modifier.weight(1f),
                        value = year,
                        suffix = "年",
                        options = years,
                        onValueChange = { year = it },
                    )
                    DatePartDropdown(
                        modifier = Modifier.weight(1f),
                        value = month,
                        suffix = "月",
                        options = months,
                        onValueChange = { month = it },
                    )
                    DatePartDropdown(
                        modifier = Modifier.weight(1f),
                        value = day,
                        suffix = "日",
                        options = days,
                        onValueChange = { day = it },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalDate.of(year, month, day)) }) {
                Text("确定")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (allowClear) {
                    TextButton(onClick = onClear) {
                        Text("清空日期")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        },
    )
}

@Composable
private fun DatePartDropdown(
    modifier: Modifier = Modifier,
    value: Int,
    suffix: String,
    options: List<Int>,
    onValueChange: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { expanded = true },
        ) {
            Text("$value$suffix")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text("$option$suffix") },
                    onClick = {
                        expanded = false
                        onValueChange(option)
                    },
                )
            }
        }
    }
}

@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
    )
}

private fun String.toLocalDateOrToday(): LocalDate {
    return runCatching {
        LocalDate.parse(take(10), DateTimeFormatter.ISO_LOCAL_DATE)
    }.getOrDefault(LocalDate.now())
}

@Composable
private fun ExternalLabelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
        )
    }
}

@Composable
private fun ErrorText(message: String?) {
    if (message == null) return
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
    )
}

@Composable
private fun SuccessText(message: String?) {
    if (message == null) return
    Text(
        text = message,
        color = MaterialTheme.colorScheme.primary,
    )
}
