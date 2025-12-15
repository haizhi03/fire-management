package com.fire.management.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 路径规划VO
 */
@Data
public class RouteVO {

    /**
     * 路径模式：walking, driving, cycling
     */
    private String mode;

    /**
     * 总距离（米）
     */
    private Double totalDistance;

    /**
     * 预计时间（秒）
     */
    private Integer estimatedTime;

    /**
     * 路径点列表
     */
    private List<RoutePoint> points;

    /**
     * 导航指令列表
     */
    private List<NavigationInstruction> instructions;

    /**
     * 路径点
     */
    @Data
    public static class RoutePoint {
        private BigDecimal latitude;
        private BigDecimal longitude;
    }

    /**
     * 导航指令
     */
    @Data
    public static class NavigationInstruction {
        private String instruction;
        private Double distance;
        private Integer duration;
        private String direction;
    }
}