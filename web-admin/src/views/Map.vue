<template>
  <div class="map-container">
    <!-- 搜索面板 -->
    <el-card class="search-panel">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="设施类型">
          <el-select v-model="searchForm.facilityType" placeholder="全部" clearable @change="loadMarkers">
            <el-option label="消防栓" :value="1" />
            <el-option label="消防车" :value="2" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="查询半径">
          <el-select v-model="searchForm.radius" placeholder="选择半径">
            <el-option label="500米" :value="500" />
            <el-option label="1公里" :value="1000" />
            <el-option label="2公里" :value="2000" />
            <el-option label="5公里" :value="5000" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="loadNearbyFacilities" :disabled="!currentLocation">
            <el-icon><Search /></el-icon>
            查询周边
          </el-button>
          <el-button @click="loadMarkers">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="stats">
        <el-tag type="success">正常: {{ stats.normal }}</el-tag>
        <el-tag type="danger">损坏: {{ stats.damaged }}</el-tag>
        <el-tag type="warning">维修中: {{ stats.repairing }}</el-tag>
        <el-tag type="info">总计: {{ stats.total }}</el-tag>
      </div>
    </el-card>

    <!-- 地图容器 -->
    <el-card class="map-card" v-loading="mapLoading" element-loading-text="地图加载中...">
      <div id="amap-container" class="amap-container"></div>
    </el-card>

    <!-- 设施详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="设施详情"
      width="600px"
    >
      <el-descriptions :column="2" border v-if="currentFacility">
        <el-descriptions-item label="设施名称">
          {{ currentFacility.facilityName }}
        </el-descriptions-item>
        <el-descriptions-item label="设施类型">
          {{ currentFacility.facilityType === 1 ? '消防栓' : '消防车' }}
        </el-descriptions-item>
        <el-descriptions-item label="经度">
          {{ currentFacility.longitude }}
        </el-descriptions-item>
        <el-descriptions-item label="纬度">
          {{ currentFacility.latitude }}
        </el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">
          {{ currentFacility.address }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentFacility.status === 1" type="success">正常</el-tag>
          <el-tag v-else-if="currentFacility.status === 2" type="danger">损坏</el-tag>
          <el-tag v-else type="warning">维修中</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="工作压力">
          {{ currentFacility.pressure }} MPa
        </el-descriptions-item>
        <el-descriptions-item label="距离" v-if="currentFacility.distance">
          {{ (currentFacility.distance / 1000).toFixed(2) }} 公里
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="navigateToFacility">导航到此</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const map = ref(null)
const markers = ref([])
const currentLocation = ref(null)
const detailVisible = ref(false)
const currentFacility = ref(null)
const mapLoading = ref(true)

const searchForm = reactive({
  facilityType: null,
  radius: 1000
})

const stats = reactive({
  normal: 0,
  damaged: 0,
  repairing: 0,
  total: 0
})

// 初始化地图
const initMap = () => {
  // 等待AMap加载
  const checkAMap = setInterval(() => {
    if (window.AMap) {
      clearInterval(checkAMap)
      
      try {
        // 创建地图实例
        map.value = new window.AMap.Map('amap-container', {
          zoom: 13,
          center: [116.397428, 39.90923], // 默认北京
          viewMode: '2D',
          mapStyle: 'amap://styles/normal'
        })

        mapLoading.value = false
        ElMessage.success('地图加载成功')

        // 尝试获取当前位置
        window.AMap.plugin('AMap.Geolocation', () => {
          const geolocation = new window.AMap.Geolocation({
            enableHighAccuracy: true,
            timeout: 10000,
            zoomToAccuracy: true
          })

          geolocation.getCurrentPosition((status, result) => {
            if (status === 'complete') {
              currentLocation.value = {
                longitude: result.position.lng,
                latitude: result.position.lat
              }
              map.value.setCenter([result.position.lng, result.position.lat])
              
              // 添加当前位置标记
              new window.AMap.Marker({
                position: [result.position.lng, result.position.lat],
                icon: 'https://a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-red.png',
                title: '当前位置',
                map: map.value
              })

              ElMessage.success('已定位到当前位置')
            } else {
              ElMessage.warning('无法获取当前位置，使用默认位置')
              // 使用北京作为默认位置
              currentLocation.value = {
                longitude: 116.397428,
                latitude: 39.90923
              }
            }
            
            // 加载设施标记
            loadMarkers()
          })
        })
      } catch (error) {
        mapLoading.value = false
        console.error('地图初始化失败:', error)
        ElMessage.error('地图初始化失败: ' + error.message)
      }
    }
  }, 100)

  // 10秒超时
  setTimeout(() => {
    if (!map.value) {
      clearInterval(checkAMap)
      mapLoading.value = false
      ElMessage.error('地图加载超时，请刷新页面重试')
    }
  }, 10000)
}

// 加载所有设施标记
const loadMarkers = async () => {
  try {
    const res = await request.get('/facilities', {
      params: {
        pageNum: 1,
        pageSize: 1000,
        facilityType: searchForm.facilityType,
        auditStatus: 1 // 只显示已审核通过的
      }
    })

    // 清除旧标记
    markers.value.forEach(marker => marker.setMap(null))
    markers.value = []

    // 统计数据
    stats.normal = 0
    stats.damaged = 0
    stats.repairing = 0
    stats.total = res.data.records.length

    // 添加新标记
    res.data.records.forEach(facility => {
      // 统计
      if (facility.status === 1) stats.normal++
      else if (facility.status === 2) stats.damaged++
      else if (facility.status === 3) stats.repairing++

      // 根据状态选择图标颜色
      let iconColor = '#52c41a' // 绿色-正常
      if (facility.status === 2) iconColor = '#ff4d4f' // 红色-损坏
      else if (facility.status === 3) iconColor = '#faad14' // 黄色-维修中

      const marker = new window.AMap.Marker({
        position: [facility.longitude, facility.latitude],
        title: facility.facilityName,
        extData: facility,
        map: map.value
      })

      // 自定义标记样式
      const content = `
        <div style="
          background-color: ${iconColor};
          width: 24px;
          height: 24px;
          border-radius: 50%;
          border: 2px solid white;
          box-shadow: 0 2px 6px rgba(0,0,0,0.3);
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          font-size: 12px;
          font-weight: bold;
        ">
          ${facility.facilityType === 1 ? '栓' : '车'}
        </div>
      `
      marker.setContent(content)

      // 点击标记显示详情
      marker.on('click', () => {
        showFacilityDetail(facility)
      })

      markers.value.push(marker)
    })

    if (stats.total > 0) {
      ElMessage.success(`已加载 ${stats.total} 个设施`)
    } else {
      ElMessage.info('暂无设施数据')
    }
  } catch (error) {
    console.error('加载设施失败:', error)
    ElMessage.error('加载设施失败')
  }
}

// 查询周边设施
const loadNearbyFacilities = async () => {
  if (!currentLocation.value) {
    ElMessage.warning('请先获取当前位置')
    return
  }

  try {
    const res = await request.get('/facilities/nearby', {
      params: {
        latitude: currentLocation.value.latitude,
        longitude: currentLocation.value.longitude,
        radius: searchForm.radius,
        facilityType: searchForm.facilityType
      }
    })

    // 清除旧标记
    markers.value.forEach(marker => marker.setMap(null))
    markers.value = []

    // 统计数据
    stats.normal = 0
    stats.damaged = 0
    stats.repairing = 0
    stats.total = res.data.length

    // 添加新标记
    res.data.forEach(facility => {
      // 统计
      if (facility.status === 1) stats.normal++
      else if (facility.status === 2) stats.damaged++
      else if (facility.status === 3) stats.repairing++

      // 根据状态选择图标颜色
      let iconColor = '#52c41a'
      if (facility.status === 2) iconColor = '#ff4d4f'
      else if (facility.status === 3) iconColor = '#faad14'

      const marker = new window.AMap.Marker({
        position: [facility.longitude, facility.latitude],
        title: facility.facilityName,
        extData: facility,
        map: map.value
      })

      const content = `
        <div style="
          background-color: ${iconColor};
          width: 24px;
          height: 24px;
          border-radius: 50%;
          border: 2px solid white;
          box-shadow: 0 2px 6px rgba(0,0,0,0.3);
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          font-size: 12px;
          font-weight: bold;
        ">
          ${facility.facilityType === 1 ? '栓' : '车'}
        </div>
      `
      marker.setContent(content)

      marker.on('click', () => {
        showFacilityDetail(facility)
      })

      markers.value.push(marker)
    })

    if (stats.total > 0) {
      ElMessage.success(`找到 ${stats.total} 个周边设施`)
    } else {
      ElMessage.info('周边暂无设施')
    }
  } catch (error) {
    console.error('查询周边设施失败:', error)
    ElMessage.error('查询周边设施失败')
  }
}

// 显示设施详情
const showFacilityDetail = async (facility) => {
  try {
    const res = await request.get(`/facilities/${facility.facilityId}`)
    currentFacility.value = res.data
    detailVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 导航到设施
const navigateToFacility = () => {
  if (currentFacility.value) {
    const facilityName = encodeURIComponent(currentFacility.value.facilityName || '目的地')
    const url = `https://uri.amap.com/navigation?to=${currentFacility.value.longitude},${currentFacility.value.latitude},${facilityName}&mode=walking&src=fire-management`
    window.open(url, '_blank')
  }
}

// 重置视图
const resetView = () => {
  if (currentLocation.value) {
    map.value.setCenter([currentLocation.value.longitude, currentLocation.value.latitude])
    map.value.setZoom(13)
  }
  searchForm.facilityType = null
  searchForm.radius = 1000
  loadMarkers()
}

onMounted(() => {
  initMap()
})

onBeforeUnmount(() => {
  if (map.value) {
    map.value.destroy()
  }
})
</script>

<style scoped>
.map-container {
  height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.map-container {
  position: relative;
  z-index: 1;
}

.search-panel {
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: none;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.stats {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}

.map-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 500px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: none;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.map-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  padding: 0;
}

.amap-container {
  width: 100%;
  height: 100%;
  min-height: 500px;
}
</style>
