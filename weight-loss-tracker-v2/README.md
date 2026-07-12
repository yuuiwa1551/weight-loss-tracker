# 减肥追踪系统 V2

这是减肥追踪系统的重写版 demo。目标不是修复旧项目，而是保留产品想法，重新做一个结构清晰、能联调、方便继续扩展的演示版本。

## 当前阶段

Phase 5 已完成：后端和 Web 支持 QQ 多用户隔离，AstrBot 插件已部署记录、查询、估算确认和撤销工具。本地 Docker 服务将 Web、API 和 H2 持久化合并部署，并已加入现有 AstrBot 网络。

Phase 6 正在实施，前三阶段已完成：后端已有能量资料、`P6_V1` 计算服务、计划预览/确认、活动计划快照和实时每日预算 API。食物和运动的后端强制预览确认、Web 和 AstrBot 接入仍在后续阶段。

- 产品规格：[docs/SPEC.md](docs/SPEC.md)
- 开发计划：[docs/PLAN.md](docs/PLAN.md)
- API 契约：[docs/API.md](docs/API.md)
- 本地部署：[docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)

## 建议技术方向

- 后端：Spring Boot 4 + Java 17 target，已在本机 Java 21 环境验证
- 前端：React + Vite + TypeScript + React Router
- 数据库：H2 文件数据库，接口稳定后再考虑切 MySQL
- 架构：本机多用户数据服务，不建设传统登录页面；由 AstrBot LLM tools 提供聊天入口

## 当前功能

1. 用户选择：在本机页面切换 Local 或 AstrBot QQ 用户
2. 仪表盘：查看每日摄入、消耗、净热量、目标差和最近 7 天趋势
3. 食物记录：新增、查看和删除当天食物记录
4. 运动记录：新增、查看和删除当天运动记录
5. 体重趋势：新增、查看和删除体重记录，展示最近 30 天体重变化
6. 周期报表：查看 7 天周报和 30 天月报，包含热量、目标达成、营养均值和体重变化
7. 目标资料：按用户维护昵称、身高、当前体重、目标体重和每日热量目标
8. 能量计划 API：预览并确认静息消耗、基础日常消耗、每日缺口和摄入预算，实时查询当天剩余额度

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

默认启动只会初始化一条用户目标资料，不会自动写入食物、运动或体重样例记录。需要演示样例数据时，可以临时启用：

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--app.demo-data.sample-records-enabled=true"
```

## 前端运行

```powershell
cd web
npm install
npm run check
npm run dev
```

前端默认运行在 `http://127.0.0.1:5173/`，开发模式下 `/api` 会代理到 `http://localhost:8080`。

## Docker 常驻运行

```powershell
docker compose -f deploy/compose.yml up -d --build
```

Web 和 API 统一位于 `http://127.0.0.1:8080`。H2 文件保存在 Git 忽略的 `runtime-data/h2`，AstrBot 插件通过 `http://weight-loss-tracker:8080` 访问后端。备份与可选 MySQL 配置见 [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)。

## 已验证

```powershell
cd backend
.\mvnw.cmd test

cd ..\web
npm run check
```

`npm run check` 会依次执行 ESLint、Vitest、TypeScript 和生产构建。前端通过 Vite proxy 调用后端接口；新增食物记录后仪表盘汇总会实时变化；新增体重记录后资料、趋势和报表会更新。前端支持 `/`、`/food`、`/exercise`、`/weight`、`/reports`、`/profile` 六个路由。

## 已实现目标

1. 记录每日食物摄入
2. 记录每日运动消耗
3. 查看今日和近期热量汇总
4. 配置基础用户目标
5. 记录体重并查看 30 天趋势
6. 查看 7 天和 30 天周期报表
