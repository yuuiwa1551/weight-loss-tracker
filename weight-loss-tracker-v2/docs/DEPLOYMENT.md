# 本地部署

## 默认 H2 模式

部署文件位于 `deploy/compose.yml`。服务只绑定宿主机回环地址，同时加入现有 AstrBot Docker 网络：

```powershell
docker network inspect astrbot_astrbot_network
docker compose -f deploy/compose.yml up -d --build
docker compose -f deploy/compose.yml ps
```

- Web 与 API：`http://127.0.0.1:8080`
- 健康检查：`http://127.0.0.1:8080/actuator/health`
- AstrBot 容器内地址：`http://weight-loss-tracker:8080`
- H2 宿主目录：`runtime-data/h2`

`runtime-data` 已被 Git 忽略。容器重建不会删除 H2 文件，只有明确删除宿主目录才会丢失数据。

## H2 备份

备份脚本会检查服务是否正在运行；若正在运行，只停止减肥追踪容器，复制完整 H2 目录后恢复服务。AstrBot 和 NapCat 不受影响。

```powershell
.\scripts\backup-h2.ps1
```

默认备份到 `backups/h2/<时间戳>`。可通过 `-BackupRoot D:\Backups\weight-loss-tracker` 指定其他目录。

## 开发模式 H2 Console

H2 Console 在默认和 Docker profile 中关闭，只在显式 `dev` profile 中开启：

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

## 可选 MySQL profile

MySQL 不进入默认启动路径。先从 `.env.example` 创建本机 `.env`，然后设置：

```dotenv
SPRING_PROFILES_ACTIVE=docker,mysql
DB_URL=jdbc:mysql://host.docker.internal:3306/weight_loss_tracker?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
DB_USERNAME=weight_loss_tracker
DB_PASSWORD=replace-me
```

再执行 `docker compose -f deploy/compose.yml up -d --build`。仓库不包含 MySQL 密码，也不会自动创建或删除 MySQL 实例。
