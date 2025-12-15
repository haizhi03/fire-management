# 安全配置说明

## 1. CORS 跨域配置

系统已配置 CORS（跨域资源共享），允许前端应用访问后端 API。

### 配置位置
- `SecurityConfig.corsConfigurationSource()`

### 配置内容
- 允许的源：所有源（生产环境应配置具体域名）
- 允许的方法：GET, POST, PUT, DELETE, OPTIONS
- 允许的请求头：所有
- 允许携带凭证：是
- 预检请求有效期：3600秒

### 生产环境建议
在生产环境中，应该将 `setAllowedOriginPatterns(Arrays.asList("*"))` 修改为具体的前端域名：
```java
configuration.setAllowedOrigins(Arrays.asList(
    "https://your-domain.com",
    "https://admin.your-domain.com"
));
```

## 2. XSS 防护

系统实现了两层 XSS 防护：

### 2.1 HTTP 安全头
在 `SecurityConfig.filterChain()` 中配置：
- X-XSS-Protection：启用浏览器内置的 XSS 过滤器
- X-Content-Type-Options：防止 MIME 类型嗅探
- X-Frame-Options：防止点击劫持攻击

### 2.2 输入过滤
- `XssFilter`：全局过滤器，拦截所有请求
- `XssHttpServletRequestWrapper`：对请求参数进行 HTML 转义

所有用户输入都会经过 HTML 转义处理，防止恶意脚本注入。

## 3. SQL 注入防护

系统使用 MyBatis-Plus 框架，所有数据库查询都使用参数化查询，自动防止 SQL 注入。

### 安全实践
- ✅ 使用 `LambdaQueryWrapper` 构建查询条件
- ✅ 使用 `#{param}` 参数占位符
- ❌ 避免使用字符串拼接构建 SQL
- ❌ 避免使用 `${param}` 直接替换

### 示例
```java
// 安全的查询方式
LambdaQueryWrapper<FireFacility> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(FireFacility::getFacilityName, facilityName);
facilityMapper.selectList(wrapper);

// 不安全的方式（避免使用）
// String sql = "SELECT * FROM fire_facility WHERE facility_name = '" + facilityName + "'";
```

## 4. HTTPS 配置

### 开发环境
开发环境默认使用 HTTP 协议（端口 8080）。

### 生产环境配置

#### 4.1 生成 SSL 证书
使用 Let's Encrypt 或购买商业证书。

#### 4.2 配置 Spring Boot
在 `application.yml` 中添加：
```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: your-password
    key-store-type: PKCS12
    key-alias: tomcat
```

#### 4.3 使用 Nginx 反向代理（推荐）
```nginx
server {
    listen 443 ssl;
    server_name your-domain.com;
    
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# HTTP 重定向到 HTTPS
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

## 5. 认证与授权

### JWT 令牌
- 使用 JWT 实现无状态认证
- 令牌有效期：移动端 7 天，Web 端 30 分钟
- 密码使用 BCrypt 加密存储

### 权限控制
- 基于角色的访问控制（RBAC）
- 使用 `@PreAuthorize` 注解控制方法级权限

## 6. 其他安全措施

### 6.1 文件上传安全
- 限制文件大小：单个文件 10MB，请求总大小 20MB
- 验证文件类型：只允许上传图片文件
- 文件名随机化：使用 UUID 生成文件名

### 6.2 密码策略
- 使用 BCrypt 加密存储
- 建议：要求密码长度至少 8 位，包含字母和数字

### 6.3 日志审计
- 记录所有关键操作（登录、数据修改、审核等）
- 日志包含：操作人、操作时间、操作内容、IP 地址

## 7. 安全检查清单

部署前请确认：

- [ ] 修改默认数据库密码
- [ ] 修改 JWT 密钥（`jwt.secret`）
- [ ] 配置具体的 CORS 允许源
- [ ] 启用 HTTPS
- [ ] 配置防火墙规则
- [ ] 定期更新依赖包
- [ ] 配置日志监控和告警
- [ ] 定期备份数据库
