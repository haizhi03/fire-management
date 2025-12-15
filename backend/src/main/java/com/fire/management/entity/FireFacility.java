package com.fire.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fire.management.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 消防设施实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fire_facility")
public class FireFacility extends BaseEntity {

    /**
     * 设施ID
     */
    @TableId(type = IdType.AUTO)
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
     * 工作压力(MPa)，仅消防栓
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
     * 审核状态：0-待审核，1-已通过，2-已驳回
     */
    private Integer auditStatus;

    /**
     * 是否已合并：0-否，1-是
     */
    private Integer isMerged;

    /**
     * 合并到的设施ID
     */
    private Long mergedTo;
}
