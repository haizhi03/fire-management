# 消防设施管理系统

基于 Spring Boot + Vue 3 + MySQL 的消防设施管理系统，支持 Web 管理端和移动端。

## 📋 功能特性

- ✅ 用户认证与权限管理
- ✅ 消防设施数据采集
- ✅ 地图可视化与设施查询
- ✅ 数据审核与去重处理
- ✅ 统计分析与看板展示
- ✅ 离线数据同步
- ✅ 文件上传与管理

## 🚀 快速开始

### 1. 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+

### 2. 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE fire_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 执行初始化脚本（包含表结构+测试数据）
mysql -u root -p --default-character-set=utf8mb4 < backend/INIT_ALL.sql
```

### 3. 启动后端

```bash
cd backend
# 修改 src/main/resources/application.yml 中的数据库密码
mvn clean install
mvn spring-boot:run
```

后端服务：http://localhost:8080

### 4. 启动前端

```bash
cd web-admin
npm install
npm run dev
```

Web 管理端：http://localhost:3000

### 5. 登录系统

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 系统管理员 |
| collector01 | 123456 | 数据采集员 |
| auditor01 | 123456 | 数据审核员 |

## 📦 打包部署

### 打包部署版本

```bash
# 运行打包脚本
build-release.bat

# 生成的文件在 release/ 目录
# 包含：后端JAR + 前端静态文件 + 数据库脚本 + 文档
```

### 打包源代码

```bash
# 运行源码打包脚本
create-source-zip.bat

# 自动排除：.kiro, node_modules, target, dist, .git 等
```

## 📚 文档

- **开发指南.md** - 开发、测试、API文档
- **部署指南.md** - 生产环境部署和运维

## 🛠 技术栈

**后端：** Spring Boot 2.7 + MyBatis Plus + MySQL + Redis + JWT

**前端：** Vue 3 + Vite + Element Plus + ECharts + Leaflet

**移动端：** Vue 3 + Vant + Capacitor

## 📁 项目结构

```
fire-management-system/
├── backend/                 # 后端（Spring Boot）
│   ├── src/main/java/      # Java 源码
│   ├── src/main/resources/ # 配置文件
│   ├── INIT_ALL.sql        # 数据库初始化脚本
│   └── pom.xml
├── web-admin/              # Web 管理端（Vue 3）
│   ├── src/                # 源码
│   ├── package.json
│   └── vite.config.js
├── mobile-app/             # 移动端（Vue 3 + Capacitor）
├── build-release.bat       # 打包部署版本
├── create-source-zip.bat   # 打包源代码
├── README.md               # 本文件
├── 开发指南.md             # 开发文档
└── 部署指南.md             # 部署文档
```

## 📄 许可证

MIT License
