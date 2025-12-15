# 设计文档

## 概述

消防设施管理系统是一个基于"云-管-端"混合架构的综合性管理平台，旨在解决消防救援支队在消防设施数据管理、现场查询和应急响应方面的痛点。系统采用SpringBoot作为后端框架，Vue.js作为前端框架，MySQL作为数据库，支持移动端和Web端多终端访问。

系统核心功能包括：
- 用户认证与权限管理
- 消防设施数据采集（支持离线模式）
- 地图可视化与设施查询
- 数据审核与去重处理
- 统计分析与看板展示
- 离线数据同步

## 架构设计

### 总体架构

系统采用前后端分离的微服务架构，分为以下几层：

1. **表现层**：
   - Web管理端（Vue.js）：面向系统管理员，提供数据审核、用户管理、统计看板等功能
   - 移动端（Vue.js + Cordova/Capacitor）：面向数据采集员和消防战士，提供数据采集和设施查询功能

2. **应用层**：
   - API网关：统一入口，负责路由、认证、限流
   - 业务服务：用户服务、设施服务、审核服务、统计服务

3. **数据层**：
   - MySQL主数据库：存储核心业务数据
   - Redis缓存：缓存热点数据，提升查询性能
   - 本地SQLite：移动端离线数据存储

4. **基础设施层**：
   - 文件存储：存储设施照片
   - 地图服务：提供地图底图和空间查询能力

### 技术栈

- **后端**：Spring Boot 2.7+, Spring Security, MyBatis-Plus
- **前端**：Vue 3, Element Plus, Leaflet/Mapbox GL
- **数据库**：MySQL 8.0, Redis 6.0
- **移动端**：Vue 3 + Vant UI + Capacitor
- **构建工具**：Maven, Vite

## 组件与接口

### 核心组件

#### 1. 用户认证组件
- 负责用户登录、令牌管理、权限验证
- 使用JWT实现无状态认证
- 支持基于角色的访问控制（RBAC）

#### 2. 设施管理组件
- 提供设施的增删改查功能
- 支持空间查询（周边查询、范围查询）
- 集成去重算法，防止重复数据

#### 3. 数据采集组件
- 支持移动端数据采集
- 自动获取GPS位置
- 照片自动压缩和上传
- 离线数据本地存储

#### 4. 审核管理组件
- 提供待审核数据列表
- 支持审核通过、驳回操作
- 记录审核历史

#### 5. 地图可视化组件
- 集成地图SDK
- 设施标注和状态展示
- 路径规划功能

#### 6. 统计分析组件
- 数据聚合和指标计算
- 看板数据生成
- 报表导出

#### 7. 离线同步组件
- 网络状态检测
- 增量数据同步
- 断点续传支持

#### 8. 智能距离查询组件
- 可视化距离圈绘制
- 多单位距离计算和转换
- 实时距离更新
- 距离范围筛选
- 最近设施智能推荐

#### 9. 导航路径规划组件
- 多模式路径规划（步行、驾车、骑行）
- 实时导航和位置跟踪
- 语音导航提示
- 紧急模式优化

### 接口设计

#### RESTful API规范

所有API遵循RESTful设计原则，使用标准HTTP方法：
- GET：查询资源
- POST：创建资源
- PUT：更新资源
- DELETE：删除资源

#### 核心接口

**用户认证接口**
```
POST /api/auth/login - 用户登录
POST /api/auth/refresh - 刷新令牌
POST /api/auth/logout - 用户登出
```

**设施管理接口**
```
GET /api/facilities - 查询设施列表
GET /api/facilities/{id} - 查询设施详情
GET /api/facilities/nearby - 周边设施查询
POST /api/facilities - 创建设施
PUT /api/facilities/{id} - 更新设施
DELETE /api/facilities/{id} - 删除设施
```

**数据审核接口**
```
GET /api/audit/pending - 获取待审核列表
POST /api/audit/{id}/approve - 审核通过
POST /api/audit/{id}/reject - 审核驳回
```

**统计分析接口**
```
GET /api/stats/dashboard - 获取看板数据
GET /api/stats/export - 导出统计报表
```

**数据同步接口**
```
POST /api/sync/upload - 上传离线数据
GET /api/sync/check - 检查更新
```

**智能距离查询接口**
```
GET /api/facilities/nearby/enhanced - 增强周边查询（支持距离圈、单位转换）
GET /api/facilities/closest - 查询最近的N个设施
POST /api/facilities/distance-filter - 按距离范围筛选设施
GET /api/navigation/route - 获取路径规划方案
POST /api/navigation/start - 启动实时导航
```

## 数据模型

### 核心数据表

