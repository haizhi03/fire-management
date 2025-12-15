-- 修复权限问题
-- 为系统管理员角色添加 facility:view 权限

USE fire_management;

-- 更新系统管理员角色权限
UPDATE sys_role 
SET permissions = 'user:manage,facility:manage,facility:view,audit:manage,stats:view,system:config'
WHERE role_id = 1;

-- 验证更新
SELECT role_id, role_name, permissions 
FROM sys_role 
WHERE role_id = 1;

-- 说明：
-- 执行此脚本后，需要重新登录才能生效
-- 或者清除浏览器的 localStorage 中的 token
