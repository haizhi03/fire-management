package com.fire.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设施照片实体
 */
@Data
@TableName("fire_facility_photo")
public class FireFacilityPhoto implements Serializable {

    /**
     * 照片ID
     */
    @TableId(type = IdType.AUTO)
    private Long photoId;

    /**
     * 设施ID
     */
    private Long facilityId;

    /**
     * 照片URL
     */
    private String photoUrl;

    /**
     * 照片描述
     */
    private String photoDesc;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
