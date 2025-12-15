# 数据库安装说明

## 1. 环境要求

- MySQL 8.0+
- 字符集：utf8mb4
- 排序规则：utf8mb4_unicode_ci

## 2. 安装步骤

### 2.1 创建数据库

```sql
CREATE DATABASE fire_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2.2 执行表结构脚本

```bash
mysql -u root -p fire_management < src/main/resources/sql/schema.sql
```

或在MySQL客户端中执行：

```sql
USE fire_management;
SOURCE /path/to/backend/src/main/resources/sql/schema.sql;
```

### 2.3 执行初始数据脚本

```bash
mysql -u root -p fire_management < src/main/resources/sql/data.sql
```

或在MySQL客户端中执行：

```sql
USE fire_management;
SOURCE /path/to/backend/src/main/resources/sql/data.sql;
```

## 3. 数据表说明

### 3.1 用户与权限表

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| sys_role | 角色表 | role_id, role_name, permissions |
| sys_user | 用户表 | user_id, username, password, role_id |

### 3.2 消防设施表

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| fire_facility | 消防设施表 | facility_id, facility_name, longitude, latitude, status |
| fire_facility_photo | 设施照片表 | photo_id, facility_id, photo_url |

### 3.3 审核与日志表

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| audit_record | 审核记录表 | record_id, facility_id, auditor_id, audit_result |
| operation_log | 操作日志表 | log_id, user_id, operation_type, operation_content |
| offline_operation_log | 离线操作日志表 | log_id, user_id, sync_status |

## 4. 初始账号

系统初始化后会创建以下测试账号（密码均为：123456）：

| 用户名 | 角色 | 说明 |
|--------|------|------|
| admin | 系统管理员 | 拥有所有权限 |
| collector01 | 数据采集员 | 张三 |
| collector02 | 数据采集员 | 李四 |
| firefighter01 | 消防战士 | 王五 |
| firefighter02 | 消防战士 | 赵六 |
| auditor01 | 数据审核员 | 刘七 |

## 5. 索引说明

系统已为以下字段创建索引以优化查询性能：

- **位置索引**：fire_facility表的(longitude, latitude)
- **状态索引**：fire_facility表的audit_status、status
- **用户索引**：sys_user表的username
- **时间索引**：operation_log表的create_time

## 6. 注意事项

1. **字符集**：确保数据库和表都使用utf8mb4字符集，以支持emoji和特殊字符
2. **外键约束**：表之间设置了外键约束，删除数据时注意级联关系
3. **软删除**：sys_user和fire_facility表使用软删除机制（deleted字段）
4. **时间戳**：所有时间字段使用DATETIME类型，时区为Asia/Shanghai
5. **密码加密**：用户密码使用BCrypt算法加密存储

## 7. 备份建议

建议定期备份数据库：

```bash
# 全量备份
mysqldump -u root -p fire_management > fire_management_backup_$(date +%Y%m%d).sql

# 仅备份表结构
mysqldump -u root -p --no-data fire_management > fire_management_schema.sql

# 仅备份数据
mysqldump -u root -p --no-create-info fire_management > fire_management_data.sql
```

## 8. 性能优化建议

1. 定期分析表：`ANALYZE TABLE fire_facility;`
2. 定期优化表：`OPTIMIZE TABLE fire_facility;`
3. 监控慢查询日志
4. 根据实际使用情况调整索引
