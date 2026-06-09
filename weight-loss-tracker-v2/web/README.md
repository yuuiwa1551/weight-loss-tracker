# 减肥追踪系统 V2 Web

React + Vite + TypeScript 前端，用于对接 V2 后端 API。

## 功能

- 仪表盘：展示每日热量、营养汇总、目标状态和最近 7 天趋势
- 食物记录：新增、查看和删除当天食物记录
- 运动记录：新增、查看和删除当天运动记录
- 体重趋势：新增体重记录，查看最近 30 天变化
- 周期报表：查看 7 天和 30 天热量、目标达成和体重变化
- 目标资料：查看和更新用户基础目标
- 路由：`/`、`/food`、`/exercise`、`/weight`、`/reports`、`/profile`

## 本地运行

先启动后端：

```powershell
cd ..\backend
.\mvnw.cmd spring-boot:run
```

再启动前端：

```powershell
cd ..\web
npm install
npm run dev
```

前端地址：`http://127.0.0.1:5173/`

开发服务器会把 `/api` 代理到 `http://localhost:8080`。

## 检查命令

```powershell
npm run check
```

`check` 会依次执行 ESLint 和生产构建。
