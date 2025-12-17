# Implementation Plan

- [-] 1. 创建验证工具函数



  - [x] 1.1 创建 `validateFileSize` 文件大小验证函数

    - 验证文件大小是否超过 5MB 限制
    - 返回 boolean 表示是否有效
    - _Requirements: 3.3_
  - [x] 1.2 编写 `validateFileSize` 属性测试


    - **Property 1: 文件大小验证正确性**
    - **Validates: Requirements 3.3**

  - [ ] 1.3 创建 `validatePhotoCount` 照片数量验证函数
    - 验证当前照片数量是否达到上限 5 张
    - 返回 boolean 表示是否可以继续上传
    - _Requirements: 3.4_

  - [ ] 1.4 编写 `validatePhotoCount` 属性测试
    - **Property 2: 照片数量验证正确性**

    - **Validates: Requirements 3.4**
  - [ ] 1.5 创建 `validateRequiredFields` 表单验证函数
    - 验证所有必填字段是否已填写
    - 返回验证结果和错误信息列表
    - _Requirements: 4.1_

  - [ ] 1.6 编写 `validateRequiredFields` 属性测试
    - **Property 3: 表单必填字段验证正确性**
    - **Validates: Requirements 4.1**

- [ ] 2. 创建数据采集页面基础结构
  - [ ] 2.1 创建 DataCollection.vue 页面组件
    - 创建页面基础布局（标签页切换：新增设施/采集记录）
    - 添加路由配置
    - 添加侧边栏菜单项
    - _Requirements: 1.1_
  - [ ] 2.2 实现设施信息表单
    - 添加设施名称、类型、地址、经纬度、状态、压力等表单项
    - 配置表单验证规则
    - _Requirements: 1.2, 1.3, 1.4_

- [ ] 3. 实现地图选点功能
  - [ ] 3.1 创建地图选点对话框组件
    - 集成高德地图
    - 实现点击地图获取经纬度
    - 实现逆地理编码获取地址
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [ ] 4. 实现照片上传功能
  - [ ] 4.1 实现照片上传组件
    - 使用 el-upload 组件
    - 集成文件大小和数量验证
    - 实现照片预览和删除
    - _Requirements: 3.1, 3.2, 3.5_

- [ ] 5. 实现表单提交功能
  - [ ] 5.1 实现表单提交逻辑
    - 调用验证函数验证表单
    - 调用 API 提交数据
    - 处理成功和失败响应
    - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [ ] 6. 实现采集记录功能
  - [ ] 6.1 实现采集记录列表
    - 获取当前用户的采集记录
    - 显示设施名称、类型、时间、审核状态
    - 显示驳回原因（如有）
    - _Requirements: 5.1, 5.2, 5.3_
  - [ ] 6.2 编写用户记录过滤属性测试
    - **Property 4: 用户记录过滤正确性**
    - **Validates: Requirements 5.1**

- [ ] 7. Checkpoint - 确保所有测试通过
  - Ensure all tests pass, ask the user if questions arise.
