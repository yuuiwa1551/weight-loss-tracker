# 开发计划

## 推荐起点

先写 spec 和 plan，再开始建项目骨架。

原因：旧项目最大的问题是模型、接口和前端预期没有先对齐。V2 要先把这些边界写清楚，再按契约实现。

## 阶段 0：确定项目骨架

建议采用单仓库结构：

```text
weight-loss-tracker-v2/
├── backend/
├── web/
└── docs/
```

当前采用技术栈：

- backend：Spring Boot 4 + Java 17 target，已在 Java 21 环境验证
- web：React + Vite + TypeScript + React Router
- database：H2 文件数据库，用于 demo 快速启动，后续再切 MySQL

如果你更想贴近真实部署，可以第一版直接用 MySQL，但开发成本会稍高。

## 阶段 1：后端可运行版本

状态：已完成。

目标：后端能启动，API 契约先稳定。

已完成任务：

1. 创建 Spring Boot 项目
2. 建立基础包结构
   - controller
   - service
   - repository
   - entity
   - dto
   - common
3. 实现统一响应结构
4. 实现 UserProfile 接口
5. 实现 FoodRecord 接口
6. 实现 ExerciseRecord 接口
7. 实现 DailySummary 实时聚合接口
8. 加入 MockMvc 接口测试
9. 加入 H2 配置、CORS 配置、种子数据和统一异常处理

完成标准：

- `GET /api/profile` 可用
- `POST /api/food-records` 可用
- `POST /api/exercise-records` 可用
- `GET /api/summaries/daily` 可用
- `GET /api/summaries/recent` 可用

## 阶段 2：前端可运行版本

状态：已完成。

目标：前端能调用后端真实接口，不使用假数据掩盖错误。

已完成任务：

1. 创建 Vite React TypeScript 项目
2. 建立 API client
3. 建立页面结构
   - Dashboard
   - Food Records
   - Exercise Records
   - Profile
4. 接入真实后端接口
5. 做基础加载、错误和空状态
6. 增加 Vite `/api` 代理
7. 完成 lint、build 和浏览器联调

完成标准：

- 新增食物后，Dashboard 汇总变化
- 新增运动后，Dashboard 汇总变化
- Profile 修改后，目标热量影响汇总

## 阶段 3：体验和可维护性整理

状态：已完成。

目标：把 Phase 2 的单文件前端整理成更容易维护和继续扩展的结构。

已完成任务：

1. 提取表单组件
2. 提取统计卡片组件
3. 增加路由
4. 统一错误提示
5. 补充 README 启动文档
6. 加入基本 lint/build 检查

完成标准：

- 前端支持 `/`、`/food`、`/exercise`、`/profile` 路由
- Dashboard、记录页和 Profile 页从 `App` 中拆出
- 表单、统计卡片、记录列表和通知组件可复用
- `npm run check` 可一次执行 lint 和 build

## 阶段 4：后续扩展

状态：第一轮扩展已完成。

可以考虑：

1. 登录注册和多用户
2. MySQL 持久化部署
3. AI 食物营养估算
4. 体重记录和趋势图
5. 周/月报表
6. 移动端适配增强

已完成扩展：

1. 增加体重记录后端接口
2. 增加前端 `/weight` 体重趋势页面
3. 使用最近 30 天体重记录展示趋势和目标差距
4. 增加 7 天和 30 天周期报表汇总

## 下一轮建议动作

Phase 5 已完成，实施结果和边界详见 [PHASE_5_ASTRBOT.md](PHASE_5_ASTRBOT.md)。

## 阶段 5：本地多用户与 AstrBot 接入

状态：已完成。

目标：不引入传统登录注册和公网服务，在本机完成基于 QQ 身份的数据隔离，并让 AstrBot 通过 LLM tools 记录、查询和纠正减重数据。

已锁定决策：

1. 用户名直接使用 AstrBot `aiocqhttp` 事件中的发送者 QQ 号
2. 同一 QQ 在私聊、群聊和多个 Bot 实例中共享同一份数据
3. Spring Boot 后端是唯一业务和数据服务，Web 与插件只调用 API
4. 默认继续使用 H2 文件数据库，MySQL 仅作为可选 profile
5. LLM 估算的食物营养和运动消耗必须先预览、再确认
6. 体重第一期由用户通过聊天或 Web 手动上报
7. 本地 Web 增加用户选择器，不增加登录页面
8. 后端与 Web 作为本地 Docker 服务加入 `astrbot_astrbot_network`
9. 插件源码保留在 V2 仓库，运行副本部署到 AstrBot 数据目录

实施顺序：

1. 多用户领域模型、隔离 API、幂等写入和后端测试
2. Web 用户选择器、资料未完善状态和前端测试
3. AstrBot LLM tools 插件、确认/取消/撤销流程和插件测试
4. Docker 构建、H2 持久化、备份脚本和可选 MySQL 配置
5. 全量验证、分阶段提交推送和真实 AstrBot 运行联调

验收结果：

- 后端 16 项测试通过
- 前端 lint、3 项测试和生产构建通过
- 插件 9 项测试及 AstrBot 镜像导入检查通过
- Docker 健康检查、SPA 路由、H2 重启持久化和备份恢复启动通过
- AstrBot 运行副本加载 v0.1.0，注册 10 个 LLM tools
- `aiocqhttp` QQ 事件身份解析和后端资料查询 smoke test 通过
- Phase 5 的 6 个阶段提交已推送到 `origin/master`

完成标准：

- 两个 QQ 用户的资料、记录、汇总和报表互不可见
- 同一 QQ 在私聊和群聊中解析为同一用户
- LLM 估算记录未经确认不会写入数据库
- 重复 ToolLoop 请求不会重复写入
- Web 可以切换并管理不同 QQ 用户
- H2 数据在容器重启后仍然存在
- Maven、前端、插件和 Docker 验证全部通过
- 插件运行副本在当前 AstrBot 容器中加载并完成真实 API 联调

## 阶段 6：动态热量预算与减重计划

状态：已完成。7 个阶段均已实施、验证并按阶段推送。

目标：根据用户的最小身体资料和非运动日常活动估算维持热量，在用户确认减重计划后生成每日缺口与摄入目标；食物和运动记录成功后实时返回今日剩余可摄入热量和预计缺口。

已确定方向：

1. LLM 负责理解、补问和解释，后端负责所有公式、汇总和持久化
2. 只新增年龄、计算用性别和非运动日常活动等级等必要字段
3. 专项运动按当天记录单独计入，避免与日常活动系数重复计算
4. 保留手动热量目标，并增加可确认、可追溯的自动计划
5. P6 不增加 BMI、最低摄入等医学安全拦截
6. P6 不增加 Coser 专属字段、图片识别、设备同步和主动提醒
7. 静息消耗、每日缺口、基础预算以及每条食物和运动数值都必须预览确认后再写入
8. 用户明确提供的数值也不能跳过确认，确认时同时展示写入后的预算变化

详细计划：[PHASE_6_ENERGY_BUDGET.md](PHASE_6_ENERGY_BUDGET.md)。
