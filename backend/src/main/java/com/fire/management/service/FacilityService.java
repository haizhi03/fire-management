package com.fire.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fire.management.dto.FacilityCreateRequest;
import com.fire.management.entity.FireFacility;
import com.fire.management.entity.FireFacilityPhoto;
import com.fire.management.entity.SysUser;
import com.fire.management.mapper.FireFacilityMapper;
import com.fire.management.mapper.FireFacilityPhotoMapper;
import com.fire.management.mapper.SysUserMapper;
import com.fire.management.utils.GeoUtil;
import com.fire.management.vo.FacilityDetailVO;
import com.fire.management.vo.FacilityNearbyVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设施服务
 */
@Service
public class FacilityService {

    @Autowired
    private FireFacilityMapper facilityMapper;

    @Autowired
    private FireFacilityPhotoMapper photoMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private FileService fileService;

    /**
     * 分页查询设施列表
     */
    public Page<FireFacility> getFacilityList(int pageNum, int pageSize, 
                                               Integer facilityType, 
                                               Integer status,
                                               Integer auditStatus,
                                               String keyword) {
        Page<FireFacility> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FireFacility> wrapper = new LambdaQueryWrapper<>();
        
        // 筛选条件
        if (facilityType != null) {
            wrapper.eq(FireFacility::getFacilityType, facilityType);
        }
        if (status != null) {
            wrapper.eq(FireFacility::getStatus, status);
        }
        if (auditStatus != null) {
            wrapper.eq(FireFacility::getAuditStatus, auditStatus);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(FireFacility::getFacilityName, keyword)
                   .or()
                   .like(FireFacility::getAddress, keyword);
        }
        
        // 排除已合并的数据
        wrapper.eq(FireFacility::getIsMerged, 0);
        
        // 按创建时间倒序
        wrapper.orderByDesc(FireFacility::getCreateTime);
        
        return facilityMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID查询设施详情
     */
    public FacilityDetailVO getFacilityDetail(Long facilityId) {
        FireFacility facility = facilityMapper.selectById(facilityId);
        if (facility == null) {
            throw new RuntimeException("设施不存在");
        }

        FacilityDetailVO vo = new FacilityDetailVO();
        BeanUtils.copyProperties(facility, vo);

        // 查询采集人员信息
        SysUser collector = userMapper.selectById(facility.getCollectorId());
        if (collector != null) {
            vo.setCollectorName(collector.getRealName());
        }

        // 查询照片列表
        LambdaQueryWrapper<FireFacilityPhoto> photoWrapper = new LambdaQueryWrapper<>();
        photoWrapper.eq(FireFacilityPhoto::getFacilityId, facilityId);
        List<FireFacilityPhoto> photos = photoMapper.selectList(photoWrapper);
        vo.setPhotos(photos);

        return vo;
    }

    /**
     * 创建设施
     */
    @Transactional(rollbackFor = Exception.class)
    public FireFacility createFacility(FacilityCreateRequest request, Long collectorId) {
        // 验证必填字段
        if (request.getFacilityType() == 1 && request.getPressure() == null) {
            throw new RuntimeException("消防栓必须填写工作压力");
        }

        // 创建设施
        FireFacility facility = new FireFacility();
        BeanUtils.copyProperties(request, facility);
        facility.setCollectorId(collectorId);
        facility.setAuditStatus(0); // 默认待审核
        facility.setIsMerged(0);

        facilityMapper.insert(facility);

        // 保存照片
        if (request.getPhotoUrls() != null && !request.getPhotoUrls().isEmpty()) {
            for (int i = 0; i < request.getPhotoUrls().size(); i++) {
                FireFacilityPhoto photo = new FireFacilityPhoto();
                photo.setFacilityId(facility.getFacilityId());
                photo.setPhotoUrl(request.getPhotoUrls().get(i));
                if (request.getPhotoDescs() != null && i < request.getPhotoDescs().size()) {
                    photo.setPhotoDesc(request.getPhotoDescs().get(i));
                }
                photoMapper.insert(photo);
            }
        }

        return facility;
    }

    /**
     * 更新设施
     */
    @Transactional(rollbackFor = Exception.class)
    public FireFacility updateFacility(Long facilityId, FacilityCreateRequest request) {
        FireFacility facility = facilityMapper.selectById(facilityId);
        if (facility == null) {
            throw new RuntimeException("设施不存在");
        }

        // 更新设施信息
        BeanUtils.copyProperties(request, facility);
        facility.setFacilityId(facilityId);
        facilityMapper.updateById(facility);

        // 删除旧照片
        LambdaQueryWrapper<FireFacilityPhoto> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireFacilityPhoto::getFacilityId, facilityId);
        photoMapper.delete(wrapper);

        // 保存新照片
        if (request.getPhotoUrls() != null && !request.getPhotoUrls().isEmpty()) {
            for (int i = 0; i < request.getPhotoUrls().size(); i++) {
                FireFacilityPhoto photo = new FireFacilityPhoto();
                photo.setFacilityId(facilityId);
                photo.setPhotoUrl(request.getPhotoUrls().get(i));
                if (request.getPhotoDescs() != null && i < request.getPhotoDescs().size()) {
                    photo.setPhotoDesc(request.getPhotoDescs().get(i));
                }
                photoMapper.insert(photo);
            }
        }

        return facility;
    }

