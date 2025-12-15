package com.fire.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fire.management.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long userId;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 加密后的密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 状态：1-正常，0-禁用
     */
    private Integer status;
}
