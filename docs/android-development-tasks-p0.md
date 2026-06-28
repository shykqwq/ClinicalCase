# 乳腺外科临床病例库与科研随访 App Android 页面开发任务列表（P0 版）

版本：v0.1  
日期：2026-06-14  
技术建议：Kotlin + Jetpack Compose + MVVM + Retrofit + Room + DataStore + WorkManager

## 1. Android P0 开发目标

Android 端 P0 需要完成医生日常使用闭环：

- 登录进入系统。
- 查看首页工作台。
- 创建和检索患者。
- 创建、编辑、查看乳腺外科病例。
- 上传、预览附件。
- 使用科研筛选和标签。
- 创建、完成随访。
- 管理成员，仅管理员可见。
- 按权限展示完整或脱敏信息。

## 2. 建议模块划分

| 模块 | 说明 |
| --- | --- |
| app | Android 主工程 |
| core-network | Retrofit、拦截器、API Result |
| core-database | Room 本地缓存 |
| core-datastore | Token、用户配置、脱敏开关 |
| core-ui | 通用组件、主题、表单控件 |
| feature-auth | 登录 |
| feature-dashboard | 首页工作台 |
| feature-patient | 患者列表、详情、编辑 |
| feature-case | 病例列表、详情、编辑、筛选 |
| feature-attachment | 附件上传与预览 |
| feature-tag | 标签管理 |
| feature-followup | 随访计划与记录 |
| feature-user | 成员管理 |
| feature-settings | 个人设置 |

## 3. 基础工程任务

### A0-01 初始化 Android 工程

内容：

- 创建 Kotlin Android 项目。
- 启用 Jetpack Compose。
- 配置 Material 3。
- 配置多模块或按包分层结构。

验收：

- App 可正常启动。
- 有统一主题、颜色、字体和基础布局。

### A0-02 网络层

内容：

- 配置 Retrofit。
- 配置 OkHttp。
- 添加认证 Header 拦截器。
- 添加统一错误处理。
- 添加 JSON 序列化。

验收：

- 能调用测试 API。
- 401 时跳转登录或触发重新登录。
- 网络错误有统一提示。

### A0-03 本地存储

内容：

- 使用 DataStore 保存 access token、refresh token、当前用户信息。
- 使用 Room 预留患者、病例、随访列表缓存。

验收：

- 登录后重启 App 仍保持登录态。
- 退出登录后清理本地令牌。

### A0-04 导航结构

内容：

- 配置 Compose Navigation。
- 登录态和业务态分离。
- 底部导航或首页入口导航。

验收：

- 未登录进入登录页。
- 已登录进入首页。
- 页面返回逻辑正常。

### A0-05 权限与脱敏 UI 支持

内容：

- 根据用户角色控制入口显示。
- 封装敏感字段展示组件。
- 只读成员默认显示脱敏信息。

验收：

- 管理员可看到成员管理入口。
- 只读成员不能看到编辑、新增、删除按钮。
- 手机号、身份证号按权限脱敏。

## 4. 页面任务

### A1 登录页

路径建议：`feature-auth`

页面：

- 登录页

功能：

- 输入账号、密码。
- 点击登录。
- 展示加载中、错误提示。
- 登录成功进入首页。

接口：

- `POST /auth/login`
- `GET /auth/me`

验收：

- 正确账号可登录。
- 错误账号提示失败。
- 停用账号提示不可登录。

### A2 首页工作台

路径建议：`feature-dashboard`

页面：

- 首页工作台

功能：

- 展示今日待随访数量。
- 展示逾期随访数量。
- 展示病例总数。
- 展示最近新增病例。
- 提供快速搜索。
- 提供新建患者入口。
- 提供科研筛选入口。

接口：

- `GET /dashboard/summary`
- `GET /cases`
- `GET /follow-up-plans/today`
- `GET /follow-up-plans/overdue`

验收：

- 登录后默认进入首页。
- 点击待随访进入随访列表。
- 点击最近病例进入病例详情。

### A3 患者列表页

路径建议：`feature-patient`

页面：

- 患者列表页

功能：

- 患者分页列表。
- 关键词搜索。
- 初诊日期筛选。
- 下拉刷新。
- 上拉加载。
- 新建患者按钮。

