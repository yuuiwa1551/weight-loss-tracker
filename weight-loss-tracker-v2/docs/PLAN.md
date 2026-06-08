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
- web：React + Vite + TypeScript
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

## 阶段 2：前端最小可运行版本

目标：前端能调用后端真实接口，不使用假数据掩盖错误。

任务：

1. 创建 Vite React TypeScript 项目
2. 建立 API client
3. 建立页面结构
   - Dashboard
   - Food Records
   - Exercise Records
   - Profile
4. 接入真实后端接口
5. 做基础加载、错误和空状态

完成标准：

- 新增食物后，Dashboard 汇总变化
- 新增运动后，Dashboard 汇总变化
- Profile 修改后，目标热量影响汇总

## 阶段 3：体验和可维护性整理

任务：

1. 提取表单组件
2. 提取统计卡片组件
3. 增加路由
4. 统一错误提示
5. 补充 README 启动文档
6. 加入基本 lint/build 检查

## 阶段 4：后续扩展

可以考虑：

1. 登录注册和多用户
2. MySQL 持久化部署
3. AI 食物营养估算
4. 体重记录和趋势图
5. 周/月报表
6. 移动端适配增强

## 下一轮建议动作

下一步建议进入阶段 2，创建 `web` 前端并按 [API.md](API.md) 对接真实接口。

具体顺序：

1. 创建 Vite React TypeScript 项目
2. 建立 API client 和类型
3. 先做 Dashboard，对接 `GET /api/summaries/daily` 和 `GET /api/summaries/recent`
4. 再做食物、运动、Profile 页面
5. 每做完一个页面就跑一次构建和简单联调
