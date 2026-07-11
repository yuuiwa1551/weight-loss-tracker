# Phase 5：本地多用户与 AstrBot 接入

## 1. 目标与边界

本阶段把 V2 从单用户 Web demo 扩展为本机运行的多用户减重数据服务，并通过 AstrBot 聊天完成自然语言录入和查询。

本阶段不建设传统登录注册、公网服务或移动端 App。Web 仍是仅供本机使用的管理界面，AstrBot 是主要聊天入口。

明确不做：

- 用户名密码、OAuth、JWT 登录页面
- 公网暴露、VPS 部署和互联网多租户服务
- 医疗级营养建议或诊断
- 外部营养数据库、食物图片识别
- 体脂秤、Apple Health 或其他健康 App 同步
- 第一阶段强制迁移 MySQL

## 2. 总体架构

```text
QQ 私聊/群聊
    |
    v
AstrBot Agent + weight-loss LLM tools
    |
    | HTTP, Docker network
    v
Spring Boot API + React Web
    |
    v
H2 file database
```

职责边界：

- AstrBot 负责意图识别、结构化参数生成、营养估算和聊天交互
- 插件负责身份解析、待确认状态、API 调用、错误转换和 ToolLoop 去重
- Spring Boot 负责校验、用户隔离、持久化、汇总、报表和写入幂等
- React Web 负责本机多用户查看与人工维护
- 插件不得直接访问 H2 文件，也不得维护第二份业务数据库

## 3. 用户身份与资料

### 3.1 QQ 身份

AstrBot `aiocqhttp` 事件的 `event.get_sender_id()` 返回发送者 QQ 号。第一期直接将该字符串作为 `AppUser.username`。

身份规则：

- `platform` 固定为 `aiocqhttp`
- `username` 为纯数字 QQ 号字符串
- 数据库唯一键为 `(platform, username)`
- `event.get_sender_name()` 仅作为可更新的 `displayName`
- `groupId`、`sessionId` 和 Bot 自身 QQ 不参与用户唯一键
- 同一 QQ 在私聊、不同群和多个 Bot 实例中共享数据
- sender ID 为空或不是纯数字时拒绝自动建档，不使用昵称兜底

### 3.2 渐进建档

首次使用时只创建 `AppUser` 和空资料。昵称、身高、目标体重、每日热量目标可以后续补充。

行为规则：

- 食物和体重可在资料未完善时记录
- BMI 需要身高和当前体重
- 目标差和目标状态需要目标体重或每日热量目标
- 缺少必要字段时返回明确的 `UNSET` 或 `missingFields`，不套用 Demo 默认值
- 最新日期的体重记录同步为资料中的当前体重
- 删除最新体重后，重新取剩余最新记录；没有记录时当前体重为空

## 4. 数据模型

新增 `AppUser`：

- `id`
- `platform`
- `username`
- `displayName`
- `createdAt`
- `updatedAt`

现有 `UserProfile`、`FoodRecord`、`ExerciseRecord` 和 `WeightRecord` 增加 `user_id` 外键。所有按日期查询的索引调整为以 `user_id` 开头的联合索引。

写入记录增加：

- `clientRequestId`：可空；同一用户内唯一，用于 AstrBot 重试幂等
- `source`：`WEB` 或 `ASTRBOT`
- 食物记录增加 `nutritionSource`：`USER_PROVIDED` 或 `LLM_ESTIMATE`
- 食物估算说明保存在独立字段，不混入用户备注

默认数据库继续使用 H2 文件模式。数据量按 20 个用户、每人每天 10 条、连续 10 年估算不足百万条，H2 足够承担单实例本地服务。

## 5. API 契约

统一响应结构保持不变。

### 5.1 用户解析

```http
POST /api/users/resolve
GET /api/users
```

解析请求：

```json
{
  "platform": "aiocqhttp",
  "username": "1154824108",
  "displayName": "雪"
}
```

`resolve` 必须幂等：相同 `(platform, username)` 返回原用户，并更新非空显示昵称。

### 5.2 用户范围接口

```http
GET  /api/users/{userId}/profile
PUT  /api/users/{userId}/profile

POST   /api/users/{userId}/food-records
GET    /api/users/{userId}/food-records?date=2026-07-12
DELETE /api/users/{userId}/food-records/{recordId}

POST   /api/users/{userId}/exercise-records
GET    /api/users/{userId}/exercise-records?date=2026-07-12
DELETE /api/users/{userId}/exercise-records/{recordId}

POST   /api/users/{userId}/weight-records
GET    /api/users/{userId}/weight-records/recent?days=30
DELETE /api/users/{userId}/weight-records/{recordId}

GET /api/users/{userId}/summaries/daily?date=2026-07-12
GET /api/users/{userId}/summaries/recent?days=7
GET /api/users/{userId}/reports/overview?days=30
```

所有删除操作同时校验记录所属用户。跨用户访问返回 `404`，不泄露记录是否存在。

旧的全局单用户路由由新接口替代，Web 与 API 文档在同一阶段更新，不长期保留兼容别名。

