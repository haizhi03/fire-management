-- ============================================
-- 消防设施管理系统 - 完整数据库脚本
-- 生成时间: 2024年
-- 说明: 包含完整的表结构和测试数据
-- 使用方法: 直接在MySQL中执行此脚本
-- ============================================

-- 删除已存在的数据库（谨慎使用）
DROP DATABASE IF EXISTS fire_management;

-- 创建数据库
CREATE DATABASE fire_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE fire_management;

-- ============================================
-- 第一部分：表结构创建
-- ============================================

-- 1. 角色表
CREATE TABLE sys_role (
    role_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(30) UNIQUE NOT NULL COMMENT '角色名称',
    permissions VARCHAR(500) COMMENT '权限标识，多个用逗号分隔',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 2. 用户表
CREATE TABLE sys_user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '登录账号',
    password VARCHAR(100) NOT NULL COMMENT '加密后的密码',
    real_name VARCHAR(20) NOT NULL COMMENT '真实姓名',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    phone VARCHAR(11) COMMENT '联系电话',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    INDEX idx_username (username),
    INDEX idx_role_id (role_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 3. 消防设施表
CREATE TABLE fire_facility (
    facility_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设施ID',
    facility_name VARCHAR(100) NOT NULL COMMENT '设施名称',
    facility_type TINYINT NOT NULL COMMENT '设施类型：1-消防栓，2-消防车',
    longitude DECIMAL(10,6) NOT NULL COMMENT '经度',
    latitude DECIMAL(10,6) NOT NULL COMMENT '纬度',
    address VARCHAR(200) NOT NULL COMMENT '详细地址',
    status TINYINT DEFAULT 1 COMMENT '设施状态：1-正常，2-损坏，3-维修中',
    pressure DECIMAL(5,2) COMMENT '工作压力(MPa)，仅消防栓',
    special_type VARCHAR(50) COMMENT '特殊类型',
    collector_id BIGINT NOT NULL COMMENT '采集人员ID',
    audit_status TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核，1-已通过，2-已驳回',
    is_merged TINYINT DEFAULT 0 COMMENT '是否已合并：0-否，1-是',
    merged_to BIGINT COMMENT '合并到的设施ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    INDEX idx_location (longitude, latitude),
    INDEX idx_audit_status (audit_status),
    INDEX idx_collector (collector_id),
    INDEX idx_facility_type (facility_type),
    INDEX idx_status (status),
    FOREIGN KEY (collector_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消防设施表';


-- 4. 设施照片表
CREATE TABLE fire_facility_photo (
    photo_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '照片ID',
    facility_id BIGINT NOT NULL COMMENT '设施ID',
    photo_url VARCHAR(500) NOT NULL COMMENT '照片URL',
    photo_desc VARCHAR(100) COMMENT '照片描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_facility (facility_id),
    FOREIGN KEY (facility_id) REFERENCES fire_facility(facility_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设施照片表';

-- 5. 审核记录表
CREATE TABLE audit_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    facility_id BIGINT NOT NULL COMMENT '设施ID',
    auditor_id BIGINT NOT NULL COMMENT '审核人ID',
    audit_result TINYINT NOT NULL COMMENT '审核结果：1-通过，2-驳回',
    reject_reason VARCHAR(200) COMMENT '驳回原因',
    audit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    INDEX idx_facility (facility_id),
    INDEX idx_auditor (auditor_id),
    INDEX idx_audit_time (audit_time),
    FOREIGN KEY (facility_id) REFERENCES fire_facility(facility_id),
    FOREIGN KEY (auditor_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录表';

-- 6. 操作日志表
CREATE TABLE operation_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '操作用户ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_content VARCHAR(500) COMMENT '操作内容',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_user (user_id),
    INDEX idx_time (create_time),
    INDEX idx_type (operation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 7. 离线操作日志表
CREATE TABLE offline_operation_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '操作人ID',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型',
    operation_content VARCHAR(500) NOT NULL COMMENT '操作内容',
    operation_time DATETIME NOT NULL COMMENT '操作时间',
    sync_status TINYINT DEFAULT 0 COMMENT '同步状态：0-未同步，1-已同步',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user (user_id),
    INDEX idx_sync_status (sync_status),
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='离线操作日志表';

-- ============================================
-- 第二部分：初始化数据
-- ============================================

-- ============================================
-- 角色数据
-- ============================================
INSERT INTO sys_role (role_id, role_name, permissions) VALUES
(1, '系统管理员', 'user:manage,facility:manage,facility:view,audit:manage,stats:view,system:config'),
(2, '数据采集员', 'facility:collect,facility:view'),
(3, '消防战士', 'facility:view,facility:navigate'),
(4, '数据审核员', 'audit:manage,facility:view');

-- ============================================
-- 用户数据
-- 所有用户密码均为：123456（BCrypt加密）
-- ============================================
INSERT INTO sys_user (user_id, username, password, real_name, role_id, phone, status) VALUES
-- 系统管理员
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 1, '13800138000', 1),
-- 数据采集员
(2, 'collector01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', 2, '13800138001', 1),
(3, 'collector02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', 2, '13800138002', 1),
(4, 'collector03', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王小明', 2, '13800138006', 1),
-- 消防战士
(5, 'firefighter01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王五', 3, '13800138003', 1),
(6, 'firefighter02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '赵六', 3, '13800138004', 1),
(7, 'firefighter03', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '陈伟', 3, '13800138007', 1),
-- 数据审核员
(8, 'auditor01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '刘七', 4, '13800138005', 1),
(9, 'auditor02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '周芳', 4, '13800138008', 1);


-- ============================================
-- 消防设施数据（中山市区域 - 丰富数据）
-- ============================================

-- 石岐区消防栓
INSERT INTO fire_facility (facility_id, facility_name, facility_type, longitude, latitude, address, status, pressure, collector_id, audit_status, create_time) VALUES
(1, '中山大道消防栓-001', 1, 113.392600, 22.517400, '中山市石岐区中山大道123号', 1, 0.80, 2, 1, '2024-01-10 09:00:00'),
(2, '民生路消防栓-001', 1, 113.396000, 22.501500, '中山市石岐区民生路45号', 1, 0.75, 2, 1, '2024-01-10 10:30:00'),
(3, '兴中道消防栓-001', 1, 113.389500, 22.520100, '中山市石岐区兴中道88号', 1, 0.85, 3, 1, '2024-01-11 08:45:00'),
(4, '孙文西路消防栓-001', 1, 113.385200, 22.516800, '中山市石岐区孙文西路156号', 2, 0.60, 2, 1, '2024-01-11 14:20:00'),
(5, '博爱路消防栓-001', 1, 113.391800, 22.513200, '中山市石岐区博爱路67号', 1, 0.78, 3, 1, '2024-01-12 09:15:00'),
(6, '悦来南路消防栓-001', 1, 113.388200, 22.509800, '中山市石岐区悦来南路22号', 1, 0.82, 2, 1, '2024-01-12 11:00:00'),
(7, '太平路消防栓-001', 1, 113.393500, 22.515600, '中山市石岐区太平路89号', 3, 0.70, 3, 1, '2024-01-13 10:30:00'),
(8, '莲塘路消防栓-001', 1, 113.397200, 22.518900, '中山市石岐区莲塘路33号', 1, 0.76, 4, 1, '2024-01-13 15:45:00'),

-- 东区消防栓
(9, '东区消防栓-001', 1, 113.412500, 22.523600, '中山市东区中山四路168号', 1, 0.88, 2, 1, '2024-01-14 08:30:00'),
(10, '东区消防栓-002', 1, 113.418300, 22.519200, '中山市东区博爱六路55号', 1, 0.79, 3, 1, '2024-01-14 10:15:00'),
(11, '东区消防栓-003', 1, 113.421600, 22.526800, '中山市东区长江路99号', 2, 0.55, 2, 1, '2024-01-15 09:00:00'),
(12, '东区消防栓-004', 1, 113.408900, 22.521400, '中山市东区起湾道128号', 1, 0.81, 4, 1, '2024-01-15 14:30:00'),

-- 西区消防栓
(13, '西区消防栓-001', 1, 113.368500, 22.512300, '中山市西区富华道66号', 1, 0.77, 2, 1, '2024-01-16 08:45:00'),
(14, '西区消防栓-002', 1, 113.372100, 22.508600, '中山市西区翠景道88号', 1, 0.83, 3, 1, '2024-01-16 11:20:00'),
(15, '西区消防栓-003', 1, 113.365800, 22.515900, '中山市西区沙朗路45号', 3, 0.65, 4, 1, '2024-01-17 09:30:00'),

-- 南区消防栓
(16, '南区消防栓-001', 1, 113.385600, 22.495200, '中山市南区城南一路77号', 1, 0.80, 2, 1, '2024-01-17 14:00:00'),
(17, '南区消防栓-002', 1, 113.391200, 22.491800, '中山市南区永安一路33号', 1, 0.74, 3, 1, '2024-01-18 08:30:00'),
(18, '南区消防栓-003', 1, 113.378900, 22.498500, '中山市南区马岭路56号', 2, 0.58, 2, 1, '2024-01-18 10:45:00'),

-- 火炬开发区消防栓
(19, '火炬区消防栓-001', 1, 113.458200, 22.538600, '中山市火炬开发区中山港大道188号', 1, 0.90, 3, 1, '2024-01-19 09:00:00'),
(20, '火炬区消防栓-002', 1, 113.465800, 22.542100, '中山市火炬开发区康乐大道99号', 1, 0.86, 4, 1, '2024-01-19 11:30:00'),
(21, '火炬区消防栓-003', 1, 113.452600, 22.535400, '中山市火炬开发区张家边大道66号', 1, 0.84, 2, 1, '2024-01-20 08:15:00'),

-- 待审核的消防栓
(22, '石岐区待审核消防栓-001', 1, 113.394800, 22.522300, '中山市石岐区康华路12号', 1, 0.78, 2, 0, '2024-01-20 14:30:00'),
(23, '东区待审核消防栓-001', 1, 113.415600, 22.528900, '中山市东区白沙湾路88号', 1, 0.82, 3, 0, '2024-01-21 09:45:00'),
(24, '西区待审核消防栓-001', 1, 113.370200, 22.518600, '中山市西区彩虹大道55号', 1, 0.75, 4, 0, '2024-01-21 11:00:00'),

-- 已驳回的消防栓
(25, '南区驳回消防栓-001', 1, 113.382500, 22.493600, '中山市南区竹苑路33号', 1, 0.70, 2, 2, '2024-01-22 08:30:00');


-- ============================================
-- 设施照片数据
-- ============================================
INSERT INTO fire_facility_photo (photo_id, facility_id, photo_url, photo_desc, create_time) VALUES
-- 石岐区设施照片
(1, 1, '/uploads/2024/01/hydrant_001_main.jpg', '正面照', '2024-01-10 09:05:00'),
(2, 1, '/uploads/2024/01/hydrant_001_pressure.jpg', '压力表特写', '2024-01-10 09:06:00'),
(3, 2, '/uploads/2024/01/hydrant_002_main.jpg', '正面照', '2024-01-10 10:35:00'),
(4, 2, '/uploads/2024/01/hydrant_002_env.jpg', '周边环境', '2024-01-10 10:36:00'),
(5, 3, '/uploads/2024/01/hydrant_003_main.jpg', '正面照', '2024-01-11 08:50:00'),
(6, 4, '/uploads/2024/01/hydrant_004_main.jpg', '正面照', '2024-01-11 14:25:00'),
(7, 4, '/uploads/2024/01/hydrant_004_damage.jpg', '损坏部位特写', '2024-01-11 14:26:00'),
(8, 5, '/uploads/2024/01/hydrant_005_main.jpg', '正面照', '2024-01-12 09:20:00'),
(9, 6, '/uploads/2024/01/hydrant_006_main.jpg', '正面照', '2024-01-12 11:05:00'),
(10, 7, '/uploads/2024/01/hydrant_007_main.jpg', '正面照', '2024-01-13 10:35:00'),
(11, 7, '/uploads/2024/01/hydrant_007_repair.jpg', '维修标识', '2024-01-13 10:36:00'),
(12, 8, '/uploads/2024/01/hydrant_008_main.jpg', '正面照', '2024-01-13 15:50:00'),

-- 东区设施照片
(13, 9, '/uploads/2024/01/hydrant_009_main.jpg', '正面照', '2024-01-14 08:35:00'),
(14, 9, '/uploads/2024/01/hydrant_009_pressure.jpg', '压力表', '2024-01-14 08:36:00'),
(15, 10, '/uploads/2024/01/hydrant_010_main.jpg', '正面照', '2024-01-14 10:20:00'),
(16, 11, '/uploads/2024/01/hydrant_011_main.jpg', '正面照', '2024-01-15 09:05:00'),
(17, 11, '/uploads/2024/01/hydrant_011_damage.jpg', '损坏情况', '2024-01-15 09:06:00'),
(18, 12, '/uploads/2024/01/hydrant_012_main.jpg', '正面照', '2024-01-15 14:35:00'),

-- 西区设施照片
(19, 13, '/uploads/2024/01/hydrant_013_main.jpg', '正面照', '2024-01-16 08:50:00'),
(20, 14, '/uploads/2024/01/hydrant_014_main.jpg', '正面照', '2024-01-16 11:25:00'),
(21, 15, '/uploads/2024/01/hydrant_015_main.jpg', '正面照', '2024-01-17 09:35:00'),
(22, 15, '/uploads/2024/01/hydrant_015_repair.jpg', '维修中标识', '2024-01-17 09:36:00'),

-- 南区设施照片
(23, 16, '/uploads/2024/01/hydrant_016_main.jpg', '正面照', '2024-01-17 14:05:00'),
(24, 17, '/uploads/2024/01/hydrant_017_main.jpg', '正面照', '2024-01-18 08:35:00'),
(25, 18, '/uploads/2024/01/hydrant_018_main.jpg', '正面照', '2024-01-18 10:50:00'),
(26, 18, '/uploads/2024/01/hydrant_018_damage.jpg', '损坏部位', '2024-01-18 10:51:00'),

-- 火炬区设施照片
(27, 19, '/uploads/2024/01/hydrant_019_main.jpg', '正面照', '2024-01-19 09:05:00'),
(28, 19, '/uploads/2024/01/hydrant_019_pressure.jpg', '压力表', '2024-01-19 09:06:00'),
(29, 20, '/uploads/2024/01/hydrant_020_main.jpg', '正面照', '2024-01-19 11:35:00'),
(30, 21, '/uploads/2024/01/hydrant_021_main.jpg', '正面照', '2024-01-20 08:20:00'),

-- 待审核设施照片
(31, 22, '/uploads/2024/01/hydrant_022_main.jpg', '正面照', '2024-01-20 14:35:00'),
(32, 23, '/uploads/2024/01/hydrant_023_main.jpg', '正面照', '2024-01-21 09:50:00'),
(33, 24, '/uploads/2024/01/hydrant_024_main.jpg', '正面照', '2024-01-21 11:05:00'),
(34, 25, '/uploads/2024/01/hydrant_025_main.jpg', '正面照', '2024-01-22 08:35:00');

-- ============================================
-- 审核记录数据
-- ============================================
INSERT INTO audit_record (record_id, facility_id, auditor_id, audit_result, reject_reason, audit_time) VALUES
-- 已通过的审核记录
(1, 1, 8, 1, NULL, '2024-01-10 15:30:00'),
(2, 2, 8, 1, NULL, '2024-01-10 16:00:00'),
(3, 3, 8, 1, NULL, '2024-01-11 14:15:00'),
(4, 4, 8, 1, NULL, '2024-01-11 17:20:00'),
(5, 5, 8, 1, NULL, '2024-01-12 14:45:00'),
(6, 6, 9, 1, NULL, '2024-01-12 16:30:00'),
(7, 7, 9, 1, NULL, '2024-01-13 15:00:00'),
(8, 8, 8, 1, NULL, '2024-01-13 17:45:00'),
(9, 9, 9, 1, NULL, '2024-01-14 14:30:00'),
(10, 10, 8, 1, NULL, '2024-01-14 16:15:00'),
(11, 11, 9, 1, NULL, '2024-01-15 14:00:00'),
(12, 12, 8, 1, NULL, '2024-01-15 17:30:00'),
(13, 13, 9, 1, NULL, '2024-01-16 14:45:00'),
(14, 14, 8, 1, NULL, '2024-01-16 16:20:00'),
(15, 15, 9, 1, NULL, '2024-01-17 14:30:00'),
(16, 16, 8, 1, NULL, '2024-01-17 17:00:00'),
(17, 17, 9, 1, NULL, '2024-01-18 14:30:00'),
(18, 18, 8, 1, NULL, '2024-01-18 16:45:00'),
(19, 19, 9, 1, NULL, '2024-01-19 14:00:00'),
(20, 20, 8, 1, NULL, '2024-01-19 16:30:00'),
(21, 21, 9, 1, NULL, '2024-01-20 14:15:00'),
-- 驳回的审核记录
(22, 25, 8, 2, '照片模糊，无法确认设施状态，请重新拍摄清晰照片后提交', '2024-01-22 15:30:00');


-- ============================================
-- 操作日志数据
-- ============================================
INSERT INTO operation_log (log_id, user_id, operation_type, operation_content, ip_address, create_time) VALUES
-- 登录日志
(1, 1, 'LOGIN', '系统管理员登录系统', '192.168.1.1', '2024-01-10 08:00:00'),
(2, 2, 'LOGIN', '数据采集员张三登录系统', '192.168.1.100', '2024-01-10 08:30:00'),
(3, 3, 'LOGIN', '数据采集员李四登录系统', '192.168.1.101', '2024-01-11 08:15:00'),
(4, 8, 'LOGIN', '审核员刘七登录系统', '192.168.1.102', '2024-01-10 14:00:00'),
(5, 9, 'LOGIN', '审核员周芳登录系统', '192.168.1.103', '2024-01-12 14:00:00'),

-- 设施创建日志
(6, 2, 'FACILITY_CREATE', '创建消防栓：中山大道消防栓-001', '192.168.1.100', '2024-01-10 09:00:00'),
(7, 2, 'FACILITY_CREATE', '创建消防栓：民生路消防栓-001', '192.168.1.100', '2024-01-10 10:30:00'),
(8, 3, 'FACILITY_CREATE', '创建消防栓：兴中道消防栓-001', '192.168.1.101', '2024-01-11 08:45:00'),
(9, 2, 'FACILITY_CREATE', '创建消防栓：孙文西路消防栓-001', '192.168.1.100', '2024-01-11 14:20:00'),
(10, 3, 'FACILITY_CREATE', '创建消防栓：博爱路消防栓-001', '192.168.1.101', '2024-01-12 09:15:00'),
(11, 2, 'FACILITY_CREATE', '创建消防栓：悦来南路消防栓-001', '192.168.1.100', '2024-01-12 11:00:00'),
(12, 3, 'FACILITY_CREATE', '创建消防栓：太平路消防栓-001', '192.168.1.101', '2024-01-13 10:30:00'),
(13, 4, 'FACILITY_CREATE', '创建消防栓：莲塘路消防栓-001', '192.168.1.104', '2024-01-13 15:45:00'),
(14, 2, 'FACILITY_CREATE', '创建消防栓：东区消防栓-001', '192.168.1.100', '2024-01-14 08:30:00'),
(15, 3, 'FACILITY_CREATE', '创建消防栓：东区消防栓-002', '192.168.1.101', '2024-01-14 10:15:00'),

-- 审核日志
(16, 8, 'AUDIT_APPROVE', '审核通过：中山大道消防栓-001', '192.168.1.102', '2024-01-10 15:30:00'),
(17, 8, 'AUDIT_APPROVE', '审核通过：民生路消防栓-001', '192.168.1.102', '2024-01-10 16:00:00'),
(18, 8, 'AUDIT_APPROVE', '审核通过：兴中道消防栓-001', '192.168.1.102', '2024-01-11 14:15:00'),
(19, 8, 'AUDIT_APPROVE', '审核通过：孙文西路消防栓-001', '192.168.1.102', '2024-01-11 17:20:00'),
(20, 8, 'AUDIT_APPROVE', '审核通过：博爱路消防栓-001', '192.168.1.102', '2024-01-12 14:45:00'),
(21, 9, 'AUDIT_APPROVE', '审核通过：悦来南路消防栓-001', '192.168.1.103', '2024-01-12 16:30:00'),
(22, 9, 'AUDIT_APPROVE', '审核通过：太平路消防栓-001', '192.168.1.103', '2024-01-13 15:00:00'),
(23, 8, 'AUDIT_APPROVE', '审核通过：莲塘路消防栓-001', '192.168.1.102', '2024-01-13 17:45:00'),
(24, 9, 'AUDIT_APPROVE', '审核通过：东区消防栓-001', '192.168.1.103', '2024-01-14 14:30:00'),
(25, 8, 'AUDIT_APPROVE', '审核通过：东区消防栓-002', '192.168.1.102', '2024-01-14 16:15:00'),
(26, 8, 'AUDIT_REJECT', '审核驳回：南区驳回消防栓-001，原因：照片模糊', '192.168.1.102', '2024-01-22 15:30:00'),

-- 用户管理日志
(27, 1, 'USER_CREATE', '创建用户：collector03', '192.168.1.1', '2024-01-08 10:00:00'),
(28, 1, 'USER_CREATE', '创建用户：firefighter03', '192.168.1.1', '2024-01-08 10:15:00'),
(29, 1, 'USER_CREATE', '创建用户：auditor02', '192.168.1.1', '2024-01-08 10:30:00'),
(30, 1, 'USER_UPDATE', '修改用户信息：collector01', '192.168.1.1', '2024-01-15 09:00:00'),

-- 设施更新日志
(31, 2, 'FACILITY_UPDATE', '更新消防栓状态：孙文西路消防栓-001 -> 损坏', '192.168.1.100', '2024-01-20 10:00:00'),
(32, 3, 'FACILITY_UPDATE', '更新消防栓状态：太平路消防栓-001 -> 维修中', '192.168.1.101', '2024-01-21 09:00:00'),
(33, 2, 'FACILITY_UPDATE', '更新消防栓状态：东区消防栓-003 -> 损坏', '192.168.1.100', '2024-01-22 11:00:00');

-- ============================================
-- 离线操作日志数据（模拟移动端离线操作）
-- ============================================
INSERT INTO offline_operation_log (log_id, user_id, operation_type, operation_content, operation_time, sync_status, create_time) VALUES
(1, 2, 'FACILITY_CREATE', '离线创建消防栓：临时点位-001', '2024-01-20 10:30:00', 1, '2024-01-20 14:00:00'),
(2, 3, 'FACILITY_UPDATE', '离线更新消防栓压力值', '2024-01-21 11:00:00', 1, '2024-01-21 15:30:00'),
(3, 4, 'FACILITY_CREATE', '离线创建消防栓：临时点位-002', '2024-01-22 09:15:00', 0, '2024-01-22 09:15:00');

-- ============================================
-- 脚本执行完成
-- ============================================
-- 数据统计：
-- 角色：4条
-- 用户：9条
-- 消防设施：25条（已审核21条，待审核3条，已驳回1条）
-- 设施照片：34条
-- 审核记录：22条
-- 操作日志：33条
-- 离线操作日志：3条
-- ============================================

SELECT '数据库初始化完成！' AS message;
SELECT CONCAT('角色数量: ', COUNT(*)) AS info FROM sys_role;
SELECT CONCAT('用户数量: ', COUNT(*)) AS info FROM sys_user;
SELECT CONCAT('消防设施数量: ', COUNT(*)) AS info FROM fire_facility;
SELECT CONCAT('设施照片数量: ', COUNT(*)) AS info FROM fire_facility_photo;
SELECT CONCAT('审核记录数量: ', COUNT(*)) AS info FROM audit_record;
SELECT CONCAT('操作日志数量: ', COUNT(*)) AS info FROM operation_log;
