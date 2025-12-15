# 地图功能故障排查

## 问题：地图不显示

### 可能原因和解决方案：

#### 1. 浏览器控制台检查
打开浏览器开发者工具（F12），查看Console标签页是否有错误信息。

#### 2. 网络连接检查
- 确保能访问高德地图API：https://webapi.amap.com
- 检查Network标签页，确认地图资源加载成功

#### 3. API Key配置
当前使用的是测试Key，有以下限制：
- 每日调用次数限制
- 可能被限流

**建议申请自己的Key：**
1. 访问 https://lbs.amap.com/
2. 注册账号并创建应用
3. 获取Web端(JS API) Key
4. 修改 `web-admin/index.html` 中的Key：
   ```html
   <script src="https://webapi.amap.com/maps?v=2.0&key=YOUR_KEY"></script>
   ```

#### 4. 数据检查
确保后端有设施数据：
- 访问 http://localhost:8080/api/facilities
- 检查是否返回设施列表
- 确认设施有经纬度数据

#### 5. 浏览器兼容性
- 推荐使用Chrome、Edge、Firefox最新版本
- 确保浏览器支持ES6+

#### 6. 端口和代理配置
检查 `web-admin/vite.config.js` 中的代理配置：
```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

## 常见错误信息

### "地图加载超时"
- 检查网络连接
- 检查防火墙设置
- 尝试刷新页面

### "无法获取当前位置"
- 浏览器需要HTTPS或localhost才能使用定位
- 检查浏览器位置权限设置
- 系统会使用默认位置（北京）

### "已加载 0 个设施"
- 检查后端是否运行
- 检查数据库是否有数据
- 检查API接口是否正常

## 调试步骤

1. **检查地图容器**
   ```javascript
   console.log(document.getElementById('amap-container'))
   // 应该返回一个div元素
   ```

2. **检查AMap对象**
   ```javascript
   console.log(window.AMap)
   // 应该返回AMap对象
   ```

3. **检查地图实例**
   打开Vue DevTools，查看Map组件的map.value是否有值

4. **检查API响应**
   在Network标签页查看 `/api/facilities` 请求的响应

## 测试数据

如果数据库没有数据，可以手动插入测试数据：

```sql
INSERT INTO fire_facility (facility_name, facility_type, longitude, latitude, address, status, pressure, collector_id, audit_status) 
VALUES 
('测试消防栓1', 1, 116.397428, 39.90923, '北京市东城区', 1, 0.5, 1, 1),
('测试消防栓2', 1, 116.407428, 39.91923, '北京市西城区', 1, 0.6, 1, 1),
('测试消防栓3', 1, 116.387428, 39.89923, '北京市朝阳区', 2, 0.4, 1, 1);
```

## 联系支持

如果以上方法都无法解决问题，请提供：
1. 浏览器控制台的完整错误信息
2. Network标签页的请求响应
3. 浏览器版本和操作系统信息
