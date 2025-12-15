package com.fire.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fire.management.entity.FireFacility;
import com.fire.management.entity.OperationLog;
import com.fire.management.mapper.FireFacilityMapper;
import com.fire.management.mapper.OperationLogMapper;
import com.fire.management.utils.GeoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据去重服务
 */
@Service
public class DeduplicationService {

    @Autowired
    private FireFacilityMapper facilityMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    /**
     * 检测重复数据
     * 
     * @param facilityId 要检测的设施ID
     * @return 重复的设施列表
     */
    public List<FireFacility> detectDuplicates(Long facilityId) {
        FireFacility facility = facilityMapper.selectById(facilityId);
        if (facility == null) {
            throw new RuntimeException("设施不存在");
        }

        return findNearbyDuplicates(
            facility.getLatitude(),
            facility.getLongitude(),
            10.0, // 10米内视为重复
            facilityId
        );
    }

    /**
     * 查找附近的重复数据
     */
    private List<FireFacility> findNearbyDuplicates(BigDecimal latitude, 
                                                     BigDecimal longitude, 
                                                     double maxDistance,
                                                     Long excludeId) {
        // 计算边界框
        double[] bbox = GeoUtil.getBoundingBox(
            latitude.doubleValue(),
            longitude.doubleValue(),
            maxDistance
        );

        // 查询边界框内的设施
        LambdaQueryWrapper<FireFacility> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(FireFacility::getLatitude,
                       BigDecimal.valueOf(bbox[0]),
                       BigDecimal.valueOf(bbox[1]))
               .between(FireFacility::getLongitude,
                       BigDecimal.valueOf(bbox[2]),
                       BigDecimal.valueOf(bbox[3]))
               .eq(FireFacility::getIsMerged, 0)
               .ne(FireFacility::getFacilityId, excludeId);

        List<FireFacility> candidates = facilityMapper.selectList(wrapper);

        // 精确计算距离并过滤
        List<FireFacility> duplicates = new ArrayList<>();
        for (FireFacility candidate : candidates) {
            double distance = GeoUtil.calculateDistance(
                latitude, longitude,
                candidate.getLatitude(), candidate.getLongitude()
            );

            if (distance <= maxDistance) {
                duplicates.add(candidate);
            }
        }

        return duplicates;
    }

    /**
     * 合并重复数据
     * 
     * @param keepId 保留的设施ID
     * @param mergeIds 要合并的设施ID列表
     * @param userId 操作用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void mergeDuplicates(Long keepId, List<Long> mergeIds, Long userId) {
        FireFacility keepFacility = facilityMapper.selectById(keepId);
        if (keepFacility == null) {
            throw new RuntimeException("保留的设施不存在");
        }

        StringBuilder mergeInfo = new StringBuilder();
        mergeInfo.append("合并设施：保留 ").append(keepId).append("，合并 ");

        for (Long mergeId : mergeIds) {
            FireFacility mergeFacility = facilityMapper.selectById(mergeId);
            if (mergeFacility == null) {
                continue;
            }

            // 标记为已合并
            mergeFacility.setIsMerged(1);
            mergeFacility.setMergedTo(keepId);
            facilityMapper.updateById(mergeFacility);

            mergeInfo.append(mergeId).append(" ");
        }

        // 记录操作日志
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setOperationType("DATA_MERGE");
        log.setOperationContent(mergeInfo.toString());
        log.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    /**
     * 批量检测所有重复数据
     * 
     * @return 重复数据分组
     */
    public List<Map<String, Object>> detectAllDuplicates() {
        // 查询所有未合并的设施
        LambdaQueryWrapper<FireFacility> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireFacility::getIsMerged, 0)
               .eq(FireFacility::getAuditStatus, 1); // 只检测已审核通过的

        List<FireFacility> allFacilities = facilityMapper.selectList(wrapper);
        
        List<Map<String, Object>> duplicateGroups = new ArrayList<>();
        List<Long> processed = new ArrayList<>();

        for (FireFacility facility : allFacilities) {
            if (processed.contains(facility.getFacilityId())) {
                continue;
            }

            List<FireFacility> duplicates = findNearbyDuplicates(
                facility.getLatitude(),
                facility.getLongitude(),
                10.0,
                facility.getFacilityId()
            );

            if (!duplicates.isEmpty()) {
                Map<String, Object> group = new HashMap<>();
                group.put("main", facility);
                group.put("duplicates", duplicates);
                group.put("count", duplicates.size() + 1);
                duplicateGroups.add(group);

                // 标记为已处理
                processed.add(facility.getFacilityId());
                for (FireFacility dup : duplicates) {
                    processed.add(dup.getFacilityId());
                }
            }
        }

        return duplicateGroups;
    }

    /**
     * 自动去重（保留信息最完整的记录）
     */
    @Transactional(rollbackFor = Exception.class)
    public int autoDeduplication(Long userId) {
        List<Map<String, Object>> duplicateGroups = detectAllDuplicates();
        int mergedCount = 0;

        for (Map<String, Object> group : duplicateGroups) {
            FireFacility main = (FireFacility) group.get("main");
            @SuppressWarnings("unchecked")
            List<FireFacility> duplicates = (List<FireFacility>) group.get("duplicates");

            // 找出信息最完整的记录
            FireFacility mostComplete = findMostComplete(main, duplicates);

            // 合并其他记录
            List<Long> mergeIds = new ArrayList<>();
            for (FireFacility facility : duplicates) {
                if (!facility.getFacilityId().equals(mostComplete.getFacilityId())) {
                    mergeIds.add(facility.getFacilityId());
                }
            }
            if (!main.getFacilityId().equals(mostComplete.getFacilityId())) {
                mergeIds.add(main.getFacilityId());
            }

            if (!mergeIds.isEmpty()) {
                mergeDuplicates(mostComplete.getFacilityId(), mergeIds, userId);
                mergedCount += mergeIds.size();
            }
        }

        return mergedCount;
    }

    /**
     * 找出信息最完整的记录
     */
    private FireFacility findMostComplete(FireFacility main, List<FireFacility> duplicates) {
        List<FireFacility> all = new ArrayList<>();
        all.add(main);
        all.addAll(duplicates);

        FireFacility mostComplete = main;
        int maxScore = calculateCompletenessScore(main);

        for (FireFacility facility : duplicates) {
            int score = calculateCompletenessScore(facility);
            if (score > maxScore) {
                maxScore = score;
                mostComplete = facility;
            }
        }

        return mostComplete;
    }

    /**
     * 计算信息完整度分数
     */
    private int calculateCompletenessScore(FireFacility facility) {
        int score = 0;

        if (facility.getFacilityName() != null && !facility.getFacilityName().isEmpty()) {
            score += 10;
        }
        if (facility.getAddress() != null && !facility.getAddress().isEmpty()) {
            score += 10;
        }
        if (facility.getPressure() != null) {
            score += 10;
        }
        if (facility.getSpecialType() != null && !facility.getSpecialType().isEmpty()) {
            score += 5;
        }
        if (facility.getStatus() != null) {
            score += 5;
        }

        return score;
    }
}
