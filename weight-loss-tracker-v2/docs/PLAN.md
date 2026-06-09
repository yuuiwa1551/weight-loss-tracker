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

下一步建议继续从 Phase 4 里挑更偏真实产品化的扩展点。

具体顺序：

1. 增加体重趋势的折线图和更清晰的目标达成可视化
2. 增加 AI 食物营养估算，让 demo 更有差异化
3. 增加 MySQL 配置和部署说明，贴近真实运行环境
4. 增加登录注册和多用户数据隔离
5. 补充更完整的端到端测试
