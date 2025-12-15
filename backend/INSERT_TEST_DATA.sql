-- 快速插入测试数据脚本
-- 用于测试地图功能

USE fire_management;

-- 插入测试消防栓数据（中山市区域）
INSERT INTO fire_facility (facility_name, facility_type, longitude, latitude, address, status, pressure, collector_id, audit_status) VALUES
('中山大道消防栓-001', 1, 113.392600, 22.517400, '中山市石岐区中山大道123号', 1, 0.80, 1, 1),
('民生路消防栓-001', 1, 113.396000, 22.501500, '中山市石岐区民生路45号', 1, 0.75, 1, 1),
('兴中道消防栓-001', 1, 113.389500, 22.520100, '中山市石岐区兴中道88号', 1, 0.85, 1, 1),
('孙文西路消防栓-001', 1, 113.385200, 22.516800, '中山市石岐区孙文西路156号', 2, 0.60, 1, 1),
('博爱路消防栓-001', 1, 113.391800, 22.513200, '中山市石岐区博爱路67号', 1, 0.78, 1, 1),
('岐江公园消防栓-001', 1, 113.378900, 22.515600, '中山市石岐区岐江公园内', 1, 0.82, 1, 1),
('富华道消防栓-001', 1, 113.401200, 22.523400, '中山市东区富华道234号', 1, 0.77, 1, 1),
('起湾道消防栓-001', 1, 113.405800, 22.518900, '中山市东区起湾道156号', 3, 0.70, 1, 1),
('兴文路消防栓-001', 1, 113.387600, 22.508700, '中山市西区兴文路89号', 1, 0.81, 1, 1),
('沙朗路消防栓-001', 1, 113.382400, 22.503200, '中山市西区沙朗路45号', 1, 0.79, 1, 1);

-- 查询插入的数据
SELECT 
    facility_id,
    facility_name,
    CASE facility_type 
        WHEN 1 THEN '消防栓'
        WHEN 2 THEN '消防车'
        ELSE '未知'
    END AS type_name,
    longitude,
    latitude,
    address,
    CASE status
        WHEN 1 THEN '正常'
        WHEN 2 THEN '损坏'
        WHEN 3 THEN '维修中'
        ELSE '未知'
    END AS status_name,
    pressure,
    CASE audit_status
        WHEN 0 THEN '待审核'
        WHEN 1 THEN '已通过'
        WHEN 2 THEN '已驳回'
        ELSE '未知'
    END AS audit_name
FROM fire_facility
WHERE deleted = 0
ORDER BY facility_id DESC
LIMIT 10;