接口：

- `GET /patients`

验收：

- 可以按姓名、住院号、门诊号搜索。
- 点击患者进入患者详情。
- 无结果时显示空状态。

### A4 新建/编辑患者页

页面：

- 新建患者页
- 编辑患者页

功能：

- 填写姓名、性别、出生日期、手机号、身份证号、门诊号、住院号、初诊日期、主治医生、备注。
- 保存前校验必填字段。
- 输入门诊号、住院号时可触发重复检查。

接口：

- `POST /patients`
- `PUT /patients/{patientId}`
- `GET /patients/duplicate-check`

验收：

- 姓名、性别、初诊日期必填。
- 保存成功后返回患者详情。
- 重复患者提示用户确认。

### A5 患者详情页

页面：

- 患者详情页

功能：

- 展示患者基础信息。
- 展示患者病例列表。
- 展示附件入口。
- 展示随访摘要。
- 提供编辑患者、新建病例、新建随访入口。

接口：

- `GET /patients/{patientId}`
- `GET /cases?patientId=...`
- `GET /attachments?patientId=...`
- `GET /follow-up-records?patientId=...`

验收：

- 患者真实信息按权限展示。
- 点击病例进入病例详情。
- 只读成员不显示编辑入口。

### A6 病例详情页

路径建议：`feature-case`

页面：

- 病例详情页

功能：

- 展示诊断信息。
- 展示病理与分子分型。
- 展示分期信息。
- 展示治疗信息。
- 展示当前状态。
- 展示标签。
- 展示附件列表。
- 展示随访记录摘要。
- 提供编辑、归档、上传附件、添加标签入口。

接口：

- `GET /cases/{caseId}`
- `POST /cases/{caseId}/archive`
- `PUT /cases/{caseId}/tags`

验收：

- 病例详情信息完整分组展示。
- 草稿病例可归档。
- 归档校验失败时展示缺失字段。

### A7 新建/编辑乳腺专病病例页

页面：

- 新建病例页
- 编辑病例页

功能：

- 分组表单录入专病字段。
- 支持草稿保存。
- 支持归档。
- 支持选择枚举字段。
- 支持填写治疗方案长文本。

建议表单分组：

- 基本信息
- 诊断信息
- 病理与分子分型
- TNM 与分期
- 手术与腋窝处理
- 综合治疗
- 复发与当前状态
- 病例摘要

接口：

- `POST /cases`
- `PUT /cases/{caseId}`
- `POST /cases/{caseId}/archive`
- `GET /dictionaries`

验收：

- 草稿允许缺少部分字段。
- 归档必须校验必填字段。
- 表单较长时滚动和分组清晰。

### A8 搜索与科研筛选页

页面：

- 科研筛选页
- 筛选结果页

功能：

- 支持组合筛选。
- 支持疾病类型、分子分型、分期、治疗方式、复发状态、标签等条件。
- 展示筛选结果数量和病例列表。
- 点击结果进入病例详情。

接口：

- `GET /cases`
- `GET /tags`
- `GET /dictionaries`

验收：

- 多条件筛选结果正确。
- 支持清空筛选条件。
- 空结果显示提示。

### A9 附件列表与上传

路径建议：`feature-attachment`

页面：

- 附件列表页
- 附件上传页
- 附件预览页

功能：

- 按分类查看附件。
- 选择图片或 PDF。
- 上传前校验大小和格式。
- 获取上传授权。
- 上传至对象存储。
- 保存附件元数据。
- 预览图片或 PDF。

接口：

- `POST /attachments/upload-token`
- `POST /attachments`
- `GET /attachments`
- `GET /attachments/{attachmentId}/preview-url`
- `DELETE /attachments/{attachmentId}`

验收：

- 支持 JPG、PNG、PDF。
- 单文件超过 20 MB 时提示。
- 上传过程显示进度。
- 上传成功后列表刷新。

### A10 标签管理

路径建议：`feature-tag`

页面：

- 标签管理页
- 标签选择弹窗

功能：

- 查看标签列表。
- 新建标签。
- 编辑标签名称和颜色。
- 删除标签。
- 给病例绑定多个标签。

接口：

