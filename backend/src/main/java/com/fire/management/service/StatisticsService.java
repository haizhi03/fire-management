package com.fire.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fire.management.entity.FireFacility;
import com.fire.management.mapper.FireFacilityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 统计分析服务
 */
@Service
public class StatisticsService {

    @Autowired
    private FireFacilityMapper facilityMapper;

    /**
     * 获取看板统计数据
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 设施总数
        LambdaQueryWrapper<FireFacility> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(FireFacility::getIsMerged, 0);
        long total = facilityMapper.selectCount(totalWrapper);
        stats.put("total", total);

        // 正常设施数
        LambdaQueryWrapper<FireFacility> normalWrapper = new LambdaQueryWrapper<>();
        normalWrapper.eq(FireFacility::getIsMerged, 0)
                    .eq(FireFacility::getStatus, 1);
        long normal = facilityMapper.selectCount(normalWrapper);
        stats.put("normal", normal);

        // 损坏设施数
        LambdaQueryWrapper<FireFacility> damagedWrapper = new LambdaQueryWrapper<>();
        damagedWrapper.eq(FireFacility::getIsMerged, 0)
                     .eq(FireFacility::getStatus, 2);
        long damaged = facilityMapper.selectCount(damagedWrapper);
        stats.put("damaged", damaged);

        // 维修中设施数
        LambdaQueryWrapper<FireFacility> repairingWrapper = new LambdaQueryWrapper<>();
        repairingWrapper.eq(FireFacility::getIsMerged, 0)
                       .eq(FireFacility::getStatus, 3);
        long repairing = facilityMapper.selectCount(repairingWrapper);
        stats.put("repairing", repairing);

        // 待审核数
        LambdaQueryWrapper<FireFacility> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(FireFacility::getIsMerged, 0)
                     .eq(FireFacility::getAuditStatus, 0);
        long pending = facilityMapper.selectCount(pendingWrapper);
        stats.put("pending", pending);

        // 已审核通过数
        LambdaQueryWrapper<FireFacility> approvedWrapper = new LambdaQueryWrapper<>();
        approvedWrapper.eq(FireFacility::getIsMerged, 0)
                      .eq(FireFacility::getAuditStatus, 1);
        long approved = facilityMapper.selectCount(approvedWrapper);
        stats.put("approved", approved);

        // 已驳回数
        LambdaQueryWrapper<FireFacility> rejectedWrapper = new LambdaQueryWrapper<>();
        rejectedWrapper.eq(FireFacility::getIsMerged, 0)
                      .eq(FireFacility::getAuditStatus, 2);
        long rejected = facilityMapper.selectCount(rejectedWrapper);
        stats.put("rejected", rejected);

        // 消防栓数量
        LambdaQueryWrapper<FireFacility> hydrantWrapper = new LambdaQueryWrapper<>();
        hydrantWrapper.eq(FireFacility::getIsMerged, 0)
                     .eq(FireFacility::getFacilityType, 1);
        long hydrants = facilityMapper.selectCount(hydrantWrapper);
        stats.put("hydrants", hydrants);

        // 消防车数量
        LambdaQueryWrapper<FireFacility> truckWrapper = new LambdaQueryWrapper<>();
        truckWrapper.eq(FireFacility::getIsMerged, 0)
                   .eq(FireFacility::getFacilityType, 2);
        long trucks = facilityMapper.selectCount(truckWrapper);
        stats.put("trucks", trucks);

        // 计算百分比
        if (total > 0) {
            stats.put("normalRate", String.format("%.1f%%", (normal * 100.0 / total)));
            stats.put("damagedRate", String.format("%.1f%%", (damaged * 100.0 / total)));
            stats.put("repairingRate", String.format("%.1f%%", (repairing * 100.0 / total)));
        } else {
            stats.put("normalRate", "0%");
            stats.put("damagedRate", "0%");
            stats.put("repairingRate", "0%");
        }

        return stats;
    }

    /**
     * 获取按类型统计的数据
     */
    public Map<String, Long> getStatsByType() {
        Map<String, Long> stats = new HashMap<>();

        LambdaQueryWrapper<FireFacility> hydrantWrapper = new LambdaQueryWrapper<>();
        hydrantWrapper.eq(FireFacility::getIsMerged, 0)
                     .eq(FireFacility::getFacilityType, 1);
        stats.put("hydrants", facilityMapper.selectCount(hydrantWrapper));

        LambdaQueryWrapper<FireFacility> truckWrapper = new LambdaQueryWrapper<>();
        truckWrapper.eq(FireFacility::getIsMerged, 0)
                   .eq(FireFacility::getFacilityType, 2);
        stats.put("trucks", facilityMapper.selectCount(truckWrapper));

        return stats;
    }

    /**
     * 获取按状态统计的数据
     */
    public Map<String, Long> getStatsByStatus() {
        Map<String, Long> stats = new HashMap<>();

        for (int status = 1; status <= 3; status++) {
            LambdaQueryWrapper<FireFacility> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FireFacility::getIsMerged, 0)
                   .eq(FireFacility::getStatus, status);
            long count = facilityMapper.selectCount(wrapper);
            
            String key = status == 1 ? "normal" : (status == 2 ? "damaged" : "repairing");
            stats.put(key, count);
        }

        return stats;
    }

    /**
     * 获取按审核状态统计的数据
     */
    public Map<String, Long> getStatsByAuditStatus() {
        Map<String, Long> stats = new HashMap<>();

        for (int auditStatus = 0; auditStatus <= 2; auditStatus++) {
            LambdaQueryWrapper<FireFacility> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FireFacility::getIsMerged, 0)
                   .eq(FireFacility::getAuditStatus, auditStatus);
            long count = facilityMapper.selectCount(wrapper);
            
            String key = auditStatus == 0 ? "pending" : (auditStatus == 1 ? "approved" : "rejected");
            stats.put(key, count);
        }

        return stats;
    }

    /**
     * 获取时间范围内的采集趋势
     */
    public Map<String, Long> getCollectionTrend(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Long> trend = new HashMap<>();

        LambdaQueryWrapper<FireFacility> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireFacility::getIsMerged, 0)
               .between(FireFacility::getCreateTime, startTime, endTime);
        
        long count = facilityMapper.selectCount(wrapper);
        trend.put("count", count);
        trend.put("startTime", startTime.toEpochSecond(java.time.ZoneOffset.of("+8")));
        trend.put("endTime", endTime.toEpochSecond(java.time.ZoneOffset.of("+8")));

        return trend;
    }
}
