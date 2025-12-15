-- 消防设施管理系统初始数据脚本

USE fire_management;

-- ============================================
-- 初始化角色数据
-- ============================================

INSERT INTO sys_role (role_name, permissions) VALUES
('系统管理员', 'user:manage,facility:manage,facility:view,audit:manage,stats:view,system:config'),
('数据采集员', 'facility:collect,facility:view'),
('消防战士', 'facility:view,facility:navigate'),
('数据审核员', 'audit:manage,facility:view');

-- ============================================
-- 初始化用户数据
-- 密码均为：123456（BCrypt加密后）
-- ============================================

-- 系统管理员账号
INSERT INTO sys_user (username, password, real_name, role_id, phone, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 1, '13800138000', 1);

-- 数据采集员账号
INSERT INTO sys_user (username, password, real_name, role_id, phone, status) VALUES
('collector01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', 2, '13800138001', 1),
('collector02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', 2, '13800138002', 1);

-- 消防战士账号
INSERT INTO sys_user (username, password, real_name, role_id, phone, status) VALUES
('firefighter01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王五', 3, '13800138003', 1),
('firefighter02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '赵六', 3, '13800138004', 1);

-- 数据审核员账号
INSERT INTO sys_user (username, password, real_name, role_id, phone, status) VALUES
('auditor01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '刘七', 4, '13800138005', 1);

-- ============================================
-- 初始化测试消防设施数据（中山市区域）
-- ============================================

-- 消防栓数据
INSERT INTO fire_facility (facility_name, facility_type, longitude, latitude, address, status, pressure, collector_id, audit_status) VALUES
('中山大道消防栓-001', 1, 113.392600, 22.517400, '中山市石岐区中山大道123号', 1, 0.80, 2, 1),
('民生路消防栓-001', 1, 113.396000, 22.501500, '中山市石岐区民生路45号', 1, 0.75, 2, 1),
('兴中道消防栓-001', 1, 113.389500, 22.520100, '中山市石岐区兴中道88号', 1, 0.85, 3, 1),
('孙文西路消防栓-001', 1, 113.385200, 22.516800, '中山市石岐区孙文西路156号', 2, 0.60, 2, 1),
('博爱路消防栓-001', 1, 113.391800, 22.513200, '中山市石岐区博爱路67号', 1, 0.78, 3, 1);

-- 为每个消防栓添加照片
INSERT INTO fire_facility_photo (facility_id, photo_url, photo_desc) VALUES
(1, '/uploads/hydrant_001_main.jpg', '正面照'),
(1, '/uploads/hydrant_001_pressure.jpg', '压力表特写'),
(2, '/uploads/hydrant_002_main.jpg', '正面照'),
(2, '/uploads/hydrant_002_pressure.jpg', '压力表特写'),
(3, '/uploads/hydrant_003_main.jpg', '正面照'),
(4, '/uploads/hydrant_004_main.jpg', '正面照'),
(4, '/uploads/hydrant_004_damage.jpg', '损坏部位'),
(5, '/uploads/hydrant_005_main.jpg', '正面照');

-- ============================================
-- 初始化审核记录
-- ============================================

INSERT INTO audit_record (facility_id, auditor_id, audit_result, audit_time) VALUES
(1, 6, 1, '2024-01-15 10:30:00'),
(2, 6, 1, '2024-01-15 11:00:00'),
(3, 6, 1, '2024-01-16 09:15:00'),
(4, 6, 1, '2024-01-16 14:20:00'),
(5, 6, 1, '2024-01-17 10:45:00');

-- ============================================
-- 初始化操作日志
-- ============================================

INSERT INTO operation_log (user_id, operation_type, operation_content, ip_address) VALUES
(1, 'LOGIN', '系统管理员登录', '127.0.0.1'),
(2, 'FACILITY_CREATE', '创建消防栓：中山大道消防栓-001', '192.168.1.100'),
(2, 'FACILITY_CREATE', '创建消防栓：民生路消防栓-001', '192.168.1.100'),
(6, 'AUDIT_APPROVE', '审核通过：中山大道消防栓-001', '192.168.1.101'),
(6, 'AUDIT_APPROVE', '审核通过：民生路消防栓-001', '192.168.1.101');
