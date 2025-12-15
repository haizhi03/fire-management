package com.fire.management.interceptor;

import com.fire.management.annotation.RequirePermission;
import com.fire.management.entity.SysRole;
import com.fire.management.mapper.SysRoleMapper;
import com.fire.management.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * 权限拦截器
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SysRoleMapper roleMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是方法处理器，直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 获取方法上的权限注解
        RequirePermission annotation = handlerMethod.getMethodAnnotation(RequirePermission.class);
        
        // 如果没有权限注解，直接放行
        if (annotation == null) {
            return true;
        }

        // 获取需要的权限
        String requiredPermission = annotation.value();

        // 从请求头获取token
        String token = getTokenFromRequest(request);
        if (!StringUtils.hasText(token) || !jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"未授权\"}");
            return false;
        }

        // 获取用户角色ID
        Long roleId = jwtUtil.getRoleIdFromToken(token);
        
        // 查询角色权限
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
            return false;
        }

        // 检查权限
        String permissions = role.getPermissions();
        if (!StringUtils.hasText(permissions)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
            return false;
        }

        List<String> permissionList = Arrays.asList(permissions.split(","));
        if (!permissionList.contains(requiredPermission)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
            return false;
        }

        return true;
    }

    /**
     * 从请求头中获取token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
