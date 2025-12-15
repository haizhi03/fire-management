package com.fire.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fire.management.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