    /**
     * 删除设施（软删除）
     * 同时删除关联的照片记录和文件
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFacility(Long facilityId) {
        FireFacility facility = facilityMapper.selectById(facilityId);
        if (facility == null) {
            throw new RuntimeException("设施不存在");
        }

        // 查询所有关联的照片
        LambdaQueryWrapper<FireFacilityPhoto> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireFacilityPhoto::getFacilityId, facilityId);
        List<FireFacilityPhoto> photos = photoMapper.selectList(wrapper);

        // 删除文件系统中的照片文件
        for (FireFacilityPhoto photo : photos) {
            try {
                fileService.deleteFile(photo.getPhotoUrl());
            } catch (Exception e) {
                // 记录日志但不影响删除流程
                System.err.println("删除照片文件失败: " + photo.getPhotoUrl() + ", 错误: " + e.getMessage());
            }
        }

        // 删除照片记录
        photoMapper.delete(wrapper);

        // 软删除设施
        facilityMapper.deleteById(facilityId);
    }

    /**
     * 查询周边设施
     */
    public List<FacilityNearbyVO> getNearbyFacilities(BigDecimal latitude, 
                                                       BigDecimal longitude, 
                                                       Integer radius,
                                                       Integer facilityType) {
        // 默认半径500米
        if (radius == null || radius <= 0) {
            radius = 500;
        }

        // 计算边界框以优化查询
        double[] bbox = GeoUtil.getBoundingBox(
            latitude.doubleValue(), 
            longitude.doubleValue(), 
            radius
        );

        // 查询边界框内的设施
        LambdaQueryWrapper<FireFacility> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(FireFacility::getLatitude, 
                       BigDecimal.valueOf(bbox[0]), 
                       BigDecimal.valueOf(bbox[1]))
               .between(FireFacility::getLongitude, 
                       BigDecimal.valueOf(bbox[2]), 
                       BigDecimal.valueOf(bbox[3]))
               .eq(FireFacility::getIsMerged, 0)
               .eq(FireFacility::getAuditStatus, 1); // 只查询已审核通过的

        if (facilityType != null) {
            wrapper.eq(FireFacility::getFacilityType, facilityType);
        }

        List<FireFacility> facilities = facilityMapper.selectList(wrapper);

        // 计算精确距离并过滤
        List<FacilityNearbyVO> result = new ArrayList<>();
        for (FireFacility facility : facilities) {
            double distance = GeoUtil.calculateDistance(
                latitude, longitude,
                facility.getLatitude(), facility.getLongitude()
            );

            // 只返回在半径范围内的设施
            if (distance <= radius) {
                FacilityNearbyVO vo = new FacilityNearbyVO();
                BeanUtils.copyProperties(facility, vo);
                vo.setDistance(distance);

                // 查询主照片
                LambdaQueryWrapper<FireFacilityPhoto> photoWrapper = new LambdaQueryWrapper<>();
                photoWrapper.eq(FireFacilityPhoto::getFacilityId, facility.getFacilityId())
                           .last("LIMIT 1");
                FireFacilityPhoto photo = photoMapper.selectOne(photoWrapper);
                if (photo != null) {
                    vo.setMainPhotoUrl(photo.getPhotoUrl());
                }

                result.add(vo);
            }
        }

        // 按距离排序
        result.sort(Comparator.comparingDouble(FacilityNearbyVO::getDistance));

        return result;
    }

