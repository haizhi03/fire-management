package com.fire.management.controller;

import com.fire.management.annotation.RequirePermission;
import com.fire.management.common.Result;
import com.fire.management.service.NavigationService;
import com.fire.management.vo.RouteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 导航控制器
 */
@RestController
@RequestMapping("/api/navigation")
public class NavigationController {

    @Autowired
    private NavigationService navigationService;

    /**
     * 获取路径规划方案（公开接口）
     */
    @GetMapping("/route")
    public Result<List<RouteVO>> getRoute(
            @RequestParam BigDecimal startLat,
            @RequestParam BigDecimal startLon,
            @RequestParam BigDecimal endLat,
            @RequestParam BigDecimal endLon,
            @RequestParam(defaultValue = "walking") String mode) {
        List<RouteVO> routes = navigationService.getRoute(
            startLat, startLon, endLat, endLon, mode);
        return Result.success(routes);
    }

    /**
     * 启动实时导航（公开接口）
     */
    @PostMapping("/start")
    public Result<String> startNavigation(
            @RequestParam BigDecimal startLat,
            @RequestParam BigDecimal startLon,
            @RequestParam BigDecimal endLat,
            @RequestParam BigDecimal endLon,
            @RequestParam(defaultValue = "walking") String mode) {
        String navigationId = navigationService.startNavigation(
            startLat, startLon, endLat, endLon, mode);
        return Result.success(navigationId);
    }

    /**
     * 获取导航状态（公开接口）
     */
    @GetMapping("/status/{navigationId}")
    public Result<Object> getNavigationStatus(@PathVariable String navigationId) {
        Object status = navigationService.getNavigationStatus(navigationId);
        return Result.success(status);
    }

    /**
     * 停止导航（公开接口）
     */
    @PostMapping("/stop/{navigationId}")
    public Result<Void> stopNavigation(@PathVariable String navigationId) {
        navigationService.stopNavigation(navigationId);
        return Result.success();
    }
}