# 减肥追踪系统 V2

这是减肥追踪系统的重写版 demo。目标不是修复旧项目，而是保留产品想法，重新做一个结构清晰、能联调、方便继续扩展的最小版本。

## 当前阶段

Phase 1 后端已经完成：包含 Spring Boot 后端骨架、H2 数据库、基础数据模型、接口校验、统一响应、异常处理、种子数据和接口测试。

- 产品规格：[docs/SPEC.md](docs/SPEC.md)
- 开发计划：[docs/PLAN.md](docs/PLAN.md)
- API 契约：[docs/API.md](docs/API.md)

## 建议技术方向

- 后端：Spring Boot 4 + Java 17 target，已在本机 Java 21 环境验证
- 前端：React + Vite + TypeScript
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

## MVP 目标

1. 记录每日食物摄入
2. 记录每日运动消耗
3. 查看今日和近期热量汇总
4. 配置基础用户目标
