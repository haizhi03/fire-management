package com.fire.management.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fire.management.annotation.RequirePermission;
import com.fire.management.common.Result;
import com.fire.management.entity.AuditRecord;
import com.fire.management.entity.FireFacility;
import com.fire.management.service.AuditService;
import com.fire.management.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 审核控制器
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取待审核列表
     */
    @GetMapping("/pending")
    @RequirePermission("audit:manage")
    public Result<Page<FireFacility>> getPendingList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<FireFacility> page = auditService.getPendingList(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    /**
     * 审核通过
     */
    @PostMapping("/{id}/approve")
    @RequirePermission("audit:manage")
    public Result<Void> approve(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization) {
        String token = authorization.substring(7);
        Long auditorId = jwtUtil.getUserIdFromToken(token);
        
        auditService.approve(id, auditorId);
        return Result.success();
    }

    /**
     * 审核驳回
     */
    @PostMapping("/{id}/reject")
    @RequirePermission("audit:manage")
    public Result<Void> reject(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestHeader("Authorization") String authorization) {
        String token = authorization.substring(7);
        Long auditorId = jwtUtil.getUserIdFromToken(token);
        
        auditService.reject(id, auditorId, reason);
        return Result.success();
    }

    /**
     * 获取审核记录
     */
    @GetMapping("/records")
    @RequirePermission("audit:manage")
    public Result<Page<AuditRecord>> getAuditRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long facilityId) {
        Page<AuditRecord> page = auditService.getAuditRecords(pageNum, pageSize, facilityId);
        return Result.success(page);
    }
}