#### 用户表（sys_user）
```sql
CREATE TABLE sys_user (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(100) NOT NULL,
  real_name VARCHAR(20) NOT NULL,
  role_id BIGINT NOT NULL,
  phone VARCHAR(11),
  status TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0
);
```

#### 角色表（sys_role）
```sql
CREATE TABLE sys_role (
  role_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_name VARCHAR(30) UNIQUE NOT NULL,
  permissions VARCHAR(500),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 消防设施表（fire_facility）
```sql
CREATE TABLE fire_facility (
  facility_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  facility_name VARCHAR(100) NOT NULL,
  facility_type TINYINT NOT NULL COMMENT '1-消防栓, 2-消防车',
  longitude DECIMAL(10,6) NOT NULL,
  latitude DECIMAL(10,6) NOT NULL,
  address VARCHAR(200) NOT NULL,
  status TINYINT DEFAULT 1 COMMENT '1-正常, 2-损坏, 3-维修中',
  pressure DECIMAL(5,2) COMMENT '工作压力(MPa)',
  collector_id BIGINT NOT NULL,
  audit_status TINYINT DEFAULT 0 COMMENT '0-待审核, 1-已通过, 2-已驳回',
  is_merged TINYINT DEFAULT 0,
  merged_to BIGINT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  INDEX idx_location (longitude, latitude),
  INDEX idx_audit_status (audit_status),
  INDEX idx_collector (collector_id)
);
```

#### 设施照片表（fire_facility_photo）
```sql
CREATE TABLE fire_facility_photo (
  photo_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  facility_id BIGINT NOT NULL,
  photo_url VARCHAR(500) NOT NULL,
  photo_desc VARCHAR(100),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_facility (facility_id)
);
```

#### 审核记录表（audit_record）
```sql
CREATE TABLE audit_record (
  record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  facility_id BIGINT NOT NULL,
  auditor_id BIGINT NOT NULL,
  audit_result TINYINT NOT NULL COMMENT '1-通过, 2-驳回',
  reject_reason VARCHAR(200),
  audit_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_facility (facility_id)
);
```

#### 操作日志表（operation_log）
```sql
CREATE TABLE operation_log (
  log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  operation_type VARCHAR(50) NOT NULL,
  operation_content VARCHAR(500),
  ip_address VARCHAR(50),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user (user_id),
  INDEX idx_time (create_time)
);
```

## 正确性属性

*属性是系统在所有有效执行中应该保持为真的特征或行为——本质上是关于系统应该做什么的形式化陈述。属性作为人类可读规范和机器可验证正确性保证之间的桥梁。*

### 属性 1：登录令牌有效性
*对于任何*有效的用户凭证（用户名和密码），当用户登录时，系统应返回有效的JWT令牌，该令牌可用于访问受保护的资源。
**验证需求：1.1**

### 属性 2：无效凭证拒绝
*对于任何*无效的用户凭证（错误的用户名或密码），系统应拒绝登录请求并返回明确的错误信息。
**验证需求：1.2**

### 属性 3：用户创建完整性
*对于任何*新创建的用户，系统应确保用户记录包含角色ID和加密后的密码。
**验证需求：1.4**

### 属性 4：权限访问控制
*对于任何*用户尝试访问的功能，如果用户角色不具备该功能的权限，系统应拒绝访问并返回权限不足的提示。
**验证需求：1.5**

### 属性 5：GPS位置关联
*对于任何*采集的消防栓照片，系统应自动获取并关联GPS位置信息（经度和纬度）。
**验证需求：2.1**

### 属性 6：必填字段验证
*对于任何*提交的设施信息表单，如果缺少必填字段（位置、照片、压力值），系统应拒绝提交并提示缺失的字段。
**验证需求：2.2**

### 属性 7：离线数据本地存储
*对于任何*在离线状态下采集的数据，系统应将数据保存到本地SQLite数据库。
**验证需求：2.3**

### 属性 8：自动数据同步
*对于任何*网络恢复后的设备，系统应自动检测并同步本地数据到服务器。
**验证需求：2.4**

### 属性 9：待审核状态标记
*对于任何*新提交的采集数据，系统应将其审核状态标记为待审核（audit_status = 0）。
**验证需求：2.5**

### 属性 10：设施详情完整性
*对于任何*设施的详情页面，系统应显示所有必需字段：地址、压力、状态和照片URL。
**验证需求：3.2**

### 属性 11：空间范围查询
*对于任何*位置和半径的查询请求，系统返回的所有设施应位于指定位置的半径范围内（使用Haversine公式计算距离）。
**验证需求：3.3**

### 属性 12：离线查询缓存
*对于任何*在离线模式下的查询请求，系统应从本地SQLite数据库返回结果。
**验证需求：3.4**

### 属性 13：路径规划有效性
*对于任何*有效的起点和终点坐标，系统应返回一条可行的步行路径。
**验证需求：3.5**

### 属性 14：待审核数据过滤
*对于任何*待审核列表查询，系统返回的所有数据应具有待审核状态（audit_status = 0）。
**验证需求：4.1**

### 属性 15：审核状态更新
*对于任何*审核通过的数据，系统应将其审核状态更新为已通过（audit_status = 1）。
**验证需求：4.2**

### 属性 16：驳回原因必填
*对于任何*驳回操作，系统应要求填写驳回原因，否则拒绝操作。
**验证需求：4.3**

### 属性 17：修改历史记录
*对于任何*设施信息的修改操作，系统应在操作日志表中创建一条记录，包含操作人和修改内容。
**验证需求：4.4**

### 属性 18：软删除实现
*对于任何*删除设施的操作，系统应将deleted字段标记为1，而不是物理删除记录。
**验证需求：4.5**

### 属性 19：空间去重判定
*对于任何*新采集的消防栓，如果其位置与已有消防栓的距离小于10米，系统应判定为重复数据。
**验证需求：5.1**

### 属性 20：去重记录保留
*对于任何*检测到的重复数据组，系统应保留信息最完整的记录，并将其他记录的is_merged字段标记为1。
**验证需求：5.2, 5.3**

### 属性 21：合并操作日志
*对于任何*去重合并操作，系统应在操作日志表中记录合并的详细信息。
**验证需求：5.5**

### 属性 22：看板数据完整性
*对于任何*看板页面请求，系统应返回所有必需的指标：设施总数、区域分布、状态比例。
**验证需求：6.1**

### 属性 23：热力图数据计算
*对于任何*区域，热力图数据应正确反映该区域内的设施密度（设施数量/区域面积）。
**验证需求：6.2**

### 属性 24：时间范围过滤
*对于任何*指定的时间范围，系统返回的采集趋势数据应只包含该时间范围内创建的设施。
**验证需求：6.3**

### 属性 25：Excel导出格式
*对于任何*导出操作，系统生成的文件应为有效的Excel格式（.xlsx），可被Excel软件正常打开。
**验证需求：6.4**

### 属性 26：离线模式切换
*对于任何*网络断开事件，系统应自动检测并切换到离线模式，显示离线状态提示。
**验证需求：7.1**

### 属性 27：离线操作记录
*对于任何*在离线模式下的操作，系统应将操作记录保存到本地数据库的operation_log表。
**验证需求：7.2**

### 属性 28：自动同步触发
*对于任何*网络恢复事件，系统应自动触发数据同步流程。
**验证需求：7.3**

### 属性 29：增量数据同步
*对于任何*同步操作，系统应只上传本地数据库中sync_status为0（未同步）的记录。
**验证需求：7.4**

### 属性 30：断点续传支持
*对于任何*中断的同步操作，当重新同步时，系统应从上次中断的位置继续，而不是重新开始。
**验证需求：7.5**

### 属性 31：状态颜色映射
*对于任何*设施图标，系统应根据设施状态使用正确的颜色：正常-绿色、损坏-红色、维修中-黄色。
**验证需求：8.3**

### 属性 32：信息窗口触发
*对于任何*设施图标的点击事件，系统应弹出信息窗口并显示设施的基本信息。
**验证需求：8.4**

### 属性 33：照片压缩处理
*对于任何*上传的照片，如果分辨率高于1080P，系统应自动压缩到1080P分辨率。
**验证需求：9.1**

### 属性 34：文件存储成功
*对于任何*上传的照片，系统应将文件存储到文件系统并返回可访问的URL。
**验证需求：9.2**

### 属性 35：照片关联查询
*对于任何*设施详情查询，系统应返回该设施关联的所有照片URL。
**验证需求：9.3**

### 属性 36：上传重试机制
*对于任何*失败的照片上传操作，系统应提供重试功能，允许用户重新上传。
**验证需求：9.4**

### 属性 37：级联删除照片
*对于任何*删除的设施，系统应同时删除该设施关联的所有照片记录。
**验证需求：9.5**

### 属性 38：错误日志记录
*对于任何*系统异常或错误，系统应在operation_log表中创建错误日志记录，包含错误类型和详细信息。
**验证需求：11.3**

### 属性 39：距离圈可视化准确性
*对于任何*用户设置的查询半径和中心位置，系统显示的距离圈内的所有设施都应该在指定半径范围内。
**验证需求：10.1**

### 属性 40：距离单位转换正确性
*对于任何*距离值，当用户在米和公里单位之间切换时，转换后的数值应该保持数学上的等价性（1公里=1000米）。
**验证需求：10.2**

### 属性 41：实时距离计算准确性
*对于任何*用户位置和设施位置，系统显示的距离应该与使用Haversine公式计算的结果一致（误差不超过1米）。
**验证需求：10.3**

### 属性 42：距离范围筛选完整性
*对于任何*指定的距离范围筛选条件，返回的所有设施都应该在该距离范围内，且范围内的所有设施都应该被返回。
**验证需求：10.4**

### 属性 43：多模式路径规划可用性
*对于任何*有效的起点和终点坐标，系统应该能够返回步行、驾车、骑行三种路径规划方案，每种方案都包含有效的路径数据。
**验证需求：10.5**

### 属性 44：最近设施查询准确性
*对于任何*用户位置和设施集合，系统返回的最近3个设施应该确实是距离最近的，并且按距离从近到远排序。
**验证需求：10.7**

### 属性 45：紧急模式设施筛选
*对于任何*紧急模式查询，系统返回的所有设施都应该具有正常状态（status=1），并且按距离从近到远排序。
**验证需求：10.8**

## 错误处理

### 全局异常处理

系统使用Spring Boot的@ControllerAdvice实现全局异常处理，统一返回格式：

```json
{
  "code": 错误码,
  "message": "错误描述",
  "data": null,
  "timestamp": 时间戳
}
```

### 错误码规范

- 200：成功
- 400：请求参数错误
- 401：未授权（令牌无效或过期）
- 403：权限不足
- 404：资源不存在
- 500：服务器内部错误
- 5001：业务逻辑异常
- 5002：文件上传失败
- 5003：数据同步失败

### 特定场景错误处理

1. **GPS定位失败**：提示用户检查GPS设置，允许手动输入坐标
2. **网络异常**：自动切换到离线模式，使用本地缓存数据
3. **数据库操作失败**：记录详细日志，返回"系统繁忙，请稍后重试"
4. **文件上传失败**：提供重试机制，支持断点续传

## 测试策略

### 单元测试

使用JUnit 5和Mockito进行单元测试，覆盖：
- Service层业务逻辑
- Controller层接口
- 工具类和算法

### 属性测试

使用JUnit-Quickcheck进行属性测试，每个属性测试运行至少100次迭代：
- 每个属性测试必须使用注释标记对应的设计文档属性
- 标记格式：`// Feature: fire-management-system, Property X: [属性描述]`
- 每个正确性属性必须由单个属性测试实现

