package com.fire.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fire.management.entity.AuditRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核记录Mapper
 */
@Mapper
public interface AuditRecordMapper extends BaseMapper<AuditRecord> {
}
