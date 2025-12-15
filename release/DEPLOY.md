# 消防设施管理系统 - 部署包

## 版本信息
- 打包时间：周六022512_121839
- 版本号：1.0.0

## 目录结构
```
release/
├── backend/                    # 后端服务
│   ├── fire-management-system-1.0.0.jar
│   └── application.yml         # 配置文件
├── frontend/                   # 前端静态文件
├── sql/                        # 数据库脚本
│   ├── schema.sql             # 表结构
└── docs/                       # 文档
    ├── README.md
    ├── QUICKSTART.md
    ├── DATABASE.md
    ├── SECURITY.md
    └── PERFORMANCE.md
```

## 快速部署

### 1. 环境要求
- Java 11+
- MySQL 8.0+
- Nginx (可选)

### 2. 数据库初始化
```bash
# 方法1：使用一键初始化脚本
mysql -u root -p --default-character-set=utf8mb4 < sql/INIT_ALL.sql

# 方法2：分步执行
mysql -u root -p --default-character-set=utf8mb4 < sql/schema.sql
mysql -u root -p --default-character-set=utf8mb4 fire_management < sql/data.sql
```

### 3. 配置后端
编辑 `backend/application.yml`，修改数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fire_management
    username: root
    password: 你的密码
```

### 4. 启动后端服务
```bash
cd backend
java -jar fire-management-system-1.0.0.jar
```

或者使用后台运行：
```bash
nohup java -jar fire-management-system-1.0.0.jar > app.log 2>&1 &
```

### 5. 部署前端

#### 方法1：使用 Nginx (推荐)
将 `frontend/dist` 目录复制到 Nginx 的 html 目录，配置示例：
```nginx
server {
    listen 80;
    server_name your-domain.com;
ECHO is off.
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }
ECHO is off.
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

#### 方法2：直接访问
http://localhost:8080

### 6. 访问系统
- 前端地址：http://localhost:80 (Nginx) 或 http://localhost:8080
- 后端API：http://localhost:8080/api

### 7. 测试账号
- 管理员：admin / 123456
- 采集员：collector01 / 123456
- 审核员：auditor01 / 123456

## 生产环境建议
1. 修改 JWT 密钥 (application.yml 中的 jwt.secret)
2. 修改数据库密码
4. 配置防火墙规则

## 故障排查
详见 docs/ 目录下的文档

## 技术支持
