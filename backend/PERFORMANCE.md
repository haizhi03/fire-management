# 性能优化说明

## 1. 数据库优化

### 1.1 索引优化

系统已为所有关键查询字段创建索引：

#### fire_facility 表
- `idx_location (longitude, latitude)` - 空间查询优化
- `idx_audit_status (audit_status)` - 审核状态筛选
- `idx_collector (collector_id)` - 采集人员查询
- `idx_facility_type (facility_type)` - 设施类型筛选
- `idx_status (status)` - 设施状态筛选

#### sys_user 表
- `idx_username (username)` - 登录查询优化
- `idx_role_id (role_id)` - 角色关联查询

#### audit_record 表
- `idx_facility (facility_id)` - 设施审核记录查询
- `idx_auditor (auditor_id)` - 审核人查询
- `idx_audit_time (audit_time)` - 时间范围查询

#### operation_log 表
- `idx_user (user_id)` - 用户操作日志查询
- `idx_time (create_time)` - 时间范围查询
- `idx_type (operation_type)` - 操作类型筛选

### 1.2 查询优化

#### 分页查询
所有列表查询都使用 MyBatis-Plus 的分页插件：
```java
Page<FireFacility> page = new Page<>(pageNum, pageSize);
facilityMapper.selectPage(page, wrapper);
```

#### 空间查询优化
周边设施查询使用两步优化：
1. 使用边界框（Bounding Box）预筛选，利用位置索引
2. 精确计算 Haversine 距离进行二次过滤

```java
// 1. 计算边界框
double[] bbox = GeoUtil.getBoundingBox(latitude, longitude, radius);

// 2. 使用索引查询边界框内的数据
wrapper.between(FireFacility::getLatitude, bbox[0], bbox[1])
       .between(FireFacility::getLongitude, bbox[2], bbox[3]);

// 3. 精确距离计算和过滤
double distance = GeoUtil.calculateDistance(...);
if (distance <= radius) {
    // 添加到结果集
}
```

### 1.3 软删除优化
使用 MyBatis-Plus 的逻辑删除功能，自动过滤已删除数据：
```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

## 2. 缓存优化

### 2.1 Redis 缓存
系统已集成 Redis，可用于缓存热点数据：

#### 推荐缓存策略
```java
// 设施列表缓存（5分钟）
@Cacheable(value = "facility:list", key = "#pageNum + ':' + #pageSize", unless = "#result == null")
public Page<FireFacility> getFacilityList(int pageNum, int pageSize) {
    // ...
}

// 设施详情缓存（10分钟）
@Cacheable(value = "facility:detail", key = "#facilityId", unless = "#result == null")
public FacilityDetailVO getFacilityDetail(Long facilityId) {
    // ...
}

// 看板数据缓存（5分钟）
@Cacheable(value = "stats:dashboard", unless = "#result == null")
public DashboardVO getDashboardData() {
    // ...
}
```

#### 缓存失效策略
```java
// 更新设施时清除缓存
@CacheEvict(value = {"facility:list", "facility:detail"}, key = "#facilityId")
public void updateFacility(Long facilityId, FacilityCreateRequest request) {
    // ...
}
```

### 2.2 本地缓存
对于不经常变化的数据（如角色权限），可以使用本地缓存：
```java
@Cacheable(value = "role:permissions", key = "#roleId")
public List<String> getRolePermissions(Long roleId) {
    // ...
}
```

## 3. 文件处理优化

### 3.1 图片压缩
上传的照片自动压缩到 1080P，减少存储空间和传输带宽：
```java
// FileService.compressImage()
// 最大宽度：1920px
// 最大高度：1080px
```

### 3.2 文件上传限制
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB      # 单个文件最大 10MB
      max-request-size: 20MB   # 请求总大小最大 20MB
```

### 3.3 静态资源优化
- 使用独立的文件服务器或对象存储（如阿里云 OSS）
- 配置 CDN 加速静态资源访问
- 启用浏览器缓存

## 4. 前端优化

### 4.1 图片懒加载
设施列表和地图标注使用懒加载，减少初始加载时间：
```vue
<img v-lazy="photo.photoUrl" />
```

### 4.2 虚拟滚动
大数据量列表使用虚拟滚动，只渲染可见区域：
```vue
<el-table-v2 :data="facilities" />
```

### 4.3 防抖和节流
- 搜索输入使用防抖（debounce）
- 地图缩放/平移使用节流（throttle）

```javascript
// 搜索防抖
const searchDebounced = debounce(search, 500);

// 地图事件节流
const onMapMove = throttle(updateMarkers, 200);
```

## 5. API 优化

### 5.1 响应时间要求
根据需求 10.1，常规查询操作应在 2 秒内返回结果。

### 5.2 批量操作
支持批量审核、批量删除等操作，减少网络请求次数。

### 5.3 字段筛选
API 支持字段筛选，只返回需要的字段：
```java
// 列表查询只返回必要字段
wrapper.select(FireFacility::getFacilityId, 
               FireFacility::getFacilityName,
               FireFacility::getAddress);
```

## 6. 并发优化

### 6.1 连接池配置
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20        # 最大连接数
      minimum-idle: 5              # 最小空闲连接
      connection-timeout: 30000    # 连接超时（毫秒）
      idle-timeout: 600000         # 空闲超时（毫秒）
```

### 6.2 线程池配置
```java
@Configuration
public class ThreadPoolConfig {
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
```

### 6.3 异步处理
耗时操作使用异步处理：
```java
@Async
public void processDeduplication() {
    // 去重处理
}

@Async
public void generateReport() {
    // 报表生成
}
```

## 7. 监控与调优

### 7.1 性能监控
推荐使用以下工具：
- Spring Boot Actuator：应用健康检查
- Prometheus + Grafana：指标监控
- SkyWalking：分布式追踪

### 7.2 慢查询日志
启用 MySQL 慢查询日志：
```sql
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;  -- 超过2秒的查询记录
```

### 7.3 JVM 调优
生产环境 JVM 参数建议：
```bash
java -Xms2g -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/logs/heapdump.hprof \
     -jar fire-management-system.jar
```

## 8. 性能测试

### 8.1 并发测试
使用 JMeter 进行并发测试，验证系统支持至少 100 个并发用户（需求 10.2）。

### 8.2 压力测试
测试场景：
- 设施列表查询：1000 QPS
- 设施详情查询：500 QPS
- 周边设施查询：200 QPS
- 数据采集：100 QPS

### 8.3 性能基准
| 操作 | 响应时间 | 并发数 |
|------|---------|--------|
| 登录 | < 500ms | 100 |
| 设施列表查询 | < 1s | 100 |
| 设施详情查询 | < 500ms | 100 |
| 周边设施查询 | < 2s | 50 |
| 数据采集 | < 1s | 50 |
| 看板数据 | < 2s | 20 |

## 9. 扩展性

### 9.1 水平扩展
系统支持水平扩展（需求 10.5）：
- 无状态设计（使用 JWT）
- 使用负载均衡（Nginx/HAProxy）
- 数据库读写分离
- Redis 集群

### 9.2 数据库分片
当数据量增长到一定规模时，可以考虑：
- 按地区分片
- 按时间分片（历史数据归档）

## 10. 优化检查清单

部署前检查：

- [x] 数据库索引已创建
- [x] 分页查询已实现
- [x] 图片压缩已启用
- [ ] Redis 缓存已配置
- [ ] 慢查询日志已启用
- [ ] 连接池参数已优化
- [ ] JVM 参数已调优
- [ ] 性能测试已完成
- [ ] 监控系统已部署