- `GET /tags`
- `POST /tags`
- `PUT /tags/{tagId}`
- `DELETE /tags/{tagId}`
- `PUT /cases/{caseId}/tags`

验收：

- 同名标签创建失败时提示。
- 病例详情可显示标签。
- 科研筛选可按标签筛选。

### A11 随访列表页

路径建议：`feature-followup`

页面：

- 随访列表页

功能：

- 查看待随访、已完成、已逾期、失访。
- 按日期范围筛选。
- 按负责人筛选。
- 点击进入计划详情或填写记录。

接口：

- `GET /follow-up-plans`
- `GET /follow-up-plans/today`
- `GET /follow-up-plans/overdue`

验收：

- 首页点击待随访可进入对应筛选。
- 逾期随访有明显状态标识。

### A12 新建/编辑随访计划页

页面：

- 新建随访计划页
- 编辑随访计划页

功能：

- 选择患者和病例。
- 选择随访类型。
- 设置计划日期。
- 设置负责人。
- 标记失访。

接口：

- `POST /follow-up-plans`
- `PUT /follow-up-plans/{planId}`
- `POST /follow-up-plans/{planId}/mark-lost`

验收：

- 计划日期必填。
- 创建成功后出现在随访列表。
- 失访状态保存成功。

### A13 新建/编辑随访记录页

页面：

- 新建随访记录页
- 编辑随访记录页

功能：

- 填写实际随访日期。
- 选择随访方式。
- 记录当前状态。
- 填写复发、转移、影像摘要、肿瘤标志物、当前治疗、不良反应和备注。
- 设置下次随访日期。

接口：

- `POST /follow-up-records`
- `PUT /follow-up-records/{recordId}`
- `GET /follow-up-records/{recordId}`

验收：

- 保存记录后原计划变为已完成。
- 有下次随访日期时自动生成下一次计划。

### A14 成员管理页

路径建议：`feature-user`

页面：

- 成员列表页
- 新增/编辑成员页

功能：

- 管理员查看成员列表。
- 新增成员。
- 编辑成员角色和状态。
- 停用或启用成员。

接口：

- `GET /users`
- `POST /users`
- `PUT /users/{userId}`
- `POST /users/{userId}/disable`
- `POST /users/{userId}/enable`

验收：

- 仅管理员可进入。
- 角色变更后页面展示更新。
- 停用成员后状态显示为停用。

### A15 个人设置页

路径建议：`feature-settings`

页面：

- 个人设置页

功能：

- 查看当前账号信息。
- 查看角色和科室。
- 脱敏显示开关，按权限可用。
- 退出登录。

接口：

- `GET /auth/me`
- `POST /auth/logout`

验收：

- 退出登录后回到登录页。
- 本地 token 被清理。

## 5. 推荐开发顺序

### Sprint 1：基础工程与登录

- A0-01 初始化 Android 工程
- A0-02 网络层
- A0-03 本地存储
- A0-04 导航结构
- A1 登录页

### Sprint 2：患者与首页

- A2 首页工作台
- A3 患者列表页
- A4 新建/编辑患者页
- A5 患者详情页

### Sprint 3：病例核心闭环

- A6 病例详情页
- A7 新建/编辑乳腺专病病例页
- A8 搜索与科研筛选页

### Sprint 4：附件与标签

- A9 附件列表与上传
- A10 标签管理

### Sprint 5：随访与成员管理

- A11 随访列表页
- A12 新建/编辑随访计划页
- A13 新建/编辑随访记录页
- A14 成员管理页
- A15 个人设置页

## 6. Android P0 验收总清单

- App 可以登录、保持登录态、退出登录。
- 首页可以展示待随访、逾期随访、病例总数和最近病例。
- 可以创建、搜索、查看、编辑患者。
- 可以创建、编辑、归档、查看乳腺专病病例。
- 可以上传、查看、删除授权范围内附件。
- 可以按专病字段和标签筛选病例。
- 可以创建随访计划、完成随访记录、自动生成下次随访。
- 管理员可以管理成员。
- 只读成员默认看到脱敏信息，并不能进行新增、编辑、删除。
- 网络异常、空状态、加载中、权限不足都有明确 UI 状态。

