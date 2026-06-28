# Android 端本地运行说明

日期：2026-06-19  
工程目录：`android`

## 1. 前置条件

- Android Studio
- Android SDK
- JDK 17 或 Android Studio 自带 JBR
- 后端服务已启动在 `http://localhost:3001/api/v1`

## 2. 打开工程

用 Android Studio 打开：

```text
E:\TRAE_projrcts\ClinicalCase\android
```

首次打开时，Android Studio 会同步 Gradle 依赖。

## 3. 后端地址

当前 Android 模拟器访问本机后端使用：

```text
http://10.0.2.2:3001/api/v1/
```

配置位置：

```text
android/app/build.gradle
```

```kotlin
buildConfigField "String", "API_BASE_URL", "\"http://10.0.2.2:3001/api/v1/\""
```

如果使用真机调试，需要把 `10.0.2.2` 改成电脑在局域网中的 IP，例如：

```text
http://192.168.1.100:3001/api/v1/
```

同时确保手机和电脑在同一网络，且防火墙允许访问 3001 端口。

## 4. 当前已实现页面

- 登录页
- Token 本地保存
- 首页工作台
- 首页统计接口 `/dashboard/summary`
- 最近病例列表
- 患者列表
- 患者搜索
- 新建患者
- 退出登录

默认测试账号：

```text
username: admin
password: ChangeMe123
```

## 5. 下一阶段页面

- 病例详情
- 新建病例
- 标签选择
- 随访列表
