package com.fire.management.utils;

import java.math.BigDecimal;

/**
 * 地理位置工具类
 */
public class GeoUtil {

    /**
     * 地球半径（米）
     */
    private static final double EARTH_RADIUS = 6371000;

    /**
     * 使用Haversine公式计算两点之间的距离（米）
     * 
     * @param lat1 第一个点的纬度
     * @param lon1 第一个点的经度
     * @param lat2 第二个点的纬度
     * @param lon2 第二个点的经度
     * @return 两点之间的距离（米）
     */
    public static double calculateDistance(BigDecimal lat1, BigDecimal lon1, 
                                          BigDecimal lat2, BigDecimal lon2) {
        return calculateDistance(
            lat1.doubleValue(), lon1.doubleValue(),
            lat2.doubleValue(), lon2.doubleValue()
        );
    }

    /**
     * 使用Haversine公式计算两点之间的距离（米）
     * 
     * @param lat1 第一个点的纬度
     * @param lon1 第一个点的经度
     * @param lat2 第二个点的纬度
     * @param lon2 第二个点的经度
     * @return 两点之间的距离（米）
     */
    public static double calculateDistance(double lat1, double lon1, 
                                          double lat2, double lon2) {
        // 将角度转换为弧度
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // 计算差值
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Haversine公式
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 返回距离（米）
        return EARTH_RADIUS * c;
    }

    /**
     * 判断两点之间的距离是否小于指定距离
     * 
     * @param lat1 第一个点的纬度
     * @param lon1 第一个点的经度
     * @param lat2 第二个点的纬度
     * @param lon2 第二个点的经度
     * @param maxDistance 最大距离（米）
     * @return 如果距离小于maxDistance返回true，否则返回false
     */
    public static boolean isWithinDistance(BigDecimal lat1, BigDecimal lon1,
                                          BigDecimal lat2, BigDecimal lon2,
                                          double maxDistance) {
        double distance = calculateDistance(lat1, lon1, lat2, lon2);
        return distance <= maxDistance;
    }

    /**
     * 计算给定点周围指定半径的边界框
     * 返回数组：[minLat, maxLat, minLon, maxLon]
     * 
     * @param lat 中心点纬度
     * @param lon 中心点经度
     * @param radius 半径（米）
     * @return 边界框坐标数组
     */
    public static double[] getBoundingBox(double lat, double lon, double radius) {
        // 纬度变化（1度约111km）
        double latChange = radius / 111000.0;
        
        // 经度变化（随纬度变化）
        double lonChange = radius / (111000.0 * Math.cos(Math.toRadians(lat)));

        return new double[]{
            lat - latChange,  // minLat
            lat + latChange,  // maxLat
            lon - lonChange,  // minLon
            lon + lonChange   // maxLon
        };
    }

    /**
     * 距离单位转换
     * 
     * @param distance 距离值
     * @param fromUnit 源单位（"m" 或 "km"）
     * @param toUnit 目标单位（"m" 或 "km"）
     * @return 转换后的距离
     */
    public static double convertDistance(double distance, String fromUnit, String toUnit) {
        if (fromUnit == null || toUnit == null) {
            return distance;
        }
        
        // 先转换为米
        double distanceInMeters = distance;
        if ("km".equalsIgnoreCase(fromUnit)) {
            distanceInMeters = distance * 1000;
        }
        
        // 再转换为目标单位
        if ("km".equalsIgnoreCase(toUnit)) {
            return distanceInMeters / 1000;
        }
        
        return distanceInMeters;
    }

    /**
     * 格式化距离显示
     * 
     * @param distance 距离（米）
     * @param unit 显示单位（"m" 或 "km"）
     * @return 格式化后的距离字符串
     */
    public static String formatDistance(double distance, String unit) {
        if ("km".equalsIgnoreCase(unit)) {
            double km = distance / 1000;
            return String.format("%.2f km", km);
        } else {
            return String.format("%.0f m", distance);
        }
    }

    /**
     * 智能距离格式化（自动选择合适的单位）
     * 
     * @param distance 距离（米）
     * @return 格式化后的距离字符串
     */
    public static String smartFormatDistance(double distance) {
        if (distance >= 1000) {
            return String.format("%.2f km", distance / 1000);
        } else {
            return String.format("%.0f m", distance);
        }
    }
}
