package com.fire.management.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 周边设施VO
 */
@Data
public class FacilityNearbyVO {

    /**
     * 设施ID
     */
    private Long facilityId;

    /**
     * 设施名称
     */
    private String facilityName;

    /**
     * 设施类型：1-消防栓，2-消防车
     */
    private Integer facilityType;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 设施状态：1-正常，2-损坏，3-维修中
     */
    private Integer status;

    /**
     * 工作压力(MPa)
     */
    private BigDecimal pressure;

    /**
     * 距离（米）
     */
    private Double distance;

    /**
     * 主照片URL
     */
    private String mainPhotoUrl;
}
