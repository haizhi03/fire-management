package com.fire.management.controller;

import com.fire.management.annotation.RequirePermission;
import com.fire.management.common.Result;
import com.fire.management.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 统计分析控制器
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取看板统计数据（公开接口）
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = statisticsService.getDashboardStats();
        return Result.success(stats);
    }

    /**
     * 按类型统计（公开接口）
     */
    @GetMapping("/by-type")
    public Result<Map<String, Long>> getStatsByType() {
        Map<String, Long> stats = statisticsService.getStatsByType();
        return Result.success(stats);
    }

    /**
     * 按状态统计（公开接口）
     */
    @GetMapping("/by-status")
    public Result<Map<String, Long>> getStatsByStatus() {
        Map<String, Long> stats = statisticsService.getStatsByStatus();
        return Result.success(stats);
    }

    /**
     * 按审核状态统计（公开接口）
     */
    @GetMapping("/by-audit-status")
    public Result<Map<String, Long>> getStatsByAuditStatus() {
        Map<String, Long> stats = statisticsService.getStatsByAuditStatus();
        return Result.success(stats);
    }

    /**
     * 获取采集趋势（公开接口）
     */
    @GetMapping("/trend")
    public Result<Map<String, Long>> getCollectionTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Map<String, Long> trend = statisticsService.getCollectionTrend(startTime, endTime);
        return Result.success(trend);
    }
}
