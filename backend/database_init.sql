-- ============================================
-- 消防设施管理系统 - 完整数据库初始化脚本
-- 版本: 1.0
-- 日期: 2025-12-15
-- 说明: 此脚本包含创建数据库、表结构、初始数据的完整流程
-- 使用方法: mysql -u root -p < database_init.sql
-- ============================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 第一部分：创建数据库
-- ============================================

DROP DATABASE IF EXISTS fire_management;
CREATE DATABASE fire_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE fire_management;

-- ============================================
-- 第二部分：创建表结构
-- ============================================

-- 1. 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    role_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(30) UNIQUE NOT NULL COMMENT '角色名称',
    permissions VARCHAR(500) COMMENT '权限标识，多个用逗号分隔',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 2. 用户表
DROP TABLE IF EXISTS sys_user;
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
DROP TABLE IF EXISTS fire_facility;
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
    INDEX idx_create_time (create_time),
    FOREIGN KEY (collector_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消防设施表';

-- 4. 设施照片表
DROP TABLE IF EXISTS fire_facility_photo;
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
DROP TABLE IF EXISTS audit_record;
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
DROP TABLE IF EXISTS operation_log;
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
DROP TABLE IF EXISTS offline_operation_log;
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
-- 第三部分：初始化基础数据
-- ============================================

-- 插入角色数据
INSERT INTO sys_role (role_id, role_name, permissions) VALUES
(1, '系统管理员', 'user:manage,facility:manage,facility:view,audit:manage,stats:view,system:config'),
(2, '数据采集员', 'facility:create,facility:view'),
(3, '消防战士', 'facility:view,map:view');

-- 插入默认用户（密码为BCrypt加密后的 "123456"）
INSERT INTO sys_user (user_id, username, password, real_name, role_id, phone, status) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 1, '13800138000', 1),
(2, 'collector', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '采集员张三', 2, '13800138001', 1),
(3, 'firefighter', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '消防员李四', 3, '13800138002', 1);

-- ============================================
-- 第四部分：插入示例消防设施数据
-- ============================================

-- 正常状态的消防栓
INSERT INTO fire_facility (facility_name, facility_type, longitude, latitude, address, status, pressure, collector_id, audit_status) VALUES
('中山大道消防栓-001', 1, 113.392600, 22.517400, '中山市石岐区中山大道123号', 1, 0.80, 1, 1),
('民生路消防栓-001', 1, 113.396000, 22.501500, '中山市石岐区民生路45号', 1, 0.75, 1, 1),
('兴中道消防栓-001', 1, 113.389500, 22.520100, '中山市石岐区兴中道88号', 1, 0.85, 1, 1),
('博爱路消防栓-001', 1, 113.391800, 22.513200, '中山市石岐区博爱路67号', 1, 0.78, 1, 1),
('岐江公园消防栓-001', 1, 113.378900, 22.515600, '中山市石岐区岐江公园内', 1, 0.82, 1, 1),
('富华道消防栓-001', 1, 113.401200, 22.523400, '中山市东区富华道234号', 1, 0.77, 1, 1),
('兴文路消防栓-001', 1, 113.387600, 22.508700, '中山市西区兴文路89号', 1, 0.81, 1, 1),
('沙朗路消防栓-001', 1, 113.382400, 22.503200, '中山市西区沙朗路45号', 1, 0.79, 1, 1);

-- 损坏状态的消防栓
INSERT INTO fire_facility (facility_name, facility_type, longitude, latitude, address, status, pressure, collector_id, audit_status) VALUES
('孙文西路消防栓-001', 1, 113.385200, 22.516800, '中山市石岐区孙文西路156号', 2, 0.60, 1, 1),
('东明路消防栓-001', 1, 113.399800, 22.519200, '中山市东区东明路78号', 2, 0.55, 1, 1);

-- 维修中的消防栓
INSERT INTO fire_facility (facility_name, facility_type, longitude, latitude, address, status, pressure, collector_id, audit_status) VALUES
('起湾道消防栓-001', 1, 113.405800, 22.518900, '中山市东区起湾道156号', 3, 0.70, 1, 1),
('康华路消防栓-001', 1, 113.394500, 22.525600, '中山市东区康华路234号', 3, 0.68, 1, 1);

-- 待审核的消防栓
INSERT INTO fire_facility (facility_name, facility_type, longitude, latitude, address, status, pressure, collector_id, audit_status) VALUES
('测试消防栓-待审核1', 1, 113.388900, 22.512300, '中山市石岐区测试路1号', 1, 0.75, 1, 0),
('测试消防栓-待审核2', 1, 113.395600, 22.518700, '中山市石岐区测试路2号', 1, 0.80, 1, 0),
('测试消防栓-待审核3', 1, 113.402300, 22.514500, '中山市东区测试路3号', 1, 0.78, 1, 0);

-- ============================================
-- 第五部分：插入关联数据
-- ============================================

-- 为部分设施添加照片记录
INSERT INTO fire_facility_photo (facility_id, photo_url, photo_desc) VALUES
(1, '/uploads/facility_1_main.jpg', '正面照'),
(2, '/uploads/facility_2_main.jpg', '正面照'),
(3, '/uploads/facility_3_main.jpg', '正面照'),
(4, '/uploads/facility_4_main.jpg', '正面照'),
(5, '/uploads/facility_5_main.jpg', '正面照');

-- 添加审核记录
INSERT INTO audit_record (facility_id, auditor_id, audit_result, audit_time) VALUES
(1, 1, 1, NOW()),
(2, 1, 1, NOW()),
(3, 1, 1, NOW()),
(4, 1, 1, NOW()),
(5, 1, 1, NOW()),
(6, 1, 1, NOW()),
(7, 1, 1, NOW()),
(8, 1, 1, NOW()),
(9, 1, 1, NOW()),
(10, 1, 1, NOW());

-- 添加操作日志
INSERT INTO operation_log (user_id, operation_type, operation_content, ip_address) VALUES
(1, 'LOGIN', '系统管理员登录', '127.0.0.1'),
(1, 'FACILITY_CREATE', '批量创建消防设施', '127.0.0.1'),
(1, 'AUDIT_APPROVE', '批量审核通过设施', '127.0.0.1'),
(1, 'STATS_VIEW', '查看数据看板', '127.0.0.1');

-- ============================================
-- 第六部分：恢复外键检查
-- ============================================

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第七部分：验证数据
-- ============================================

SELECT '========== 数据库初始化完成 ==========' AS 提示;

SELECT '--- 角色统计 ---' AS 信息;
SELECT role_id, role_name, permissions FROM sys_role;

SELECT '--- 用户统计 ---' AS 信息;
SELECT user_id, username, real_name, role_id, status FROM sys_user;

SELECT '--- 设施统计 ---' AS 信息;
SELECT 
    COUNT(*) AS 总数,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS 正常,
    SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS 损坏,
    SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) AS 维修中,
    SUM(CASE WHEN audit_status = 0 THEN 1 ELSE 0 END) AS 待审核,
    SUM(CASE WHEN audit_status = 1 THEN 1 ELSE 0 END) AS 已通过
FROM fire_facility WHERE deleted = 0;

SELECT '========== 初始化脚本执行完毕 ==========' AS 提示;
SELECT '默认账号: admin / 123456' AS 登录信息;
