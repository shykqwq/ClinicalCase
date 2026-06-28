# 乳腺外科临床病例库与科研随访 App API 接口清单（P0 版）

版本：v0.1  
日期：2026-06-14  
接口风格：RESTful API  
基础路径：`/api/v1`

## 1. 通用约定

### 1.1 认证

除登录接口外，所有业务接口都需要携带访问令牌：

```http
Authorization: Bearer <access_token>
```

### 1.2 响应结构

成功响应：

```json
{
  "success": true,
  "data": {},
  "message": "ok"
}
```

失败响应：

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "参数错误"
  }
}
```

分页响应：

```json
{
  "success": true,
  "data": {
    "items": [],
    "page": 1,
    "pageSize": 20,
    "total": 100
  }
}
```

### 1.3 通用错误码

| 错误码 | 说明 |
| --- | --- |
| UNAUTHORIZED | 未登录或令牌无效 |
| FORBIDDEN | 无权限 |
| VALIDATION_ERROR | 参数校验失败 |
| NOT_FOUND | 资源不存在 |
| CONFLICT | 数据冲突 |
| FILE_TOO_LARGE | 文件过大 |
| UNSUPPORTED_FILE_TYPE | 不支持的文件类型 |
| INTERNAL_ERROR | 服务端错误 |

## 2. Auth API

### 2.1 登录

`POST /auth/login`

请求：

```json
{
  "username": "doctor01",
  "password": "password"
}
```

响应：

```json
{
  "accessToken": "jwt-token",
  "refreshToken": "refresh-token",
  "expiresIn": 7200,
  "user": {
    "id": "uuid",
    "displayName": "张医生",
    "role": "doctor",
    "departmentId": "uuid"
  }
}
```

权限：公开

### 2.2 刷新令牌

`POST /auth/refresh`

请求：

```json
{
  "refreshToken": "refresh-token"
}
```

权限：公开

### 2.3 当前用户信息

`GET /auth/me`

权限：登录用户

### 2.4 退出登录

`POST /auth/logout`

权限：登录用户

## 3. User API

### 3.1 成员列表

`GET /users?page=1&pageSize=20&role=doctor&status=active`

权限：管理员

### 3.2 新增成员

`POST /users`

请求：

```json
{
  "username": "doctor02",
  "password": "initialPassword",
  "displayName": "李医生",
  "phone": "13800000000",
  "role": "doctor",
  "status": "active"
}
```

权限：管理员

### 3.3 成员详情

`GET /users/{userId}`

权限：管理员

### 3.4 编辑成员

`PUT /users/{userId}`

权限：管理员

### 3.5 停用成员

`POST /users/{userId}/disable`

权限：管理员

### 3.6 启用成员

`POST /users/{userId}/enable`

权限：管理员

## 4. Patient API

### 4.1 患者列表

`GET /patients?page=1&pageSize=20&keyword=张&firstVisitStart=2026-01-01&firstVisitEnd=2026-12-31`

查询参数：

| 参数 | 说明 |
| --- | --- |
| keyword | 姓名、手机号、门诊号、住院号 |
| firstVisitStart | 初诊开始日期 |
| firstVisitEnd | 初诊结束日期 |
| attendingDoctorId | 主治医生 |

权限：登录用户

### 4.2 新增患者

`POST /patients`

请求：

```json
{
  "name": "张三",
  "gender": "female",
  "birthDate": "1975-03-01",
  "phone": "13800000000",
  "identityNo": "可选",
  "outpatientNo": "MZ001",
  "inpatientNo": "ZY001",
  "firstVisitDate": "2026-06-14",
  "attendingDoctorId": "uuid",
  "remark": "备注"
}
```

权限：管理员、医生、研究助手

### 4.3 患者详情

`GET /patients/{patientId}`

权限：登录用户，按角色返回完整或脱敏字段

### 4.4 编辑患者

`PUT /patients/{patientId}`

权限：管理员、医生、授权研究助手

### 4.5 删除患者

`DELETE /patients/{patientId}`

权限：管理员

### 4.6 患者重复检查

`GET /patients/duplicate-check?outpatientNo=MZ001&inpatientNo=ZY001&phone=13800000000`

权限：管理员、医生、研究助手

## 5. Case API

### 5.1 病例列表与科研筛选

`GET /cases?page=1&pageSize=20&keyword=乳腺癌&molecularSubtype=triple_negative&caseStatus=archived`

查询参数：

| 参数 | 说明 |
| --- | --- |
| keyword | 诊断关键词、患者姓名、住院号 |
| patientId | 患者 ID |
| diseaseType | 疾病类型 |
| laterality | 侧别 |
| pathologyType | 病理类型 |
| erStatus | ER 状态 |
| prStatus | PR 状态 |
| her2Status | HER2 状态 |
| molecularSubtype | 分子分型 |
| clinicalStage | 临床分期 |
| pathologicalStage | 病理分期 |
| surgeryType | 手术方式 |
| hasNeoadjuvantTherapy | 是否新辅助 |
| hasRadiotherapy | 是否放疗 |
| hasTargetedTherapy | 是否靶向 |
| hasEndocrineTherapy | 是否内分泌 |
| recurrenceStatus | 复发状态 |
| currentStatus | 当前状态 |
| tagIds | 标签 ID，逗号分隔 |
| surgeryStart | 手术开始日期 |
| surgeryEnd | 手术结束日期 |

权限：登录用户

### 5.2 新增病例

`POST /cases`

权限：管理员、医生、研究助手

### 5.3 病例详情

`GET /cases/{caseId}`

权限：登录用户

### 5.4 编辑病例

`PUT /cases/{caseId}`

权限：管理员、医生、研究助手

### 5.5 病例归档

`POST /cases/{caseId}/archive`

说明：校验归档必填字段，归档成功后设置 `caseStatus=archived`。

权限：管理员、医生、研究助手

### 5.6 删除病例

`DELETE /cases/{caseId}`

权限：管理员

### 5.7 病例标签绑定

`PUT /cases/{caseId}/tags`

请求：

```json
{
  "tagIds": ["uuid-1", "uuid-2"]
}
```

权限：管理员、医生、研究助手

## 6. Attachment API

### 6.1 创建上传授权

`POST /attachments/upload-token`

请求：

```json
{
  "patientId": "uuid",
  "caseId": "uuid",
  "category": "pathology",
  "filename": "report.pdf",
  "contentType": "application/pdf",
  "fileSize": 1048576
}
```

响应：

```json
{
  "uploadUrl": "https://object-storage-signed-url",
  "objectKey": "departments/xxx/patients/xxx/report.pdf",
  "expiresIn": 600
}
```

权限：管理员、医生、研究助手

### 6.2 保存附件元数据

`POST /attachments`

请求：

```json
{
  "patientId": "uuid",
  "caseId": "uuid",
  "category": "pathology",
  "originalFilename": "report.pdf",
  "contentType": "application/pdf",
  "fileSize": 1048576,
  "objectKey": "departments/xxx/patients/xxx/report.pdf",
  "checksum": "optional"
}
```

权限：管理员、医生、研究助手

### 6.3 附件列表

`GET /attachments?patientId=uuid&caseId=uuid&category=pathology`

权限：登录用户

### 6.4 附件预览地址

`GET /attachments/{attachmentId}/preview-url`

响应：

```json
{
  "url": "https://object-storage-signed-download-url",
  "expiresIn": 600
}
```

权限：登录用户

### 6.5 删除附件

`DELETE /attachments/{attachmentId}`

权限：管理员、上传者

## 7. Tag API

### 7.1 标签列表

`GET /tags`

权限：登录用户

### 7.2 新增标签

`POST /tags`

请求：

```json
{
  "name": "三阴性乳腺癌",
  "color": "#2F80ED"
}
```

权限：管理员、医生、研究助手

### 7.3 编辑标签

`PUT /tags/{tagId}`

权限：管理员、医生、研究助手

### 7.4 删除标签

`DELETE /tags/{tagId}`

权限：管理员、医生、研究助手

## 8. Follow-up API

### 8.1 随访计划列表

`GET /follow-up-plans?page=1&pageSize=20&status=pending&plannedStart=2026-06-01&plannedEnd=2026-06-30&assigneeId=uuid`

权限：登录用户

### 8.2 今日待随访

`GET /follow-up-plans/today`

权限：登录用户

### 8.3 逾期随访

`GET /follow-up-plans/overdue`

权限：登录用户

### 8.4 新增随访计划

`POST /follow-up-plans`

请求：

```json
{
  "patientId": "uuid",
  "caseId": "uuid",
  "followUpType": "postoperative",
  "plannedDate": "2026-07-14",
  "assigneeId": "uuid"
}
```

权限：管理员、医生、研究助手

### 8.5 编辑随访计划

`PUT /follow-up-plans/{planId}`

权限：管理员、医生、研究助手

### 8.6 标记失访

`POST /follow-up-plans/{planId}/mark-lost`

权限：管理员、医生、研究助手

### 8.7 新增随访记录

`POST /follow-up-records`

说明：保存记录后，将对应计划置为已完成；如果传入 `nextFollowUpDate`，自动生成下一次随访计划。

权限：管理员、医生、研究助手

### 8.8 随访记录列表

`GET /follow-up-records?patientId=uuid&caseId=uuid`

权限：登录用户

### 8.9 随访记录详情

`GET /follow-up-records/{recordId}`

权限：登录用户

### 8.10 编辑随访记录

`PUT /follow-up-records/{recordId}`

权限：管理员、医生、研究助手

## 9. Dashboard API

### 9.1 首页概览

`GET /dashboard/summary`

响应：

```json
{
  "todayFollowUpCount": 5,
  "overdueFollowUpCount": 2,
  "totalCaseCount": 356,
  "recentCases": []
}
```

权限：登录用户

## 10. Audit API

### 10.1 操作日志列表

`GET /audit-logs?page=1&pageSize=20&action=CASE_UPDATE&actorId=uuid&start=2026-06-01&end=2026-06-14`

权限：管理员

### 10.2 操作日志详情

`GET /audit-logs/{logId}`

权限：管理员

## 11. 枚举字典 API

### 11.1 获取枚举字典

`GET /dictionaries`

说明：返回性别、角色、病例状态、分子分型、随访状态等前端表单所需选项。

权限：登录用户

