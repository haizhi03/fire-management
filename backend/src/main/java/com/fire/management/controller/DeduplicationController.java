package com.fire.management.controller;

import com.fire.management.annotation.RequirePermission;
import com.fire.management.common.Result;
import com.fire.management.entity.FireFacility;
import com.fire.management.service.DeduplicationService;
import com.fire.management.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据去重控制器
 */
@RestController
@RequestMapping("/api/deduplication")
public class DeduplicationController {

    @Autowired
    private DeduplicationService deduplicationService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 检测指定设施的重复数据
     */
    @GetMapping("/detect/{id}")
    @RequirePermission("facility:manage")
    public Result<List<FireFacility>> detectDuplicates(@PathVariable Long id) {
        List<FireFacility> duplicates = deduplicationService.detectDuplicates(id);
        return Result.success(duplicates);
    }

    /**
     * 检测所有重复数据
     */
    @GetMapping("/detect-all")
    @RequirePermission("facility:manage")
    public Result<List<Map<String, Object>>> detectAllDuplicates() {
        List<Map<String, Object>> groups = deduplicationService.detectAllDuplicates();
        return Result.success(groups);
    }

    /**
     * 手动合并重复数据
     */
    @PostMapping("/merge")
    @RequirePermission("facility:manage")
    public Result<Void> mergeDuplicates(
            @RequestParam Long keepId,
            @RequestParam List<Long> mergeIds,
            @RequestHeader("Authorization") String authorization) {
        String token = authorization.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);

        deduplicationService.mergeDuplicates(keepId, mergeIds, userId);
        return Result.success();
    }

    /**
     * 自动去重
     */
    @PostMapping("/auto")
    @RequirePermission("facility:manage")
    public Result<Integer> autoDeduplication(@RequestHeader("Authorization") String authorization) {
        String token = authorization.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);

        int mergedCount = deduplicationService.autoDeduplication(userId);
        return Result.success("成功合并 " + mergedCount + " 条重复数据", mergedCount);
    }
}
