-- 数据诊断脚本
-- 用于检查数据是否正确插入

USE fire_management;

-- 1. 检查设施数据
SELECT '=== 设施数据检查 ===' as '';
SELECT 
    COUNT(*) as 设施总数,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as 正常,
    SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as 损坏,
    SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as 维修中,
    SUM(CASE WHEN audit_status = 0 THEN 1 ELSE 0 END) as 待审核,
    SUM(CASE WHEN audit_status = 1 THEN 1 ELSE 0 END) as 已通过
FROM fire_facility 
WHERE deleted = 0;

-- 2. 检查权限配置
SELECT '=== 权限配置检查 ===' as '';
SELECT role_id, role_name, permissions 
FROM sys_role 
WHERE role_id = 1;

-- 3. 检查用户配置
SELECT '=== 用户配置检查 ===' as '';
SELECT u.user_id, u.username, u.real_name, r.role_name, r.permissions
FROM sys_user u
JOIN sys_role r ON u.role_id = r.role_id
WHERE u.username = 'admin';

-- 4. 列出最近的设施
SELECT '=== 最近的设施 ===' as '';
SELECT facility_id, facility_name, status, audit_status, address
FROM fire_facility
WHERE deleted = 0
ORDER BY facility_id DESC
LIMIT 5;

-- 5. 检查操作日志
SELECT '=== 最近的操作日志 ===' as '';
SELECT log_id, user_id, operation_type, operation_content, create_time
FROM operation_log
ORDER BY log_id DESC
LIMIT 5;
