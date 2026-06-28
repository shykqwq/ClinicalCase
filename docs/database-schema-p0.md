# 乳腺外科临床病例库与科研随访 App 数据库表结构（P0 版）

版本：v0.1  
日期：2026-06-14  
数据库建议：PostgreSQL  
命名约定：表名使用 snake_case，主键统一使用 UUID，时间字段统一使用 `timestamptz`

## 1. 设计原则

- 所有业务数据按科室隔离，核心表均包含 `department_id`。
- P0 支持真实患者身份信息，因此患者敏感字段需要支持脱敏展示，后续可增加字段级加密。
- 业务删除优先采用软删除，避免误删临床资料。
- 枚举字段 P0 可用字符串或数据库枚举，推荐先用字符串并在后端统一校验。
- 附件文件本体保存在对象存储，数据库只保存元数据和对象 Key。
- 所有关键操作通过 `audit_logs` 记录。

## 2. 通用字段

建议核心业务表统一包含：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | uuid | 主键 |
| department_id | uuid | 所属科室 |
| created_by | uuid | 创建人 |
| updated_by | uuid | 最后更新人 |
| created_at | timestamptz | 创建时间 |
| updated_at | timestamptz | 更新时间 |
| deleted_at | timestamptz | 软删除时间，未删除为空 |

## 3. 表结构

### 3.1 departments 科室表

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | uuid | 是 | 科室 ID |
| name | varchar(100) | 是 | 科室名称，例如乳腺外科 |
| hospital_name | varchar(150) | 否 | 医院名称 |
| status | varchar(20) | 是 | active, disabled |
| created_at | timestamptz | 是 | 创建时间 |
| updated_at | timestamptz | 是 | 更新时间 |

索引：

- `idx_departments_status(status)`

### 3.2 users 用户表

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | uuid | 是 | 用户 ID |
| department_id | uuid | 是 | 所属科室 |
| username | varchar(80) | 是 | 登录账号，同科室唯一 |
| password_hash | varchar(255) | 是 | 密码哈希 |
| display_name | varchar(80) | 是 | 用户姓名 |
| phone | varchar(30) | 否 | 手机号 |
| email | varchar(120) | 否 | 邮箱 |
| role | varchar(30) | 是 | admin, doctor, research_assistant, readonly |
| status | varchar(20) | 是 | active, disabled |
| last_login_at | timestamptz | 否 | 最近登录时间 |
| created_at | timestamptz | 是 | 创建时间 |
| updated_at | timestamptz | 是 | 更新时间 |
| deleted_at | timestamptz | 否 | 软删除时间 |

约束与索引：

- `uk_users_department_username(department_id, username)`
- `idx_users_department_role(department_id, role)`
- `idx_users_department_status(department_id, status)`

### 3.3 patients 患者表

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | uuid | 是 | 患者 ID |
| department_id | uuid | 是 | 所属科室 |
| name | varchar(80) | 是 | 患者姓名 |
| gender | varchar(20) | 是 | female, male, other, unknown |
| birth_date | date | 否 | 出生日期 |
| age_at_first_visit | int | 否 | 初诊年龄 |
| phone | varchar(30) | 否 | 手机号 |
| identity_no | varchar(30) | 否 | 身份证号 |
| outpatient_no | varchar(80) | 否 | 门诊号 |
| inpatient_no | varchar(80) | 否 | 住院号 |
| first_visit_date | date | 是 | 初诊日期 |
| attending_doctor_id | uuid | 否 | 主治医生 |
| remark | text | 否 | 备注 |
| created_by | uuid | 是 | 建档人 |
| updated_by | uuid | 是 | 更新人 |
| created_at | timestamptz | 是 | 创建时间 |
| updated_at | timestamptz | 是 | 更新时间 |
| deleted_at | timestamptz | 否 | 软删除时间 |

约束与索引：

- `idx_patients_department_name(department_id, name)`
- `idx_patients_department_phone(department_id, phone)`
- `idx_patients_department_outpatient_no(department_id, outpatient_no)`
- `idx_patients_department_inpatient_no(department_id, inpatient_no)`
- `idx_patients_department_first_visit(department_id, first_visit_date)`

说明：

- P0 不强制门诊号、住院号唯一，因为可能存在历史录入差异；后端在重复时提示确认。
- 后续如需强约束，可增加同科室内非空唯一索引。

