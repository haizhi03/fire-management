# 快速启动指南

## ⚠️ 地图功能配置（重要）

在启动前端之前，需要配置高德地图API Key：

1. 访问 https://lbs.amap.com/ 注册并获取API Key
2. 打开 `web-admin/src/views/Map.vue`
3. 将第85行的 `YOUR_AMAP_KEY` 替换为你的Key
4. 详细说明请查看 `web-admin/MAP_SETUP.md`

---

## 前置要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+

## 1. 数据库初始化

### 1.1 创建数据库

```bash
mysql -u root -p
```

```sql
CREATE DATABASE fire_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 1.2 执行SQL脚本

```bash
cd backend
mysql -u root -p fire_management < src/main/resources/sql/schema.sql
mysql -u root -p fire_management < src/main/resources/sql/data.sql
```

## 2. 启动后端服务

### 2.1 修改配置

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fire_management
    username: root
    password: your_password  # 修改为你的MySQL密码
```

### 2.2 启动服务

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

## 3. 启动Web管理端

```bash
cd web-admin
npm install
npm run dev
```

Web管理端将在 http://localhost:3000 启动

## 4. 登录系统

打开浏览器访问：http://localhost:3000

### 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 系统管理员 |
| collector01 | 123456 | 数据采集员 |
| firefighter01 | 123456 | 消防战士 |
| auditor01 | 123456 | 数据审核员 |

## 5. 功能说明

### 5.1 数据看板
- 查看设施总数、正常设施、损坏设施、待审核数量
- 实时统计数据

### 5.2 设施管理
- 查询设施列表（支持分页、筛选）
- 查看设施详情（含照片）
- 删除设施

### 5.3 数据审核
- 审核待审核的设施数据
- 通过或驳回数据

### 5.4 用户管理
- 管理系统用户
- 分配角色权限

## 6. API接口测试

### 6.1 登录接口

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456",
    "clientType": "web"
  }'
```

### 6.2 查询设施列表

```bash
curl -X GET "http://localhost:8080/api/facilities?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 6.3 查询周边设施

```bash
curl -X GET "http://localhost:8080/api/facilities/nearby?latitude=22.5174&longitude=113.3926&radius=500" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 7. 常见问题

### 7.1 后端启动失败

**问题**：数据库连接失败
**解决**：检查MySQL是否启动，配置文件中的用户名密码是否正确

### 7.2 前端启动失败

**问题**：依赖安装失败
**解决**：删除 node_modules 文件夹，重新执行 npm install

**问题**：端口被占用
**解决**：修改 vite.config.js 中的端口号

### 7.3 登录失败

**问题**：401 未授权
**解决**：检查用户名密码是否正确

**问题**：CORS跨域错误
**解决**：确保后端服务已启动，检查CORS配置

## 8. 开发建议

### 8.1 后端开发

- 使用IDE（IntelliJ IDEA推荐）打开backend项目
- 修改代码后，使用 `mvn spring-boot:run` 重启服务
- 查看日志：控制台输出

### 8.2 前端开发

- 使用VSCode打开web-admin项目
- 安装推荐插件：Volar, ESLint
- 修改代码后，Vite会自动热更新
- 查看日志：浏览器控制台

## 9. 生产部署

### 9.1 后端打包

```bash
cd backend
mvn clean package
```

生成的jar包位于：`target/fire-management-system-1.0.0.jar`

### 9.2 前端打包

```bash
cd web-admin
npm run build
```

生成的静态文件位于：`dist/` 目录

### 9.3 部署到服务器

1. 将后端jar包上传到服务器
2. 使用 `java -jar fire-management-system-1.0.0.jar` 启动
3. 将前端dist目录部署到Nginx
4. 配置Nginx反向代理到后端服务

## 10. 技术支持

如有问题，请查看：
- README.md - 项目说明
- DATABASE.md - 数据库文档
- 设计文档：.kiro/specs/fire-management-system/

祝使用愉快！🎉
