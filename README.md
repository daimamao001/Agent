
# 轻量级 Chat 聊天式交互界面。

## 技术栈
- 后端: Java (Spring 生态)
- 前端: Vue 3 + Vite
- 数据库: MySQL
- 缓存: Redis
- 容器化: Docker / Docker Compose

## 目录结构
```
├── Back-end/       # Java 后端服务
├── Front-end/      # Vue 前端应用
├── Database/       # MySQL 初始化脚本
├── docker-compose.yml
```

## 快速开始（Docker 一键部署）
1) 确保已安装 Docker 与 Docker Compose。
2) 克隆仓库后在项目根目录执行：
	docker compose  up -d --build
3) 首次启动会构建镜像并拉起前端、后端、数据库等服务。

## 访问入口（默认端口）
- 前端: http://localhost:81
- 后端 API: http://localhost:8082
- phpMyAdmin: http://localhost:8083
