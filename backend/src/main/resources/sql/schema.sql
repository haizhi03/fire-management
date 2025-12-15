-- 消防设施管理系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS fire_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE fire_management;

-- ============================================
-- 用户与权限相关表
-- ============================================

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    role_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(30) UNIQUE NOT NULL COMMENT '角色名称',
    permissions VARCHAR(500) COMMENT '权限标识，多个用逗号分隔',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
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

-- ============================================
-- 消防设施相关表
-- ============================================

-- 消防设施表
CREATE TABLE IF NOT EXISTS fire_facility (
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

-- 设施照片表
CREATE TABLE IF NOT EXISTS fire_facility_photo (
    photo_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '照片ID',
    facility_id BIGINT NOT NULL COMMENT '设施ID',
    photo_url VARCHAR(500) NOT NULL COMMENT '照片URL',
    photo_desc VARCHAR(100) COMMENT '照片描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_facility (facility_id),
    FOREIGN KEY (facility_id) REFERENCES fire_facility(facility_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设施照片表';

-- ============================================
-- 审核与日志相关表
-- ============================================

-- 审核记录表
CREATE TABLE IF NOT EXISTS audit_record (
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

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
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

-- ============================================
-- 离线同步相关表
-- ============================================

-- 离线操作日志表（用于移动端离线操作记录）
CREATE TABLE IF NOT EXISTS offline_operation_log (
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
