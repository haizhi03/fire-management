package com.fire.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审核记录实体
 */
@Data
@TableName("audit_record")
public class AuditRecord implements Serializable {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long recordId;

    /**
     * 设施ID
     */
    private Long facilityId;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核结果：1-通过，2-驳回
     */
    private Integer auditResult;

    /**
     * 驳回原因
     */
    private String rejectReason;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
}