### 集成测试

使用Spring Boot Test进行集成测试，覆盖：
- API接口端到端测试
- 数据库操作测试
- 文件上传下载测试

### 性能测试

使用JMeter进行性能测试，验证：
- 查询响应时间 < 2秒
- 并发用户支持 >= 100
- 数据同步效率

## 安全设计

### 认证与授权

- 使用JWT实现无状态认证
- 令牌有效期：移动端7天，Web端30分钟
- 密码使用BCrypt加密存储
- 实现基于角色的访问控制（RBAC）

### 数据安全

- 所有API通信使用HTTPS
- 敏感数据加密存储
- SQL注入防护（使用参数化查询）
- XSS攻击防护（输入验证和输出转义）

### 审计日志

- 记录所有关键操作（登录、数据修改、审核等）
- 日志包含：操作人、操作时间、操作内容、IP地址
- 日志保留30天

## 部署架构

### 开发环境
- 单机部署，所有服务运行在同一台服务器
- 使用内嵌数据库进行快速开发

### 生产环境
- 前后端分离部署
- 后端服务部署在应用服务器（Tomcat/Nginx）
- 数据库独立部署，配置主从复制
- Redis独立部署，配置持久化
- 文件存储使用独立的文件服务器或对象存储服务

### 智能距离查询技术实现

#### 距离计算优化
- 使用Haversine公式进行精确距离计算
- 实现边界框（Bounding Box）预筛选优化查询性能
- 支持地理空间索引（GeoSpatial Index）加速范围查询

#### 可视化距离圈
- 使用Leaflet/Mapbox GL的Circle图层绘制距离圈
- 支持动态半径调整和实时更新
- 实现距离圈与设施标注的交互联动

#### 路径规划集成
- 集成高德地图/百度地图路径规划API
- 支持多种出行方式的路径计算
- 实现路径可视化和导航指引

#### 性能优化策略
- 使用Redis缓存热点区域的设施数据
- 实现增量更新机制，减少不必要的计算
- 采用Web Worker进行复杂距离计算，避免UI阻塞

### 扩展性考虑
- 支持水平扩展，通过负载均衡分发请求
- 数据库支持分库分表
- 缓存支持集群模式