    /**
     * 增强周边查询（支持距离圈、单位转换）
     */
    public List<FacilityNearbyVO> getEnhancedNearbyFacilities(BigDecimal latitude, 
                                                               BigDecimal longitude, 
                                                               Integer radius,
                                                               String unit,
                                                               Integer facilityType,
                                                               Integer status) {
        // 处理单位转换
        int radiusInMeters = convertToMeters(radius, unit);
        
        // 默认半径500米
        if (radiusInMeters <= 0) {
            radiusInMeters = 500;
        }

        // 计算边界框以优化查询
        double[] bbox = GeoUtil.getBoundingBox(
            latitude.doubleValue(), 
            longitude.doubleValue(), 
            radiusInMeters
        );

        // 查询边界框内的设施
        LambdaQueryWrapper<FireFacility> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(FireFacility::getLatitude, 
                       BigDecimal.valueOf(bbox[0]), 
                       BigDecimal.valueOf(bbox[1]))
               .between(FireFacility::getLongitude, 
                       BigDecimal.valueOf(bbox[2]), 
                       BigDecimal.valueOf(bbox[3]))
               .eq(FireFacility::getIsMerged, 0)
               .eq(FireFacility::getAuditStatus, 1); // 只查询已审核通过的

        if (facilityType != null) {
            wrapper.eq(FireFacility::getFacilityType, facilityType);
        }
        
        if (status != null) {
            wrapper.eq(FireFacility::getStatus, status);
        }

        List<FireFacility> facilities = facilityMapper.selectList(wrapper);

        // 计算精确距离并过滤
        List<FacilityNearbyVO> result = new ArrayList<>();
        for (FireFacility facility : facilities) {
            double distance = GeoUtil.calculateDistance(
                latitude, longitude,
                facility.getLatitude(), facility.getLongitude()
            );

            // 只返回在半径范围内的设施
            if (distance <= radiusInMeters) {
                FacilityNearbyVO vo = new FacilityNearbyVO();
                BeanUtils.copyProperties(facility, vo);
                vo.setDistance(distance);

                // 查询主照片
                LambdaQueryWrapper<FireFacilityPhoto> photoWrapper = new LambdaQueryWrapper<>();
                photoWrapper.eq(FireFacilityPhoto::getFacilityId, facility.getFacilityId())
                           .last("LIMIT 1");
                FireFacilityPhoto photo = photoMapper.selectOne(photoWrapper);
                if (photo != null) {
                    vo.setMainPhotoUrl(photo.getPhotoUrl());
                }

                result.add(vo);
            }
        }

        // 按距离排序
        result.sort(Comparator.comparingDouble(FacilityNearbyVO::getDistance));

        return result;
    }

