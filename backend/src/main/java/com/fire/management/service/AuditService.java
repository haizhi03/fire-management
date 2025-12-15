package com.fire.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fire.management.entity.AuditRecord;
import com.fire.management.entity.FireFacility;
import com.fire.management.mapper.AuditRecordMapper;
import com.fire.management.mapper.FireFacilityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 审核服务
 */
@Service
public class AuditService {

    @Autowired
    private FireFacilityMapper facilityMapper;

    @Autowired
    private AuditRecordMapper auditRecordMapper;

    /**
     * 获取待审核列表
     */
    public Page<FireFacility> getPendingList(int pageNum, int pageSize, String keyword) {
        Page<FireFacility> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FireFacility> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询待审核的数据
        wrapper.eq(FireFacility::getAuditStatus, 0);
        
        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(FireFacility::getFacilityName, keyword)
                           .or()
                           .like(FireFacility::getAddress, keyword));
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(FireFacility::getCreateTime);
        
        return facilityMapper.selectPage(page, wrapper);
    }

    /**
     * 审核通过
     */
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long facilityId, Long auditorId) {
        FireFacility facility = facilityMapper.selectById(facilityId);
        if (facility == null) {
            throw new RuntimeException("设施不存在");
        }

        if (facility.getAuditStatus() != 0) {
            throw new RuntimeException("该设施已审核，无需重复审核");
        }

        // 更新审核状态
        facility.setAuditStatus(1);
        facilityMapper.updateById(facility);

        // 创建审核记录
        AuditRecord record = new AuditRecord();
        record.setFacilityId(facilityId);
        record.setAuditorId(auditorId);
        record.setAuditResult(1);
        record.setAuditTime(LocalDateTime.now());
        auditRecordMapper.insert(record);
    }

    /**
     * 审核驳回
     */
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long facilityId, Long auditorId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("驳回原因不能为空");
        }

        FireFacility facility = facilityMapper.selectById(facilityId);
        if (facility == null) {
            throw new RuntimeException("设施不存在");
        }

        if (facility.getAuditStatus() != 0) {
            throw new RuntimeException("该设施已审核，无需重复审核");
        }

        // 更新审核状态
        facility.setAuditStatus(2);
        facilityMapper.updateById(facility);

        // 创建审核记录
        AuditRecord record = new AuditRecord();
        record.setFacilityId(facilityId);
        record.setAuditorId(auditorId);
        record.setAuditResult(2);
        record.setRejectReason(reason);
        record.setAuditTime(LocalDateTime.now());
        auditRecordMapper.insert(record);
    }

    /**
     * 获取审核记录
     */
    public Page<AuditRecord> getAuditRecords(int pageNum, int pageSize, Long facilityId) {
        Page<AuditRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AuditRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (facilityId != null) {
            wrapper.eq(AuditRecord::getFacilityId, facilityId);
        }
        
        wrapper.orderByDesc(AuditRecord::getAuditTime);
        
        return auditRecordMapper.selectPage(page, wrapper);
    }
}
