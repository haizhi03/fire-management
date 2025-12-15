package com.fire.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fire.management.dto.UserCreateRequest;
import com.fire.management.entity.SysUser;
import com.fire.management.mapper.SysUserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务
 */
@Service
public class UserService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户列表
     */
    public Page<SysUser> getUserList(int pageNum, int pageSize, String keyword) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysUser::getUsername, keyword)
                   .or()
                   .like(SysUser::getRealName, keyword);
        }
        
        return userMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID查询用户
     */
    public SysUser getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 创建用户
     */
    public SysUser createUser(UserCreateRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        BeanUtils.copyProperties(request, user);
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1); // 默认启用

        userMapper.insert(user);
        return user;
    }

    /**
     * 更新用户
     */
    public SysUser updateUser(Long userId, UserCreateRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新用户信息
        user.setRealName(request.getRealName());
        user.setRoleId(request.getRoleId());
        user.setPhone(request.getPhone());

        // 如果提供了新密码，则更新密码
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userMapper.updateById(user);
        return user;
    }

    /**
     * 删除用户（软删除）
     */
    public void deleteUser(Long userId) {
        userMapper.deleteById(userId);
    }

    /**
     * 启用/禁用用户
     */
    public void toggleUserStatus(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userMapper.updateById(user);
    }
}