    /**
     * 查询最近的N个设施
     */
    public List<FacilityNearbyVO> getClosestFacilities(BigDecimal latitude, 
                                                        BigDecimal longitude, 
                                                        Integer limit,
                                                        Integer facilityType,
                                                        Integer status) {
        // 使用较大的搜索半径确保能找到足够的设施
        int searchRadius = 5000; // 5公里

        // 计算边界框
        double[] bbox = GeoUtil.getBoundingBox(
            latitude.doubleValue(), 
            longitude.doubleValue(), 
            searchRadius
        );

        // 查询边界框内的设施
        LambdaQueryWrapper<FireFacility> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(FireFacility::getLatitude, 
                       BigDecimal.valueOf(bbox[0]), 
                       BigDecimal.valueOf(bbox[1]))
               .between(FireFacility::getLongitude, 
                       BigDecimal.valueOf(bbox[2]), 
                       BigDecimal.valueOf(bbox[3]))
               .eq(FireFacility::getIsMerged, 0)
               .eq(FireFacility::getAuditStatus, 1);

        if (facilityType != null) {
            wrapper.eq(FireFacility::getFacilityType, facilityType);
        }
        
        if (status != null) {
            wrapper.eq(FireFacility::getStatus, status);
        }

        List<FireFacility> facilities = facilityMapper.selectList(wrapper);

        // 计算距离并排序
        List<FacilityNearbyVO> result = new ArrayList<>();
        for (FireFacility facility : facilities) {
            double distance = GeoUtil.calculateDistance(
                latitude, longitude,
                facility.getLatitude(), facility.getLongitude()
            );

            FacilityNearbyVO vo = new FacilityNearbyVO();
            BeanUtils.copyProperties(facility, vo);
            vo.setDistance(distance);

            // 查询主照片
            LambdaQueryWrapper<FireFacilityPhoto> photoWrapper = new LambdaQueryWrapper<>();
            photoWrapper.eq(FireFacilityPhoto::getFacilityId, facility.getFacilityId())
                       .last("LIMIT 1");
            FireFacilityPhoto photo = photoMapper.selectOne(photoWrapper);
            if (photo != null) {
                vo.setMainPhotoUrl(photo.getPhotoUrl());
            }

            result.add(vo);
        }

        // 按距离排序并限制数量
        result.sort(Comparator.comparingDouble(FacilityNearbyVO::getDistance));
        
        if (result.size() > limit) {
            result = result.subList(0, limit);
        }

        return result;
    }

    /**
     * 按距离范围筛选设施
     */
    public List<FacilityNearbyVO> filterFacilitiesByDistance(BigDecimal latitude, 
                                                              BigDecimal longitude, 
                                                              Integer minDistance,
                                                              Integer maxDistance,
                                                              String unit,
                                                              Integer facilityType,
                                                              Integer status) {
        // 处理单位转换
        int minDistanceInMeters = convertToMeters(minDistance, unit);
        int maxDistanceInMeters = convertToMeters(maxDistance, unit);

        // 使用最大距离计算边界框
        double[] bbox = GeoUtil.getBoundingBox(
            latitude.doubleValue(), 
            longitude.doubleValue(), 
            maxDistanceInMeters
        );

        // 查询边界框内的设施
        LambdaQueryWrapper<FireFacility> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(FireFacility::getLatitude, 
                       BigDecimal.valueOf(bbox[0]), 
                       BigDecimal.valueOf(bbox[1]))
               .between(FireFacility::getLongitude, 
                       BigDecimal.valueOf(bbox[2]), 
                       BigDecimal.valueOf(bbox[3]))
               .eq(FireFacility::getIsMerged, 0)
               .eq(FireFacility::getAuditStatus, 1);

        if (facilityType != null) {
            wrapper.eq(FireFacility::getFacilityType, facilityType);
        }
        
        if (status != null) {
            wrapper.eq(FireFacility::getStatus, status);
        }

        List<FireFacility> facilities = facilityMapper.selectList(wrapper);

        // 计算精确距离并按范围过滤
        List<FacilityNearbyVO> result = new ArrayList<>();
        for (FireFacility facility : facilities) {
            double distance = GeoUtil.calculateDistance(
                latitude, longitude,
                facility.getLatitude(), facility.getLongitude()
            );

            // 只返回在距离范围内的设施
            if (distance >= minDistanceInMeters && distance <= maxDistanceInMeters) {
                FacilityNearbyVO vo = new FacilityNearbyVO();
                BeanUtils.copyProperties(facility, vo);
                vo.setDistance(distance);

                // 查询主照片
                LambdaQueryWrapper<FireFacilityPhoto> photoWrapper = new LambdaQueryWrapper<>();
                photoWrapper.eq(FireFacilityPhoto::getFacilityId, facility.getFacilityId())
                           .last("LIMIT 1");
                FireFacilityPhoto photo = photoMapper.selectOne(photoWrapper);
                if (photo != null) {
                    vo.setMainPhotoUrl(photo.getPhotoUrl());
                }

                result.add(vo);
            }
        }

        // 按距离排序
        result.sort(Comparator.comparingDouble(FacilityNearbyVO::getDistance));

        return result;
    }

