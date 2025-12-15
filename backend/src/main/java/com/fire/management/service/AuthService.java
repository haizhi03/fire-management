package com.fire.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fire.management.dto.LoginRequest;
import com.fire.management.entity.SysRole;
import com.fire.management.entity.SysUser;
import com.fire.management.mapper.SysRoleMapper;
import com.fire.management.mapper.SysUserMapper;
import com.fire.management.utils.JwtUtil;
import com.fire.management.vo.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 */
@Service
public class AuthService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        SysUser user = userMapper.selectOne(wrapper);

        // 验证用户是否存在
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 验证用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 查询角色信息
        SysRole role = roleMapper.selectById(user.getRoleId());

        // 生成token
        String token;
        if ("web".equals(request.getClientType())) {
            token = jwtUtil.generateWebToken(user.getUserId(), user.getUsername(), user.getRoleId());
        } else {
            token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRoleId());
        }

        // 返回登录信息
        return new LoginResponse(
            token,
            user.getUserId(),
            user.getUsername(),
            user.getRealName(),
            user.getRoleId(),
            role != null ? role.getRoleName() : null
        );
    }

    /**
     * 刷新令牌
     */
    public LoginResponse refreshToken(String token) {
        // 验证token
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("令牌无效或已过期");
        }

        // 从token中获取用户信息
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        Long roleId = jwtUtil.getRoleIdFromToken(token);

        // 查询用户
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new RuntimeException("用户不存在或已被禁用");
        }

        // 查询角色
        SysRole role = roleMapper.selectById(roleId);

        // 生成新token（默认移动端）
        String newToken = jwtUtil.generateToken(userId, username, roleId);

        return new LoginResponse(
            newToken,
            userId,
            username,
            user.getRealName(),
            roleId,
            role != null ? role.getRoleName() : null
        );
    }
}
