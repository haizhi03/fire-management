package com.fire.management.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fire.management.annotation.RequirePermission;
import com.fire.management.common.Result;
import com.fire.management.dto.FacilityCreateRequest;
import com.fire.management.entity.FireFacility;
import com.fire.management.service.FacilityService;
import com.fire.management.utils.JwtUtil;
import com.fire.management.vo.FacilityDetailVO;
import com.fire.management.vo.FacilityNearbyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 设施管理控制器
 */
@RestController
@RequestMapping("/api/facilities")
public class FacilityController {

    @Autowired
    private FacilityService facilityService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 分页查询设施列表（公开接口，用于地图展示）
     */
    @GetMapping
    public Result<Page<FireFacility>> getFacilityList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer facilityType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) String keyword) {
        Page<FireFacility> page = facilityService.getFacilityList(
            pageNum, pageSize, facilityType, status, auditStatus, keyword);
        return Result.success(page);
    }

    /**
     * 根据ID查询设施详情（公开接口）
     */
    @GetMapping("/{id}")
    public Result<FacilityDetailVO> getFacilityDetail(@PathVariable Long id) {
        FacilityDetailVO detail = facilityService.getFacilityDetail(id);
        return Result.success(detail);
    }

    /**
     * 创建设施
     */
    @PostMapping
    @RequirePermission("facility:collect")
    public Result<FireFacility> createFacility(
            @Validated @RequestBody FacilityCreateRequest request,
            @RequestHeader("Authorization") String authorization) {
        // 从token中获取当前用户ID
        String token = authorization.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        FireFacility facility = facilityService.createFacility(request, userId);
        return Result.success(facility);
    }

    /**
     * 更新设施
     */
    @PutMapping("/{id}")
    @RequirePermission("facility:manage")
    public Result<FireFacility> updateFacility(
            @PathVariable Long id,
            @Validated @RequestBody FacilityCreateRequest request) {
        FireFacility facility = facilityService.updateFacility(id, request);
        return Result.success(facility);
    }

    /**
     * 删除设施
     */
    @DeleteMapping("/{id}")
    @RequirePermission("facility:manage")
    public Result<Void> deleteFacility(@PathVariable Long id) {
        facilityService.deleteFacility(id);
        return Result.success();
    }

    /**
     * 查询周边设施（公开接口，用于地图展示）
     */
    @GetMapping("/nearby")
    public Result<List<FacilityNearbyVO>> getNearbyFacilities(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam(required = false) Integer radius,
            @RequestParam(required = false) Integer facilityType) {
        List<FacilityNearbyVO> facilities = facilityService.getNearbyFacilities(
            latitude, longitude, radius, facilityType);
        return Result.success(facilities);
    }

    /**
     * 增强周边查询（公开接口，支持距离圈、单位转换）
     */
    @GetMapping("/nearby/enhanced")
    public Result<List<FacilityNearbyVO>> getEnhancedNearbyFacilities(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam(required = false) Integer radius,
            @RequestParam(required = false) String unit, // "m" 或 "km"
            @RequestParam(required = false) Integer facilityType,
            @RequestParam(required = false) Integer status) {
        List<FacilityNearbyVO> facilities = facilityService.getEnhancedNearbyFacilities(
            latitude, longitude, radius, unit, facilityType, status);
        return Result.success(facilities);
    }

    /**
     * 查询最近的N个设施（公开接口）
     */
    @GetMapping("/closest")
    public Result<List<FacilityNearbyVO>> getClosestFacilities(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam(defaultValue = "3") Integer limit,
            @RequestParam(required = false) Integer facilityType,
            @RequestParam(required = false) Integer status) {
        List<FacilityNearbyVO> facilities = facilityService.getClosestFacilities(
            latitude, longitude, limit, facilityType, status);
        return Result.success(facilities);
    }

    /**
     * 按距离范围筛选设施
     */
    @PostMapping("/distance-filter")
    @RequirePermission("facility:view")
    public Result<List<FacilityNearbyVO>> filterFacilitiesByDistance(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam Integer minDistance,
            @RequestParam Integer maxDistance,
            @RequestParam(required = false) String unit,
            @RequestParam(required = false) Integer facilityType,
            @RequestParam(required = false) Integer status) {
        List<FacilityNearbyVO> facilities = facilityService.filterFacilitiesByDistance(
            latitude, longitude, minDistance, maxDistance, unit, facilityType, status);
        return Result.success(facilities);
    }

    /**
     * 紧急模式查询（公开接口，优先显示状态正常且距离最近的设施）
     */
    @GetMapping("/emergency")
    public Result<List<FacilityNearbyVO>> getEmergencyFacilities(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam(defaultValue = "1000") Integer radius,
            @RequestParam(required = false) Integer facilityType) {
        List<FacilityNearbyVO> facilities = facilityService.getEmergencyFacilities(
            latitude, longitude, radius, facilityType);
        return Result.success(facilities);
    }
}