    /**
     * 紧急模式查询（优先显示状态正常且距离最近的设施）
     */
    public List<FacilityNearbyVO> getEmergencyFacilities(BigDecimal latitude, 
                                                          BigDecimal longitude, 
                                                          Integer radius,
                                                          Integer facilityType) {
        // 默认半径1000米
        if (radius == null || radius <= 0) {
            radius = 1000;
        }

        // 计算边界框
        double[] bbox = GeoUtil.getBoundingBox(
            latitude.doubleValue(), 
            longitude.doubleValue(), 
            radius
        );

        // 查询边界框内的设施，只查询状态正常的
        LambdaQueryWrapper<FireFacility> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(FireFacility::getLatitude, 
                       BigDecimal.valueOf(bbox[0]), 
                       BigDecimal.valueOf(bbox[1]))
               .between(FireFacility::getLongitude, 
                       BigDecimal.valueOf(bbox[2]), 
                       BigDecimal.valueOf(bbox[3]))
               .eq(FireFacility::getIsMerged, 0)
               .eq(FireFacility::getAuditStatus, 1)
               .eq(FireFacility::getStatus, 1); // 只查询状态正常的设施

        if (facilityType != null) {
            wrapper.eq(FireFacility::getFacilityType, facilityType);
        }

        List<FireFacility> facilities = facilityMapper.selectList(wrapper);

        // 计算精确距离并过滤
        List<FacilityNearbyVO> result = new ArrayList<>();
        for (FireFacility facility : facilities) {
            double distance = GeoUtil.calculateDistance(
                latitude, longitude,
                facility.getLatitude(), facility.getLongitude()
            );

            // 只返回在半径范围内的设施
            if (distance <= radius) {
                FacilityNearbyVO vo = new FacilityNearbyVO();
                BeanUtils.copyProperties(facility, vo);
                vo.setDistance(distance);

                // 查询主照片
                LambdaQueryWrapper<FireFacilityPhoto> photoWrapper = new LambdaQueryWrapper<>();
                photoWrapper.eq(FireFacilityPhoto::getFacilityId, facility.getFacilityId())
                           .last("LIMIT 1");
                FireFacilityPhoto photo = photoMapper.selectOne(photoWrapper);
                if (photo != null) {
                    vo.setMainPhotoUrl(photo.getPhotoUrl());
                }

                result.add(vo);
            }
        }

        // 按距离排序
        result.sort(Comparator.comparingDouble(FacilityNearbyVO::getDistance));

        return result;
    }

    /**
     * 距离单位转换为米
     */
    private int convertToMeters(Integer distance, String unit) {
        if (distance == null) {
            return 0;
        }
        
        if ("km".equalsIgnoreCase(unit)) {
            return distance * 1000;
        }
        
        // 默认为米
        return distance;
    }
}
