# Requirements Document

## Introduction

本功能为消防设施管理系统添加数据采集上传模块，允许数据采集员通过 Web 端录入新的消防设施信息，包括设施基本信息、地理位置和现场照片，提交后进入待审核流程。

## Glossary

- **数据采集员**: 具有 roleId=2 的系统用户，负责现场采集消防设施数据
- **设施（Facility）**: 消防设施，包括消防栓和消防车两种类型
- **经纬度**: 设施的地理坐标，用于地图定位
- **待审核状态**: 新提交的设施数据默认状态，需要审核员审核通过后才正式生效

## Requirements

### Requirement 1

**User Story:** 作为数据采集员，我希望能够在系统中新增消防设施记录，以便将现场采集的数据录入系统。

#### Acceptance Criteria

1. WHEN 用户点击"新增设施"按钮 THEN 系统 SHALL 显示设施信息录入表单
2. WHEN 表单显示 THEN 系统 SHALL 包含设施名称、设施类型、地址、经度、纬度、状态、工作压力等必填字段
3. WHEN 用户选择设施类型 THEN 系统 SHALL 提供消防栓和消防车两个选项
4. WHEN 用户选择设施状态 THEN 系统 SHALL 提供正常、损坏、维修中三个选项

### Requirement 2

**User Story:** 作为数据采集员，我希望能够通过地图选点获取设施的经纬度，以便准确记录设施位置。

#### Acceptance Criteria

1. WHEN 用户点击"地图选点"按钮 THEN 系统 SHALL 显示地图选点对话框
2. WHEN 用户在地图上点击某个位置 THEN 系统 SHALL 自动获取该位置的经度和纬度并填入表单
3. WHEN 用户在地图上点击某个位置 THEN 系统 SHALL 通过逆地理编码自动获取地址信息
4. WHEN 地图选点完成 THEN 系统 SHALL 在地图上显示标记点确认位置

### Requirement 3

**User Story:** 作为数据采集员，我希望能够上传设施现场照片，以便提供设施状态的视觉证据。

#### Acceptance Criteria

1. WHEN 用户点击上传照片区域 THEN 系统 SHALL 允许选择本地图片文件
2. WHEN 用户选择图片 THEN 系统 SHALL 显示图片预览
3. WHEN 用户上传照片 THEN 系统 SHALL 限制单张图片大小不超过 5MB
4. WHEN 用户上传照片 THEN 系统 SHALL 允许上传最多 5 张照片
5. WHEN 照片上传成功 THEN 系统 SHALL 显示删除按钮允许移除已上传照片

### Requirement 4

**User Story:** 作为数据采集员，我希望提交的数据能够进入审核流程，以便确保数据质量。

#### Acceptance Criteria

1. WHEN 用户填写完表单并点击提交 THEN 系统 SHALL 验证所有必填字段
2. WHEN 表单验证失败 THEN 系统 SHALL 显示具体的错误提示信息
3. WHEN 表单验证通过并提交成功 THEN 系统 SHALL 将设施状态设置为待审核
4. WHEN 提交成功 THEN 系统 SHALL 显示成功提示并清空表单

### Requirement 5

**User Story:** 作为数据采集员，我希望能够查看自己提交的采集记录，以便跟踪审核状态。

#### Acceptance Criteria

1. WHEN 用户访问采集记录页面 THEN 系统 SHALL 显示当前用户提交的所有设施记录
2. WHEN 显示采集记录 THEN 系统 SHALL 包含设施名称、类型、提交时间、审核状态等信息
3. WHEN 记录被驳回 THEN 系统 SHALL 显示驳回原因
