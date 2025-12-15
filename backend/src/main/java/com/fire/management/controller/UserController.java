package com.fire.management.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fire.management.annotation.RequirePermission;
import com.fire.management.common.Result;
import com.fire.management.dto.UserCreateRequest;
import com.fire.management.entity.SysUser;
import com.fire.management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 分页查询用户列表
     */
    @GetMapping
    @RequirePermission("user:manage")
    public Result<Page<SysUser>> getUserList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<SysUser> page = userService.getUserList(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    @RequirePermission("user:manage")
    public Result<SysUser> getUserById(@PathVariable Long id) {
        SysUser user = userService.getUserById(id);
        return Result.success(user);
    }

    /**
     * 创建用户
     */
    @PostMapping
    @RequirePermission("user:manage")
    public Result<SysUser> createUser(@Validated @RequestBody UserCreateRequest request) {
        SysUser user = userService.createUser(request);
        return Result.success(user);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @RequirePermission("user:manage")
    public Result<SysUser> updateUser(@PathVariable Long id, 
                                      @Validated @RequestBody UserCreateRequest request) {
        SysUser user = userService.updateUser(id, request);
        return Result.success(user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @RequirePermission("user:manage")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/{id}/toggle-status")
    @RequirePermission("user:manage")
    public Result<Void> toggleUserStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return Result.success();
    }
}
