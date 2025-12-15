# 消防设施管理系统

基于SpringBoot + Vue 3 + MySQL的消防设施管理系统，支持Web管理端和移动端。

## 项目结构

```
fire-management-system/
├── backend/                 # 后端项目（Spring Boot）
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/fire/management/
│   │   │   │       ├── controller/      # 控制器
│   │   │   │       ├── service/         # 服务层
│   │   │   │       ├── mapper/          # 数据访问层
│   │   │   │       ├── entity/          # 实体类
│   │   │   │       ├── dto/             # 数据传输对象
│   │   │   │       ├── vo/              # 视图对象
│   │   │   │       ├── config/          # 配置类
│   │   │   │       ├── common/          # 通用类
│   │   │   │       └── utils/           # 工具类
│   │   │   └── resources/
│   │   │       ├── mapper/              # MyBatis XML
│   │   │       └── application.yml      # 配置文件
│   │   └── test/                        # 测试代码
│   └── pom.xml
├── web-admin/               # Web管理端（Vue 3）
│   ├── src/
│   │   ├── views/           # 页面组件
│   │   ├── components/      # 通用组件
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # 状态管理
│   │   ├── utils/           # 工具函数
│   │   ├── api/             # API接口
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
├── mobile-app/              # 移动端（Vue 3 + Capacitor）
│   ├── src/
│   │   ├── views/           # 页面组件
│   │   ├── components/      # 通用组件
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # 状态管理
│   │   ├── utils/           # 工具函数
│   │   ├── api/             # API接口
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   ├── vite.config.js
│   └── capacitor.config.json
└── README.md
```

## 技术栈

### 后端
- Spring Boot 2.7.18
- Spring Security
- MyBatis Plus 3.5.3.1
- MySQL 8.0
- Redis 6.0
- JWT
- Lombok

### Web管理端
- Vue 3
- Vite
- Element Plus
- Vue Router
- Pinia
- Axios
- ECharts
- Leaflet

### 移动端
- Vue 3
- Vite
- Vant UI
- Vue Router
- Pinia
- Axios
- Capacitor
- Leaflet

## 快速开始

### 1. 数据库初始化

创建数据库：
```sql
CREATE DATABASE fire_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

执行数据库初始化脚本（见下一个任务）

### 2. 后端启动

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

### 3. Web管理端启动

```bash
cd web-admin
npm install
npm run dev
```

Web管理端将在 http://localhost:3000 启动

### 4. 移动端启动

```bash
cd mobile-app
npm install
npm run dev
```

移动端将在 http://localhost:3001 启动

## 配置说明

### 后端配置

修改 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fire_management
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
```

### 文件上传路径

修改 `application.yml` 中的文件上传路径：

```yaml
file:
  upload-path: /your/upload/path/
  access-url: http://your-domain/files/
```

## 功能模块

- ✅ 用户认证与权限管理
- ✅ 消防设施数据采集
- ✅ 地图可视化与设施查询
- ✅ 数据审核与去重处理
- ✅ 统计分析与看板展示
- ✅ 离线数据同步
- ✅ 文件上传与管理

## 开发规范

- 后端遵循RESTful API设计规范
- 前端使用Vue 3 Composition API
- 代码注释清晰，命名规范
- 提交前进行代码格式化

## 许可证

MIT License