### 3.4 clinical_cases 病例表

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | uuid | 是 | 病例 ID |
| department_id | uuid | 是 | 所属科室 |
| patient_id | uuid | 是 | 患者 ID |
| title | varchar(150) | 是 | 病例标题 |
| visit_type | varchar(30) | 是 | outpatient, inpatient, postoperative_review |
| laterality | varchar(20) | 是 | left, right, bilateral, unknown |
| disease_type | varchar(50) | 是 | breast_cancer, benign_tumor, mastitis, other |
| preliminary_diagnosis | text | 否 | 初步诊断 |
| confirmed_diagnosis | text | 否 | 确定诊断 |
| pathology_type | varchar(120) | 否 | 病理类型 |
| er_status | varchar(20) | 否 | positive, negative, unknown |
| pr_status | varchar(20) | 否 | positive, negative, unknown |
| her2_status | varchar(30) | 否 | zero, one_plus, two_plus, three_plus, fish_positive, fish_negative, unknown |
| ki67_percent | numeric(5,2) | 否 | Ki-67 百分比 |
| molecular_subtype | varchar(40) | 否 | luminal_a, luminal_b, her2_positive, triple_negative, unknown |
| clinical_t_stage | varchar(20) | 否 | 临床 T 分期 |
| clinical_n_stage | varchar(20) | 否 | 临床 N 分期 |
| clinical_m_stage | varchar(20) | 否 | 临床 M 分期 |
| pathological_t_stage | varchar(20) | 否 | 病理 T 分期 |
| pathological_n_stage | varchar(20) | 否 | 病理 N 分期 |
| pathological_m_stage | varchar(20) | 否 | 病理 M 分期 |
| clinical_stage | varchar(30) | 否 | 临床分期 |
| pathological_stage | varchar(30) | 否 | 病理分期 |
| has_neoadjuvant_therapy | boolean | 否 | 是否新辅助治疗 |
| neoadjuvant_therapy_plan | text | 否 | 新辅助治疗方案 |
| surgery_date | date | 否 | 手术日期 |
| surgery_type | varchar(120) | 否 | 手术方式 |
| axillary_management | varchar(120) | 否 | 腋窝处理方式 |
| chemotherapy_plan | text | 否 | 化疗方案 |
| has_radiotherapy | boolean | 否 | 是否放疗 |
| has_targeted_therapy | boolean | 否 | 是否靶向治疗 |
| targeted_therapy_plan | text | 否 | 靶向治疗方案 |
| has_endocrine_therapy | boolean | 否 | 是否内分泌治疗 |
| endocrine_therapy_plan | text | 否 | 内分泌治疗方案 |
| recurrence_status | varchar(40) | 否 | none, local_recurrence, distant_metastasis, unknown |
| current_status | varchar(40) | 是 | treating, follow_up, recurrence, deceased, lost |
| summary | text | 否 | 病例摘要 |
| case_status | varchar(20) | 是 | draft, archived |
| archived_at | timestamptz | 否 | 归档时间 |
| created_by | uuid | 是 | 创建人 |
| updated_by | uuid | 是 | 更新人 |
| created_at | timestamptz | 是 | 创建时间 |
| updated_at | timestamptz | 是 | 更新时间 |
| deleted_at | timestamptz | 否 | 软删除时间 |

索引：

- `idx_cases_department_patient(department_id, patient_id)`
- `idx_cases_department_status(department_id, case_status)`
- `idx_cases_department_disease(department_id, disease_type)`
- `idx_cases_department_subtype(department_id, molecular_subtype)`
- `idx_cases_department_stage(department_id, clinical_stage, pathological_stage)`
- `idx_cases_department_current_status(department_id, current_status)`
- `idx_cases_surgery_date(surgery_date)`

### 3.5 tags 标签表

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | uuid | 是 | 标签 ID |
| department_id | uuid | 是 | 所属科室 |
| name | varchar(80) | 是 | 标签名称 |
| color | varchar(20) | 否 | 标签颜色，例如 #2F80ED |
| created_by | uuid | 是 | 创建人 |
| created_at | timestamptz | 是 | 创建时间 |
| updated_at | timestamptz | 是 | 更新时间 |
| deleted_at | timestamptz | 否 | 软删除时间 |

约束与索引：

- `uk_tags_department_name(department_id, name)`

### 3.6 case_tag_relations 病例标签关系表

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | uuid | 是 | 关系 ID |
| department_id | uuid | 是 | 所属科室 |
| case_id | uuid | 是 | 病例 ID |
| tag_id | uuid | 是 | 标签 ID |
| created_by | uuid | 是 | 创建人 |
| created_at | timestamptz | 是 | 创建时间 |

约束与索引：

- `uk_case_tag(case_id, tag_id)`
- `idx_case_tag_department_tag(department_id, tag_id)`

### 3.7 attachments 附件表

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | uuid | 是 | 附件 ID |
| department_id | uuid | 是 | 所属科室 |
| patient_id | uuid | 是 | 患者 ID |
| case_id | uuid | 否 | 病例 ID |
| category | varchar(40) | 是 | pathology, imaging, laboratory, surgery, discharge_summary, outpatient_record, follow_up, other |
| original_filename | varchar(255) | 是 | 原始文件名 |
| content_type | varchar(120) | 是 | MIME 类型 |
| file_size | bigint | 是 | 文件大小 |
| object_key | varchar(500) | 是 | 对象存储 Key |
| checksum | varchar(128) | 否 | 文件校验值 |
| uploaded_by | uuid | 是 | 上传人 |
| uploaded_at | timestamptz | 是 | 上传时间 |
| deleted_at | timestamptz | 否 | 软删除时间 |

索引：

- `idx_attachments_department_patient(department_id, patient_id)`
- `idx_attachments_department_case(department_id, case_id)`
- `idx_attachments_category(category)`

