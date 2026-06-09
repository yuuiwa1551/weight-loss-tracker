# 减肥追踪系统 V2

这是减肥追踪系统的重写版 demo。目标不是修复旧项目，而是保留产品想法，重新做一个结构清晰、能联调、方便继续扩展的演示版本。

## 当前阶段

Phase 4 的第一轮扩展已经完成：后端和前端都可以本地运行，前端通过真实 API 展示仪表盘、食物记录、运动记录、体重趋势、周期报表和目标资料。当前代码已经从单文件原型整理成路由、页面组件、表单组件、通用组件和 API client 的结构。

- 产品规格：[docs/SPEC.md](docs/SPEC.md)
- 开发计划：[docs/PLAN.md](docs/PLAN.md)
- API 契约：[docs/API.md](docs/API.md)

## 建议技术方向

- 后端：Spring Boot 4 + Java 17 target，已在本机 Java 21 环境验证
- 前端：React + Vite + TypeScript + React Router
- 数据库：H2 文件数据库，接口稳定后再考虑切 MySQL
- 架构：先做单用户 demo，后续再扩展登录、多用户和 AI 功能

## 当前功能

1. 仪表盘：查看每日摄入、消耗、净热量、目标差和最近 7 天趋势
2. 食物记录：新增、查看和删除当天食物记录
3. 运动记录：新增、查看和删除当天运动记录
4. 体重趋势：新增、查看和删除体重记录，展示最近 30 天体重变化
5. 周期报表：查看 7 天周报和 30 天月报，包含热量、目标达成、营养均值和体重变化
6. 目标资料：维护昵称、身高、当前体重、目标体重和每日热量目标

## 后端运行

```powershell
cd backend
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

H2 控制台：`http://localhost:8080/h2-console`

默认连接信息：

- JDBC URL：`jdbc:h2:file:./data/weight-loss-tracker-v2`
- username：`sa`
- password：空

## 前端运行

```powershell
cd web
npm install
npm run check
npm run dev
```

前端默认运行在 `http://127.0.0.1:5173/`，开发模式下 `/api` 会代理到 `http://localhost:8080`。

## 已验证

```powershell
cd backend
.\mvnw.cmd test

cd ..\web
npm run check
```

同时已完成浏览器联调：前端通过 Vite proxy 调用后端接口；新增食物记录后仪表盘汇总会实时变化；新增体重记录后体重趋势会更新；周期报表可以展示 7 天和 30 天统计。前端支持 `/`、`/food`、`/exercise`、`/weight`、`/reports`、`/profile` 六个路由。

## 已实现目标

1. 记录每日食物摄入
2. 记录每日运动消耗
3. 查看今日和近期热量汇总
4. 配置基础用户目标
5. 记录体重并查看 30 天趋势
6. 查看 7 天和 30 天周期报表
