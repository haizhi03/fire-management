package com.fire.management.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 设施创建请求DTO
 */
@Data
public class FacilityCreateRequest {

    /**
     * 设施名称
     */
    @NotBlank(message = "设施名称不能为空")
    private String facilityName;

    /**
     * 设施类型：1-消防栓，2-消防车
     */
    @NotNull(message = "设施类型不能为空")
    private Integer facilityType;

    /**
     * 经度
     */
    @NotNull(message = "经度不能为空")
    private BigDecimal longitude;

    /**
     * 纬度
     */
    @NotNull(message = "纬度不能为空")
    private BigDecimal latitude;

    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空")
    private String address;

    /**
     * 设施状态：1-正常，2-损坏，3-维修中
     */
    private Integer status = 1;

    /**
     * 工作压力(MPa)，仅消防栓
     */
    private BigDecimal pressure;

    /**
     * 特殊类型
     */
    private String specialType;

    /**
     * 照片URL列表
     */
    private List<String> photoUrls;

    /**
     * 照片描述列表
     */
    private List<String> photoDescs;
}
