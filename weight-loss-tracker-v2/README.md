# 减肥追踪系统 V2

这是减肥追踪系统的重写版 demo。目标不是修复旧项目，而是保留产品想法，重新做一个结构清晰、能联调、方便继续扩展的最小版本。

## 当前阶段

Phase 3 已经完成：后端和前端都可以本地运行，前端通过真实 API 展示仪表盘、食物记录、运动记录和目标资料，并已整理成路由、页面组件、表单组件和通用组件结构。

- 产品规格：[docs/SPEC.md](docs/SPEC.md)
- 开发计划：[docs/PLAN.md](docs/PLAN.md)
- API 契约：[docs/API.md](docs/API.md)

## 建议技术方向

- 后端：Spring Boot 4 + Java 17 target，已在本机 Java 21 环境验证
- 前端：React + Vite + TypeScript + React Router
- 数据库：H2 文件数据库，接口稳定后再考虑切 MySQL
- 架构：先做单用户 demo，后续再扩展登录、多用户和 AI 功能

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

同时已完成浏览器联调：前端通过 Vite proxy 调用后端汇总接口，新增食物记录后仪表盘汇总会实时变化。Phase 4 开始加入体重记录和周期报表，前端支持 `/`、`/food`、`/exercise`、`/weight`、`/reports`、`/profile` 六个路由。

## MVP 目标

1. 记录每日食物摄入
2. 记录每日运动消耗
3. 查看今日和近期热量汇总
4. 配置基础用户目标
