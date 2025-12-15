package com.fire.management.service;

import com.fire.management.utils.GeoUtil;
import com.fire.management.vo.RouteVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 导航服务
 */
@Service
public class NavigationService {

    private final ConcurrentHashMap<String, Object> activeNavigations = new ConcurrentHashMap<>();

    /**
     * 获取路径规划方案
     */
    public List<RouteVO> getRoute(BigDecimal startLat, BigDecimal startLon,
                                  BigDecimal endLat, BigDecimal endLon,
                                  String mode) {
        List<RouteVO> routes = new ArrayList<>();

        // 创建基础路径（直线距离）
        RouteVO route = new RouteVO();
        route.setMode(mode);

        // 计算直线距离
        double distance = GeoUtil.calculateDistance(startLat, startLon, endLat, endLon);
        route.setTotalDistance(distance);

        // 根据模式估算时间
        int estimatedTime = calculateEstimatedTime(distance, mode);
        route.setEstimatedTime(estimatedTime);

        // 创建简单的两点路径
        List<RouteVO.RoutePoint> points = new ArrayList<>();
        RouteVO.RoutePoint startPoint = new RouteVO.RoutePoint();
        startPoint.setLatitude(startLat);
        startPoint.setLongitude(startLon);
        points.add(startPoint);

        RouteVO.RoutePoint endPoint = new RouteVO.RoutePoint();
        endPoint.setLatitude(endLat);
        endPoint.setLongitude(endLon);
        points.add(endPoint);

        route.setPoints(points);

        // 创建简单的导航指令
        List<RouteVO.NavigationInstruction> instructions = new ArrayList<>();
        RouteVO.NavigationInstruction instruction = new RouteVO.NavigationInstruction();
        instruction.setInstruction("前往目的地");
        instruction.setDistance(distance);
        instruction.setDuration(estimatedTime);
        instruction.setDirection("直行");
        instructions.add(instruction);

        route.setInstructions(instructions);
        routes.add(route);

        return routes;
    }

    /**
     * 启动实时导航
     */
    public String startNavigation(BigDecimal startLat, BigDecimal startLon,
                                  BigDecimal endLat, BigDecimal endLon,
                                  String mode) {
        String navigationId = UUID.randomUUID().toString();
        
        // 创建导航会话
        NavigationSession session = new NavigationSession();
        session.setStartLat(startLat);
        session.setStartLon(startLon);
        session.setEndLat(endLat);
        session.setEndLon(endLon);
        session.setMode(mode);
        session.setStartTime(System.currentTimeMillis());
        session.setActive(true);

        activeNavigations.put(navigationId, session);
        
        return navigationId;
    }

    /**
     * 获取导航状态
     */
    public Object getNavigationStatus(String navigationId) {
        return activeNavigations.get(navigationId);
    }

    /**
     * 停止导航
     */
    public void stopNavigation(String navigationId) {
        NavigationSession session = (NavigationSession) activeNavigations.get(navigationId);
        if (session != null) {
            session.setActive(false);
            session.setEndTime(System.currentTimeMillis());
        }
        activeNavigations.remove(navigationId);
    }

    /**
     * 根据模式计算预计时间
     */
    private int calculateEstimatedTime(double distance, String mode) {
        switch (mode.toLowerCase()) {
            case "walking":
                // 步行速度约5km/h
                return (int) (distance / 1.39); // 1.39 m/s
            case "driving":
                // 驾车速度约30km/h（城市道路）
                return (int) (distance / 8.33); // 8.33 m/s
            case "cycling":
                // 骑行速度约15km/h
                return (int) (distance / 4.17); // 4.17 m/s
            default:
                return (int) (distance / 1.39);
        }
    }

    /**
     * 导航会话
     */
    public static class NavigationSession {
        private BigDecimal startLat;
        private BigDecimal startLon;
        private BigDecimal endLat;
        private BigDecimal endLon;
        private String mode;
        private long startTime;
        private long endTime;
        private boolean active;

        // Getters and Setters
        public BigDecimal getStartLat() { return startLat; }
        public void setStartLat(BigDecimal startLat) { this.startLat = startLat; }
        
        public BigDecimal getStartLon() { return startLon; }
        public void setStartLon(BigDecimal startLon) { this.startLon = startLon; }
        
        public BigDecimal getEndLat() { return endLat; }
        public void setEndLat(BigDecimal endLat) { this.endLat = endLat; }
        
        public BigDecimal getEndLon() { return endLon; }
        public void setEndLon(BigDecimal endLon) { this.endLon = endLon; }
        
        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
}