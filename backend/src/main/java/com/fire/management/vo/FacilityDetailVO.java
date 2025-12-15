package com.fire.management.vo;

import com.fire.management.entity.FireFacilityPhoto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 设施详情VO
 */
@Data
public class FacilityDetailVO {

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
     * 特殊类型
     */
    private String specialType;

    /**
     * 采集人员ID
     */
    private Long collectorId;

    /**
     * 采集人员姓名
     */
    private String collectorName;

    /**
     * 审核状态：0-待审核，1-已通过，2-已驳回
     */
    private Integer auditStatus;

    /**
     * 照片列表
     */
    private List<FireFacilityPhoto> photos;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
