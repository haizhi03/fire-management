# 地图功能配置说明

## 高德地图Key配置

地图功能使用高德地图API，需要配置API Key才能正常使用。

### 获取高德地图Key

1. 访问高德开放平台：https://lbs.amap.com/
2. 注册并登录账号
3. 进入控制台 -> 应用管理 -> 我的应用
4. 创建新应用，添加Key
5. 选择"Web端(JS API)"类型
6. 复制生成的Key

### 配置Key

打开 `web-admin/src/views/Map.vue` 文件，找到以下代码：

```javascript
const AMap = await AMapLoader.load({
  key: 'YOUR_AMAP_KEY', // 需要替换为实际的高德地图key
  version: '2.0',
  plugins: ['AMap.Geolocation', 'AMap.Marker', 'AMap.InfoWindow']
})
```

将 `YOUR_AMAP_KEY` 替换为你申请的高德地图Key。

## 安装依赖

在 `web-admin` 目录下运行：

```bash
npm install
```

## 功能说明

### 地图查询功能

- **设施标注**：在地图上显示所有消防设施位置
  - 绿色圆点：正常状态
  - 红色圆点：损坏状态
  - 黄色圆点：维修中状态

- **周边查询**：根据当前位置查询指定半径内的设施
  - 支持500米、1公里、2公里、5公里范围查询
  - 可按设施类型筛选

- **设施详情**：点击地图标记查看设施详细信息
  - 显示设施名称、类型、地址、状态等
  - 支持导航到设施位置

- **统计信息**：实时显示地图上设施的统计数据
  - 正常设施数量
  - 损坏设施数量
  - 维修中设施数量
  - 总计数量

## 注意事项

1. 高德地图Key有调用次数限制，请根据实际需求选择合适的套餐
2. 地图功能需要浏览器支持定位功能
3. 首次使用时浏览器会请求位置权限，请允许
4. 如果无法获取当前位置，地图会使用默认位置（北京）

## 后端接口

地图功能使用以下后端接口：

- `GET /api/facilities` - 获取设施列表
- `GET /api/facilities/{id}` - 获取设施详情
- `GET /api/facilities/nearby` - 查询周边设施
  - 参数：latitude（纬度）、longitude（经度）、radius（半径，米）、facilityType（设施类型，可选）