## 6. AstrBot 插件

源码目录：

```text
integrations/astrbot_plugin_weight_loss_tracker/
├── main.py
├── metadata.yaml
├── _conf_schema.json
├── README.md
├── requirements.txt
├── core/
│   ├── api_client.py
│   ├── identity.py
│   ├── pending.py
│   └── tools.py
└── tests/
```

`main.py` 只负责注册和生命周期，HTTP、身份、待确认和业务工具放入 `core/`。

第一期 LLM tools：

- 查询和更新资料
- 记录食物、运动和体重
- 查询今日汇总、近期趋势和周期报表
- 确认、取消待处理记录
- 预览并撤销最近一条记录

备用指令：

- `/减重状态`
- `/减重确认`
- `/减重取消`
- `/体重 72.4`

### 6.1 确认规则

- 用户明确提供体重或热量数值时可直接写入
- LLM 推断食物营养或运动消耗时必须先生成预览
- 预览包含记录日期、内容、热量、三大营养素、估算标识和假设说明
- 每个 QQ 只保留一个最新待确认操作，有效期 10 分钟
- 新预览覆盖旧预览时必须提示
- 撤销最近记录属于破坏性操作，必须二次确认
- 待确认状态只保存在插件内存中，插件重启后要求用户重新提交

### 6.2 幂等与故障处理

- `clientRequestId` 由平台、消息 ID 和工具名生成
- 后端收到重复请求时返回已有记录，不重复插入
- HTTP 超时默认 5 秒，连接类瞬时错误重试一次
- 参数校验错误和其他 4xx 不重试
- 后端不可用时不清除待确认项，并返回可操作的重试提示

默认配置：

- `backend_base_url=http://weight-loss-tracker:8080`
- `allow_private=true`
- `allow_group=true`
- `pending_ttl_seconds=600`
- `request_timeout_seconds=5`

插件复用 AstrBot 当前 Agent 和模型，不保存独立 LLM API Key。

## 7. Web 管理界面

- 增加用户选择器，显示“昵称 / QQ号”
- 默认选择上次使用的用户，并保存在浏览器本地存储
- 切换用户后重新加载资料、记录、趋势和报表
- 防止旧用户的异步请求覆盖新用户页面
- 用户资料未完善时显示缺失字段和设置入口
- 所有 API 调用包含当前 `userId`
- Web 只通过 `127.0.0.1` 暴露，不增加登录页面

## 8. 本地 Docker 部署

- 多阶段镜像先构建 React，再构建 Spring Boot，并由同一容器提供静态页面和 API
- React Router 非 `/api` 路径回退到 `index.html`
- 服务名为 `weight-loss-tracker`
- 加入外部网络 `astrbot_astrbot_network`
- AstrBot 插件通过服务名访问后端
- 宿主端口绑定 `127.0.0.1:8080:8080`
- H2 数据目录挂载到 D 盘持久化目录
- H2 Console 只在开发 profile 启用
- 增加健康接口和 Docker healthcheck
- 备份脚本只停止减重服务，复制 H2 数据目录后恢复服务

增加可选 MySQL profile，数据库 URL、用户名和密码全部来自环境变量。MySQL 不进入默认启动路径。

## 9. 测试与验收

### 9.1 后端

- 用户解析幂等和昵称更新
- 两用户资料、记录、汇总、趋势和报表隔离
- 跨用户删除返回 `404`
- 资料未完善时的可空字段和 `UNSET`
- 最新体重同步及删除回退
- `clientRequestId` 重复写入幂等
- 日期、天数、体重、热量和营养边界校验

### 9.2 前端

- 用户列表加载、选择和本地持久化
- 用户切换后的数据刷新和竞态保护
- 资料未完善状态
- CRUD 后仪表盘和报表刷新
- API 错误、加载和空状态

### 9.3 插件

- QQ 身份提取和非法 sender 拒绝
- 私聊与群聊映射到同一用户
- 不同 QQ 的待确认项隔离
- 估算预览、确认、取消和过期
- 明确数值直接写入
- 撤销确认和 ToolLoop 去重
- 后端超时、校验错误和不可用

自动测试不调用真实 LLM API，使用模拟工具参数和模拟 HTTP。

### 9.4 Docker 与运行环境

- 镜像成功构建
- 健康接口和本地 Web 返回成功
- 容器重启后 H2 数据仍存在
- AstrBot 容器可以解析并访问 `weight-loss-tracker`
- 插件运行副本可加载并完成真实 QQ 用户解析和 API smoke test

## 10. 阶段交付规则

1. 每个实施阶段先完成测试和文档更新
2. 验证通过后创建中文 Git 提交
3. 推送前检查 `master` 与 `origin/master` 是否分叉
4. 仅推送 V2 源码、文档和插件发布内容
5. 不提交 AstrBot 数据、日志、数据库、密钥、缓存或运行时临时文件
6. 插件源码与 `D:\astrbot\data\plugins` 运行副本分开维护
