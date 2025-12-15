-- ============================================
-- 完整初始化脚本
-- 包含：权限修复 + 测试数据
-- ============================================

USE fire_management;

-- ============================================
-- 1. 修复权限问题
-- ============================================

UPDATE sys_role 
SET permissions = 'user:manage,facility:manage,facility:view,audit:manage,stats:view,system:config'
WHERE role_id = 1;

-- ============================================
-- 2. 插入测试消防设施数据
-- ============================================

INSERT INTO fire_facility (facility_name, facility_type, longitude, latitude, address, status, pressure, collector_id, audit_status) VALUES
-- 正常状态的消防栓
('中山大道消防栓-001', 1, 113.392600, 22.517400, '中山市石岐区中山大道123号', 1, 0.80, 1, 1),
('民生路消防栓-001', 1, 113.396000, 22.501500, '中山市石岐区民生路45号', 1, 0.75, 1, 1),
('兴中道消防栓-001', 1, 113.389500, 22.520100, '中山市石岐区兴中道88号', 1, 0.85, 1, 1),
('博爱路消防栓-001', 1, 113.391800, 22.513200, '中山市石岐区博爱路67号', 1, 0.78, 1, 1),
('岐江公园消防栓-001', 1, 113.378900, 22.515600, '中山市石岐区岐江公园内', 1, 0.82, 1, 1),
('富华道消防栓-001', 1, 113.401200, 22.523400, '中山市东区富华道234号', 1, 0.77, 1, 1),
('兴文路消防栓-001', 1, 113.387600, 22.508700, '中山市西区兴文路89号', 1, 0.81, 1, 1),
('沙朗路消防栓-001', 1, 113.382400, 22.503200, '中山市西区沙朗路45号', 1, 0.79, 1, 1),

-- 损坏状态的消防栓
('孙文西路消防栓-001', 1, 113.385200, 22.516800, '中山市石岐区孙文西路156号', 2, 0.60, 1, 1),
('东明路消防栓-001', 1, 113.399800, 22.519200, '中山市东区东明路78号', 2, 0.55, 1, 1),

-- 维修中的消防栓
('起湾道消防栓-001', 1, 113.405800, 22.518900, '中山市东区起湾道156号', 3, 0.70, 1, 1),
('康华路消防栓-001', 1, 113.394500, 22.525600, '中山市东区康华路234号', 3, 0.68, 1, 1),

-- 待审核的消防栓
('测试消防栓-待审核1', 1, 113.388900, 22.512300, '中山市石岐区测试路1号', 1, 0.75, 1, 0),
('测试消防栓-待审核2', 1, 113.395600, 22.518700, '中山市石岐区测试路2号', 1, 0.80, 1, 0),
('测试消防栓-待审核3', 1, 113.402300, 22.514500, '中山市东区测试路3号', 1, 0.78, 1, 0);

-- ============================================
-- 3. 为部分设施添加照片记录
-- ============================================

INSERT INTO fire_facility_photo (facility_id, photo_url, photo_desc) 
SELECT facility_id, CONCAT('/uploads/facility_', facility_id, '_main.jpg'), '正面照'
FROM fire_facility 
WHERE facility_id <= 5;

-- ============================================
-- 4. 添加审核记录
-- ============================================

INSERT INTO audit_record (facility_id, auditor_id, audit_result, audit_time)
SELECT facility_id, 1, 1, NOW()
FROM fire_facility 
WHERE audit_status = 1 AND facility_id <= 10;

-- ============================================
-- 5. 添加操作日志
-- ============================================

INSERT INTO operation_log (user_id, operation_type, operation_content, ip_address) VALUES
(1, 'LOGIN', '系统管理员登录', '127.0.0.1'),
(1, 'FACILITY_CREATE', '批量创建消防设施', '127.0.0.1'),
(1, 'AUDIT_APPROVE', '批量审核通过设施', '127.0.0.1'),
(1, 'STATS_VIEW', '查看数据看板', '127.0.0.1');

-- ============================================
-- 6. 验证数据
-- ============================================

-- 查看设施统计
SELECT 
    '设施总数' as 项目,
    COUNT(*) as 数量
FROM fire_facility 
WHERE deleted = 0
UNION ALL
SELECT 
    '正常设施',
    COUNT(*)
FROM fire_facility 
WHERE deleted = 0 AND status = 1
UNION ALL
SELECT 
    '损坏设施',
    COUNT(*)
FROM fire_facility 
WHERE deleted = 0 AND status = 2
UNION ALL
SELECT 
    '维修中设施',
    COUNT(*)
FROM fire_facility 
WHERE deleted = 0 AND status = 3
UNION ALL
SELECT 
    '已审核通过',
    COUNT(*)
FROM fire_facility 
WHERE deleted = 0 AND audit_status = 1
UNION ALL
SELECT 
    '待审核',
    COUNT(*)
FROM fire_facility 
WHERE deleted = 0 AND audit_status = 0;

-- 查看最新的设施
SELECT 
    facility_id,
    facility_name,
    CASE status 
        WHEN 1 THEN '正常'
        WHEN 2 THEN '损坏'
        WHEN 3 THEN '维修中'
    END as 状态,
    CASE audit_status
        WHEN 0 THEN '待审核'
        WHEN 1 THEN '已通过'
        WHEN 2 THEN '已驳回'
    END as 审核状态,
    address as 地址
FROM fire_facility
WHERE deleted = 0
ORDER BY facility_id DESC
LIMIT 10;

-- ============================================
-- 完成提示
-- ============================================
SELECT '数据初始化完成！请重新登录系统查看效果。' as 提示;


USE fire_management;

UPDATE sys_role
SET permissions = 'user:manage,facility:manage,facility:view,audit:manage,stats:view,system:config'
WHERE role_id = 1;

-- 验证
SELECT role_id, role_name, permissions FROM sys_role WHERE role_id = 1;