### 3.8 follow_up_plans 随访计划表

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | uuid | 是 | 随访计划 ID |
| department_id | uuid | 是 | 所属科室 |
| patient_id | uuid | 是 | 患者 ID |
| case_id | uuid | 是 | 病例 ID |
| follow_up_type | varchar(50) | 是 | postoperative, chemotherapy, endocrine, imaging_review, routine, other |
| planned_date | date | 是 | 计划随访日期 |
| assignee_id | uuid | 否 | 负责人 |
| status | varchar(30) | 是 | pending, completed, overdue, lost |
| created_by | uuid | 是 | 创建人 |
| updated_by | uuid | 是 | 更新人 |
| created_at | timestamptz | 是 | 创建时间 |
| updated_at | timestamptz | 是 | 更新时间 |
| deleted_at | timestamptz | 否 | 软删除时间 |

索引：

- `idx_follow_up_plans_department_status_date(department_id, status, planned_date)`
- `idx_follow_up_plans_assignee(assignee_id, planned_date)`
- `idx_follow_up_plans_case(case_id)`

### 3.9 follow_up_records 随访记录表

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | uuid | 是 | 随访记录 ID |
| department_id | uuid | 是 | 所属科室 |
| follow_up_plan_id | uuid | 是 | 随访计划 ID |
| patient_id | uuid | 是 | 患者 ID |
| case_id | uuid | 是 | 病例 ID |
| actual_date | date | 是 | 实际随访日期 |
| method | varchar(40) | 是 | outpatient, phone, wechat, inpatient_review, other |
| survival_status | varchar(40) | 是 | disease_free, alive_with_disease, recurrence, metastasis, deceased, lost, unknown |
| has_recurrence | boolean | 否 | 是否复发 |
| recurrence_site | varchar(255) | 否 | 复发部位 |
| has_metastasis | boolean | 否 | 是否转移 |
| metastasis_site | varchar(255) | 否 | 转移部位 |
| imaging_summary | text | 否 | 影像检查摘要 |
| tumor_marker_summary | text | 否 | 肿瘤标志物摘要 |
| current_treatment | text | 否 | 当前治疗 |
| adverse_reactions | text | 否 | 不良反应 |
| remark | text | 否 | 随访备注 |
| next_follow_up_date | date | 否 | 下次随访日期 |
| recorded_by | uuid | 是 | 记录人 |
| created_at | timestamptz | 是 | 创建时间 |
| updated_at | timestamptz | 是 | 更新时间 |
| deleted_at | timestamptz | 否 | 软删除时间 |

索引：

- `idx_follow_up_records_plan(follow_up_plan_id)`
- `idx_follow_up_records_case(case_id, actual_date)`
- `idx_follow_up_records_patient(patient_id, actual_date)`

### 3.10 audit_logs 操作日志表

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | uuid | 是 | 日志 ID |
| department_id | uuid | 否 | 所属科室 |
| actor_id | uuid | 否 | 操作人，登录失败时可为空 |
| action | varchar(80) | 是 | 操作类型 |
| object_type | varchar(80) | 否 | 业务对象类型 |
| object_id | uuid | 否 | 业务对象 ID |
| before_snapshot | jsonb | 否 | 操作前摘要 |
| after_snapshot | jsonb | 否 | 操作后摘要 |
| ip_address | varchar(80) | 否 | IP 地址 |
| user_agent | text | 否 | 设备信息 |
| created_at | timestamptz | 是 | 操作时间 |

索引：

- `idx_audit_logs_department_time(department_id, created_at desc)`
- `idx_audit_logs_actor_time(actor_id, created_at desc)`
- `idx_audit_logs_object(object_type, object_id)`
- `idx_audit_logs_action(action)`

## 4. 关键外键关系

- `users.department_id -> departments.id`
- `patients.department_id -> departments.id`
- `patients.attending_doctor_id -> users.id`
- `clinical_cases.department_id -> departments.id`
- `clinical_cases.patient_id -> patients.id`
- `attachments.patient_id -> patients.id`
- `attachments.case_id -> clinical_cases.id`
- `tags.department_id -> departments.id`
- `case_tag_relations.case_id -> clinical_cases.id`
- `case_tag_relations.tag_id -> tags.id`
- `follow_up_plans.patient_id -> patients.id`
- `follow_up_plans.case_id -> clinical_cases.id`
- `follow_up_records.follow_up_plan_id -> follow_up_plans.id`
- `follow_up_records.patient_id -> patients.id`
- `follow_up_records.case_id -> clinical_cases.id`

## 5. P0 必填字段建议

患者：

- 姓名
- 性别
- 初诊日期

病例归档：

- 患者 ID
- 病例标题
- 就诊类型
- 侧别
- 疾病类型
- 当前状态
- 病例状态

随访计划：

- 患者 ID
- 病例 ID
- 随访类型
- 计划随访日期

随访记录：

- 随访计划 ID
- 实际随访日期
- 随访方式
- 当前生存或疾病状态

