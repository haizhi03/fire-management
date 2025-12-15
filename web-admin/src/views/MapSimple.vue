<template>
  <div class="map-simple-container">
    <el-card class="search-panel">
      <h3>智能距离查询地图</h3>
      
      <!-- 距离查询控制面板 -->
      <el-tabs v-model="activeTab" class="query-tabs">
        <el-tab-pane label="基础查询" name="basic">
          <el-form :inline="true" :model="searchForm">
            <el-form-item label="设施类型">
              <el-select v-model="searchForm.facilityType" placeholder="全部" clearable>
                <el-option label="消防栓" :value="1" />
                <el-option label="消防车" :value="2" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="设施状态">
              <el-select v-model="searchForm.status" placeholder="全部" clearable>
                <el-option label="正常" :value="1" />
                <el-option label="损坏" :value="2" />
                <el-option label="维修中" :value="3" />
              </el-select>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="loadFacilities">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button @click="resetSearch">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="距离查询" name="distance">
          <el-form :inline="true" :model="distanceForm">
            <el-form-item label="当前位置">
              <el-input-number v-model="distanceForm.latitude" placeholder="纬度" :precision="6" style="width: 120px" readonly />
              <el-input-number v-model="distanceForm.longitude" placeholder="经度" :precision="6" style="width: 120px; margin-left: 8px" readonly />
              <el-button size="small" @click="getCurrentLocation" style="margin-left: 8px">
                <el-icon><Location /></el-icon>
                重新定位
              </el-button>
            </el-form-item>
            
            <el-form-item label="查询半径">
              <el-input-number v-model="distanceForm.radius" :min="100" :max="10000" :step="100" style="width: 120px" />
              <el-select v-model="distanceForm.unit" style="width: 80px; margin-left: 8px">
                <el-option label="米" value="m" />
                <el-option label="公里" value="km" />
              </el-select>
            </el-form-item>

            <el-form-item label="距离范围">
              <el-select v-model="distanceForm.quickRange" placeholder="快速选择" @change="setQuickRange">
                <el-option label="100米内" value="100m" />
                <el-option label="500米内" value="500m" />
                <el-option label="1公里内" value="1km" />
                <el-option label="2公里内" value="2km" />
                <el-option label="自定义" value="custom" />
              </el-select>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="searchByDistance">
                <el-icon><Compass /></el-icon>
                距离查询
              </el-button>
              <el-button @click="findClosest">
                <el-icon><Aim /></el-icon>
                最近设施
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 紧急模式开关 -->
          <div class="emergency-mode">
            <el-switch
              v-model="emergencyMode"
              active-text="紧急模式"
              inactive-text="普通模式"
              active-color="#ff4757"
              @change="onEmergencyModeChange"
            />
            <el-text type="info" size="small" style="margin-left: 12px">
              紧急模式将优先显示状态正常的设施
            </el-text>
          </div>
        </el-tab-pane>

        <el-tab-pane label="导航规划" name="navigation">
          <el-form :inline="true" :model="navigationForm">
            <el-form-item label="目标设施">
              <el-select v-model="navigationForm.targetFacility" placeholder="选择目标设施" filterable>
                <el-option
                  v-for="facility in facilities"
                  :key="facility.facilityId"
                  :label="facility.facilityName"
                  :value="facility.facilityId"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="出行方式">
              <el-radio-group v-model="navigationForm.mode">
                <el-radio-button label="walking">步行</el-radio-button>
                <el-radio-button label="driving">驾车</el-radio-button>
                <el-radio-button label="cycling">骑行</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item>
              <el-button type="success" @click="planRoute">
                <el-icon><Guide /></el-icon>
                路径规划
              </el-button>
              <el-button type="warning" @click="startNavigation" :disabled="!routeInfo">
                <el-icon><Position /></el-icon>
                开始导航
              </el-button>
              <el-button type="primary" @click="openAmapNavigation" :disabled="!navigationForm.targetFacility">
                <el-icon><Location /></el-icon>
                高德导航
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 路径信息显示 -->
          <div v-if="routeInfo" class="route-info">
            <el-alert type="success" :closable="false">
              <template #title>
                路径规划结果：总距离 {{ formatDistance(routeInfo.totalDistance) }}，
                预计时间 {{ formatTime(routeInfo.estimatedTime) }}
              </template>
            </el-alert>
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <div class="stats">
        <el-statistic title="正常" :value="stats.normal" />
        <el-statistic title="损坏" :value="stats.damaged" />
        <el-statistic title="维修中" :value="stats.repairing" />
        <el-statistic title="总计" :value="stats.total" />
      </div>
    </el-card>

    <!-- 地图和列表切换 -->
    <el-card class="map-container">
      <template #header>
        <div class="map-header">
          <span>地图视图</span>
          <div class="view-controls">
            <el-radio-group v-model="viewMode" size="small">
              <el-radio-button label="map">地图模式</el-radio-button>
              <el-radio-button label="list">列表模式</el-radio-button>
              <el-radio-button label="split">分屏模式</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <div class="map-content" :class="viewMode">
        <!-- 地图区域 -->
        <div v-show="viewMode === 'map' || viewMode === 'split'" class="map-area">
          <div id="mapContainer" class="map-container-inner">
            <!-- 简化版地图显示 -->
            <div v-if="!mapLoaded" class="map-placeholder">
              <el-icon size="48"><Location /></el-icon>
              <p>地图加载中...</p>
              <el-button @click="initSimpleMap">加载地图</el-button>
            </div>
            
            <!-- 简单的设施分布图 -->
            <div v-else class="simple-map">
              <div class="map-grid">
                <div 
                  v-for="facility in facilities" 
                  :key="facility.facilityId"
                  class="facility-dot"
                  :class="{ 
                    'normal': facility.status === 1,
                    'damaged': facility.status === 2,
                    'repairing': facility.status === 3,
                    'selected': selectedFacility?.facilityId === facility.facilityId
                  }"
                  :style="getFacilityPosition(facility)"
                  @click="selectFacility(facility)"
                  :title="facility.facilityName"
                >
                  <span>{{ facility.facilityType === 1 ? '栓' : '车' }}</span>
                  <div v-if="facility.distance !== undefined" class="distance-label">
                    {{ formatDistance(facility.distance) }}
                  </div>
                </div>
                
                <!-- 当前位置 -->
                <div 
                  v-if="distanceForm.latitude && distanceForm.longitude"
                  class="current-position"
                  :style="getCurrentPosition()"
                  title="当前位置"
                >
                  <el-icon><Aim /></el-icon>
                </div>
                
                <!-- 距离圈 -->
                <div 
                  v-if="showDistanceCircle && distanceForm.latitude && distanceForm.longitude"
                  class="distance-circle"
                  :style="getDistanceCircleStyle()"
                ></div>
              </div>
            </div>
          </div>
          
          <!-- 地图控制面板 -->
          <div class="map-controls">
            <el-button-group size="small">
              <el-button @click="centerToCurrentLocation">
                <el-icon><Aim /></el-icon>
                定位
              </el-button>
              <el-button @click="showAllFacilities">
                <el-icon><View /></el-icon>
                显示全部
              </el-button>
              <el-button @click="toggleDistanceCircle">
                <el-icon><Compass /></el-icon>
                {{ showDistanceCircle ? '隐藏' : '显示' }}距离圈
              </el-button>
            </el-button-group>
          </div>

          <!-- 地图图例 -->
          <div class="map-legend">
            <div class="legend-item">
              <span class="legend-icon normal"></span>
              <span>正常设施</span>
            </div>
            <div class="legend-item">
              <span class="legend-icon damaged"></span>
              <span>损坏设施</span>
            </div>
            <div class="legend-item">
              <span class="legend-icon repairing"></span>
              <span>维修设施</span>
            </div>
            <div class="legend-item">
              <span class="legend-icon current"></span>
              <span>当前位置</span>
            </div>
          </div>
        </div>

        <!-- 列表区域 -->
        <div v-show="viewMode === 'list' || viewMode === 'split'" class="list-area" v-loading="loading">
          <div class="list-header">
            <span>设施列表</span>
            <el-tag>共 {{ facilities.length }} 条</el-tag>
          </div>
          
          <el-empty v-if="facilities.length === 0 && !loading" description="暂无数据" />
          
          <div class="facility-grid">
            <el-card
              v-for="facility in facilities"
              :key="facility.facilityId"
              class="facility-item"
              :class="{ 'selected': selectedFacility?.facilityId === facility.facilityId }"
              shadow="hover"
              @click="selectFacility(facility)"
            >
              <div class="facility-header">
                <h4>{{ facility.facilityName }}</h4>
                <el-tag v-if="facility.status === 1" type="success" size="small">正常</el-tag>
                <el-tag v-else-if="facility.status === 2" type="danger" size="small">损坏</el-tag>
                <el-tag v-else type="warning" size="small">维修中</el-tag>
              </div>
              
              <div class="facility-info">
                <p>
                  <el-icon><Location /></el-icon>
                  {{ facility.address }}
                </p>
                <p>
                  <el-icon><Position /></el-icon>
                  经度: {{ facility.longitude }}, 纬度: {{ facility.latitude }}
                </p>
                <p v-if="facility.pressure">
                  <el-icon><DataLine /></el-icon>
                  压力: {{ facility.pressure }} MPa
                </p>
                <p v-if="facility.distance !== undefined" class="distance-info">
                  <el-icon><Compass /></el-icon>
                  距离: {{ formatDistance(facility.distance) }}
                  <el-tag v-if="facility.distance <= 100" type="success" size="small">很近</el-tag>
                  <el-tag v-else-if="facility.distance <= 500" type="warning" size="small">较近</el-tag>
                  <el-tag v-else type="info" size="small">较远</el-tag>
                </p>
              </div>
              
              <div class="facility-actions">
                <el-button size="small" type="primary" @click.stop="viewDetail(facility)">
                  查看详情
                </el-button>
                <el-button size="small" @click.stop="centerMapToFacility(facility)">
                  地图定位
                </el-button>
                <el-button v-if="facility.distance !== undefined" size="small" type="success" @click.stop="navigateToFacility(facility)">
                  <el-icon><Guide /></el-icon>
                  导航
                </el-button>
              </div>
            </el-card>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 详情对话框 -->
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
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Location, 
  Compass, 
  Aim, 
  Guide, 
  Position, 
  View, 
  DataLine 
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const facilities = ref([])
const detailVisible = ref(false)
const currentFacility = ref(null)
const activeTab = ref('basic')
const emergencyMode = ref(false)
const routeInfo = ref(null)
const viewMode = ref('split')
const selectedFacility = ref(null)
const showDistanceCircle = ref(true)
const mapLoaded = ref(false)

// 地图相关变量
let map = null
let currentLocationMarker = null
let facilityMarkers = []
let distanceCircle = null
let routeLine = null

// 地图边界（用于简化版地图）
const mapBounds = {
  minLat: 39.8,
  maxLat: 40.0,
  minLng: 116.2,
  maxLng: 116.6
}

const searchForm = reactive({
  facilityType: null,
  status: null
})

const distanceForm = reactive({
  latitude: null,
  longitude: null,
  radius: 500,
  unit: 'm',
  quickRange: '500m'
})

const navigationForm = reactive({
  targetFacility: null,
  mode: 'walking'
})

const stats = reactive({
  normal: 0,
  damaged: 0,
  repairing: 0,
  total: 0
})

const loadFacilities = async () => {
  loading.value = true
  try {
    const res = await request.get('/facilities', {
      params: {
        pageNum: 1,
        pageSize: 100,
        facilityType: searchForm.facilityType,
        status: searchForm.status
        // 移除auditStatus限制，显示所有数据
      }
    })
    
    facilities.value = res.data.records || []
    updateStats()
    
    ElMessage.success(`加载了 ${stats.total} 个设施`)
  } catch (error) {
    console.error('加载设施失败:', error)
    ElMessage.error('加载设施失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.facilityType = null
  searchForm.status = null
  loadFacilities()
}

const viewDetail = async (facility) => {
  try {
    const res = await request.get(`/facilities/${facility.facilityId}`)
    currentFacility.value = res.data
    detailVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

const openInMap = (facility) => {
  // 在高德地图中打开
  const url = `https://uri.amap.com/marker?position=${facility.longitude},${facility.latitude}&name=${facility.facilityName}&src=myapp`
  window.open(url, '_blank')
}

// 获取当前位置
const getCurrentLocation = () => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        distanceForm.latitude = parseFloat(position.coords.latitude.toFixed(6))
        distanceForm.longitude = parseFloat(position.coords.longitude.toFixed(6))
        ElMessage.success('定位成功')
        // 自动将地图中心移动到当前位置
        if (map) {
          map.setView([distanceForm.latitude, distanceForm.longitude], 15)
          updateCurrentLocationMarker()
        }
      },
      (error) => {
        console.error('定位失败:', error)
        ElMessage.warning('定位失败，使用默认位置（中山市）')
        // 使用默认位置（中山市中心）
        distanceForm.latitude = 22.517400
        distanceForm.longitude = 113.392600
        // 自动将地图中心移动到默认位置
        if (map) {
          map.setView([distanceForm.latitude, distanceForm.longitude], 13)
          updateCurrentLocationMarker()
        }
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    )
  } else {
    ElMessage.error('浏览器不支持定位功能')
    // 使用默认位置（中山市中心）
    distanceForm.latitude = 22.517400
    distanceForm.longitude = 113.392600
    if (map) {
      map.setView([distanceForm.latitude, distanceForm.longitude], 13)
      updateCurrentLocationMarker()
    }
  }
}

// 设置快速距离范围
const setQuickRange = (value) => {
  switch (value) {
    case '100m':
      distanceForm.radius = 100
      distanceForm.unit = 'm'
      break
    case '500m':
      distanceForm.radius = 500
      distanceForm.unit = 'm'
      break
    case '1km':
      distanceForm.radius = 1
      distanceForm.unit = 'km'
      break
    case '2km':
      distanceForm.radius = 2
      distanceForm.unit = 'km'
      break
    case 'custom':
      // 保持当前设置
      break
  }
}

// 按距离查询设施
const searchByDistance = async () => {
  if (!distanceForm.latitude || !distanceForm.longitude) {
    ElMessage.warning('请先获取当前位置')
    return
  }

  loading.value = true
  try {
    const params = {
      latitude: distanceForm.latitude,
      longitude: distanceForm.longitude,
      radius: distanceForm.radius,
      unit: distanceForm.unit,
      facilityType: searchForm.facilityType,
      status: searchForm.status
    }

    const res = await request.get('/facilities/nearby/enhanced', { params })
    facilities.value = res.data || []
    updateStats()
    
    ElMessage.success(`找到 ${facilities.value.length} 个设施`)
  } catch (error) {
    console.error('距离查询失败:', error)
    ElMessage.error('距离查询失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 查找最近的设施
const findClosest = async () => {
  if (!distanceForm.latitude || !distanceForm.longitude) {
    ElMessage.warning('请先获取当前位置')
    return
  }

  loading.value = true
  try {
    const params = {
      latitude: distanceForm.latitude,
      longitude: distanceForm.longitude,
      limit: 3,
      facilityType: searchForm.facilityType,
      status: searchForm.status
    }

    const res = await request.get('/facilities/closest', { params })
    facilities.value = res.data || []
    updateStats()
    
    ElMessage.success(`找到最近的 ${facilities.value.length} 个设施`)
  } catch (error) {
    console.error('查找最近设施失败:', error)
    ElMessage.error('查找最近设施失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 紧急模式切换
const onEmergencyModeChange = async (value) => {
  if (value && distanceForm.latitude && distanceForm.longitude) {
    loading.value = true
    try {
      const params = {
        latitude: distanceForm.latitude,
        longitude: distanceForm.longitude,
        radius: distanceForm.radius * (distanceForm.unit === 'km' ? 1000 : 1),
        facilityType: searchForm.facilityType
      }

      const res = await request.get('/facilities/emergency', { params })
      facilities.value = res.data || []
      updateStats()
      
      ElMessage.success(`紧急模式：找到 ${facilities.value.length} 个正常设施`)
    } catch (error) {
      console.error('紧急模式查询失败:', error)
      ElMessage.error('紧急模式查询失败')
      emergencyMode.value = false
    } finally {
      loading.value = false
    }
  }
}

// 路径规划
const planRoute = async () => {
  if (!navigationForm.targetFacility) {
    ElMessage.warning('请选择目标设施')
    return
  }

  if (!distanceForm.latitude || !distanceForm.longitude) {
    ElMessage.warning('请先获取当前位置')
    return
  }

  const targetFacility = facilities.value.find(f => f.facilityId === navigationForm.targetFacility)
  if (!targetFacility) {
    ElMessage.error('目标设施不存在')
    return
  }

  try {
    const params = {
      startLat: distanceForm.latitude,
      startLon: distanceForm.longitude,
      endLat: targetFacility.latitude,
      endLon: targetFacility.longitude,
      mode: navigationForm.mode
    }

    const res = await request.get('/navigation/route', { params })
    if (res.data && res.data.length > 0) {
      routeInfo.value = res.data[0]
      drawRoute(routeInfo.value)
      ElMessage.success('路径规划成功')
    } else {
      ElMessage.error('路径规划失败')
    }
  } catch (error) {
    console.error('路径规划失败:', error)
    ElMessage.error('路径规划失败: ' + (error.message || '未知错误'))
  }
}

// 开始导航
const startNavigation = async () => {
  if (!routeInfo.value) {
    ElMessage.warning('请先进行路径规划')
    return
  }

  try {
    const targetFacility = facilities.value.find(f => f.facilityId === navigationForm.targetFacility)
    const params = {
      startLat: distanceForm.latitude,
      startLon: distanceForm.longitude,
      endLat: targetFacility.latitude,
      endLon: targetFacility.longitude,
      mode: navigationForm.mode
    }

    const res = await request.post('/navigation/start', null, { params })
    ElMessage.success('导航已启动，导航ID: ' + res.data)
    
    // 可以在这里添加实时导航逻辑
  } catch (error) {
    console.error('启动导航失败:', error)
    ElMessage.error('启动导航失败: ' + (error.message || '未知错误'))
  }
}

// 导航到指定设施
const navigateToFacility = (facility) => {
  navigationForm.targetFacility = facility.facilityId
  activeTab.value = 'navigation'
  ElMessage.info('已选择目标设施，请选择出行方式并规划路径')
}

// 打开高德地图导航
const openAmapNavigation = () => {
  if (!navigationForm.targetFacility) {
    ElMessage.warning('请先选择目标设施')
    return
  }

  const targetFacility = facilities.value.find(f => f.facilityId === navigationForm.targetFacility)
  if (!targetFacility) {
    ElMessage.error('目标设施不存在')
    return
  }

  // 构建高德地图导航URL
  let url = `https://uri.amap.com/navigation?to=${targetFacility.longitude},${targetFacility.latitude},${encodeURIComponent(targetFacility.facilityName)}`
  
  // 如果有当前位置，添加起点
  if (distanceForm.latitude && distanceForm.longitude) {
    url += `&from=${distanceForm.longitude},${distanceForm.latitude},当前位置`
  }
  
  // 添加出行方式
  url += `&mode=${navigationForm.mode}&src=fire-management`
  
  // 打开高德地图
  window.open(url, '_blank')
  ElMessage.success('正在打开高德地图导航...')
}

// 格式化距离显示
const formatDistance = (distance) => {
  if (distance === undefined || distance === null) return ''
  
  if (distance >= 1000) {
    return `${(distance / 1000).toFixed(2)} km`
  } else {
    return `${Math.round(distance)} m`
  }
}

// 格式化时间显示
const formatTime = (seconds) => {
  if (!seconds) return ''
  
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  
  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}

// 更新统计数据
const updateStats = () => {
  stats.normal = 0
  stats.damaged = 0
  stats.repairing = 0
  stats.total = facilities.value.length
  
  facilities.value.forEach(facility => {
    if (facility.status === 1) stats.normal++
    else if (facility.status === 2) stats.damaged++
    else if (facility.status === 3) stats.repairing++
  })
}

// 初始化地图
const initMap = () => {
  // 检查是否有地图容器
  const container = document.getElementById('mapContainer')
  if (!container) {
    setTimeout(() => initMap(), 100)
    return
  }

  // 使用Leaflet地图
  if (window.L) {
    initLeafletMap()
  } else {
    loadMapAPI()
  }
}

// 初始化Leaflet地图
const initLeafletMap = () => {
  const container = document.getElementById('mapContainer')
  if (!container) return

  // 如果地图已经存在，先销毁
  if (map) {
    map.remove()
  }

  map = L.map('mapContainer').setView([39.90923, 116.397428], 13)

  // 添加地图图层
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors'
  }).addTo(map)

  // 设置地图已加载
  mapLoaded.value = true

  // 地图点击事件 - 已禁用手动修改位置
  // map.on('click', (e) => {
  //   const { lat, lng } = e.latlng
  //   distanceForm.longitude = parseFloat(lng.toFixed(6))
  //   distanceForm.latitude = parseFloat(lat.toFixed(6))
  //   updateCurrentLocationMarker()
  // })

  // 初始化设施标记
  updateFacilityMarkers()
}

// 加载地图API
const loadMapAPI = () => {
  // 使用免费的OpenStreetMap作为替代方案
  if (!window.L) {
    const leafletCSS = document.createElement('link')
    leafletCSS.rel = 'stylesheet'
    leafletCSS.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'
    document.head.appendChild(leafletCSS)

    const leafletJS = document.createElement('script')
    leafletJS.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'
    leafletJS.onload = () => {
      initLeafletMap()
    }
    document.head.appendChild(leafletJS)
  } else {
    initLeafletMap()
  }
}

// 更新当前位置标记
const updateCurrentLocationMarker = () => {
  if (!map || !distanceForm.latitude || !distanceForm.longitude) return

  // 移除旧标记
  if (currentLocationMarker) {
    map.removeLayer(currentLocationMarker)
  }

  // 创建自定义图标
  const currentLocationIcon = L.divIcon({
    className: 'current-location-marker',
    html: `
      <div style="
        width: 20px; 
        height: 20px; 
        background: #409eff; 
        border: 3px solid #fff; 
        border-radius: 50%; 
        box-shadow: 0 2px 6px rgba(0,0,0,0.3);
      "></div>
    `,
    iconSize: [20, 20],
    iconAnchor: [10, 10]
  })

  // 添加新标记
  currentLocationMarker = L.marker([distanceForm.latitude, distanceForm.longitude], {
    icon: currentLocationIcon
  }).addTo(map)

  currentLocationMarker.bindPopup('当前位置').openPopup()
  updateDistanceCircle()
}

// 更新距离圈
const updateDistanceCircle = () => {
  if (!map || !showDistanceCircle.value || !distanceForm.latitude || !distanceForm.longitude) return

  // 移除旧圆圈
  if (distanceCircle) {
    map.removeLayer(distanceCircle)
  }

  const radius = distanceForm.unit === 'km' ? distanceForm.radius * 1000 : distanceForm.radius

  distanceCircle = L.circle([distanceForm.latitude, distanceForm.longitude], {
    radius: radius,
    color: '#409eff',
    weight: 2,
    opacity: 0.8,
    fillColor: '#409eff',
    fillOpacity: 0.1
  }).addTo(map)
}

// 更新设施标记
const updateFacilityMarkers = () => {
  if (!map) return

  // 清除旧标记
  facilityMarkers.forEach(marker => map.removeLayer(marker))
  facilityMarkers = []

  // 添加新标记
  facilities.value.forEach(facility => {
    // 创建自定义图标
    const facilityIcon = L.divIcon({
      className: 'facility-marker',
      html: `
        <div style="
          width: 28px; 
          height: 28px; 
          background: ${getFacilityColor(facility.status)}; 
          border: 2px solid #fff; 
          border-radius: 50%; 
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          font-size: 12px;
          font-weight: bold;
          box-shadow: 0 2px 6px rgba(0,0,0,0.3);
        ">
          ${facility.facilityType === 1 ? '栓' : '车'}
        </div>
      `,
      iconSize: [28, 28],
      iconAnchor: [14, 14]
    })

    const marker = L.marker([facility.latitude, facility.longitude], {
      icon: facilityIcon,
      title: facility.facilityName
    }).addTo(map)

    // 添加弹出窗口
    const popupContent = `
      <div style="padding: 10px; min-width: 200px;">
        <h4 style="margin: 0 0 8px 0;">${facility.facilityName}</h4>
        <p style="margin: 4px 0;"><strong>地址:</strong> ${facility.address}</p>
        <p style="margin: 4px 0;"><strong>状态:</strong> ${getStatusText(facility.status)}</p>
        ${facility.pressure ? `<p style="margin: 4px 0;"><strong>压力:</strong> ${facility.pressure} MPa</p>` : ''}
        ${facility.distance !== undefined ? `<p style="margin: 4px 0;"><strong>距离:</strong> ${formatDistance(facility.distance)}</p>` : ''}
        <div style="margin-top: 10px;">
          <button onclick="selectFacilityById(${facility.facilityId})" style="margin-right: 8px; padding: 4px 8px; background: #409eff; color: white; border: none; border-radius: 4px; cursor: pointer;">选择</button>
          <button onclick="navigateToFacilityById(${facility.facilityId})" style="padding: 4px 8px; background: #67c23a; color: white; border: none; border-radius: 4px; cursor: pointer;">导航</button>
        </div>
      </div>
    `

    marker.bindPopup(popupContent)

    // 点击事件
    marker.on('click', () => {
      selectFacility(facility)
    })

    facilityMarkers.push(marker)
  })
}

// 获取设施颜色
const getFacilityColor = (status) => {
  switch (status) {
    case 1: return '#67c23a' // 正常 - 绿色
    case 2: return '#f56c6c' // 损坏 - 红色
    case 3: return '#e6a23c' // 维修中 - 黄色
    default: return '#909399' // 未知 - 灰色
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 1: return '正常'
    case 2: return '损坏'
    case 3: return '维修中'
    default: return '未知'
  }
}

// 选择设施
const selectFacility = (facility) => {
  selectedFacility.value = facility
  if (map) {
    map.setView([facility.latitude, facility.longitude], 16)
  }
}

// 地图定位到设施
const centerMapToFacility = (facility) => {
  selectFacility(facility)
  ElMessage.success(`已定位到 ${facility.facilityName}`)
}

// 定位到当前位置
const centerToCurrentLocation = () => {
  if (distanceForm.latitude && distanceForm.longitude) {
    if (map) {
      map.setView([distanceForm.latitude, distanceForm.longitude], 15)
    }
    ElMessage.success('已定位到当前位置')
  } else {
    getCurrentLocation()
  }
}

// 显示所有设施
const showAllFacilities = () => {
  if (facilities.value.length === 0) {
    ElMessage.warning('暂无设施数据')
    return
  }
  
  if (map && facilities.value.length > 0) {
    // 创建包含所有设施的边界
    const group = new L.featureGroup(facilityMarkers)
    map.fitBounds(group.getBounds().pad(0.1))
  }
  
  ElMessage.success(`显示 ${facilities.value.length} 个设施`)
}

// 切换距离圈显示
const toggleDistanceCircle = () => {
  showDistanceCircle.value = !showDistanceCircle.value
  ElMessage.success(showDistanceCircle.value ? '已显示距离圈' : '已隐藏距离圈')
}

// 绘制导航路线
const drawRoute = (routeData) => {
  if (!map || !routeData || !routeData.points) return

  // 移除旧路线
  if (routeLine) {
    map.removeLayer(routeLine)
  }

  const path = routeData.points.map(point => [point.latitude, point.longitude])

  routeLine = L.polyline(path, {
    color: '#ff4757',
    weight: 4,
    opacity: 0.8
  }).addTo(map)

  // 调整地图视野包含整条路线
  const bounds = L.latLngBounds(path)
  map.fitBounds(bounds, { padding: [50, 50] })
}

// 全局函数供信息窗口调用
window.selectFacilityById = (facilityId) => {
  const facility = facilities.value.find(f => f.facilityId === facilityId)
  if (facility) {
    selectFacility(facility)
  }
}

window.navigateToFacilityById = (facilityId) => {
  const facility = facilities.value.find(f => f.facilityId === facilityId)
  if (facility) {
    navigateToFacility(facility)
  }
}

// 初始化简化版地图
const initSimpleMap = () => {
  // 直接加载真正的地图
  initMap()
}

// 更新地图边界
const updateMapBounds = () => {
  if (facilities.value.length === 0) return
  
  const lats = facilities.value.map(f => f.latitude)
  const lngs = facilities.value.map(f => f.longitude)
  
  mapBounds.minLat = Math.min(...lats) - 0.01
  mapBounds.maxLat = Math.max(...lats) + 0.01
  mapBounds.minLng = Math.min(...lngs) - 0.01
  mapBounds.maxLng = Math.max(...lngs) + 0.01
  
  // 如果有当前位置，也包含在边界内
  if (distanceForm.latitude && distanceForm.longitude) {
    mapBounds.minLat = Math.min(mapBounds.minLat, distanceForm.latitude - 0.01)
    mapBounds.maxLat = Math.max(mapBounds.maxLat, distanceForm.latitude + 0.01)
    mapBounds.minLng = Math.min(mapBounds.minLng, distanceForm.longitude - 0.01)
    mapBounds.maxLng = Math.max(mapBounds.maxLng, distanceForm.longitude + 0.01)
  }
}

// 获取设施在地图上的位置
const getFacilityPosition = (facility) => {
  const x = ((facility.longitude - mapBounds.minLng) / (mapBounds.maxLng - mapBounds.minLng)) * 100
  const y = ((mapBounds.maxLat - facility.latitude) / (mapBounds.maxLat - mapBounds.minLat)) * 100
  
  return {
    left: `${x}%`,
    top: `${y}%`
  }
}

// 获取当前位置在地图上的位置
const getCurrentPosition = () => {
  const x = ((distanceForm.longitude - mapBounds.minLng) / (mapBounds.maxLng - mapBounds.minLng)) * 100
  const y = ((mapBounds.maxLat - distanceForm.latitude) / (mapBounds.maxLat - mapBounds.minLat)) * 100
  
  return {
    left: `${x}%`,
    top: `${y}%`
  }
}

// 获取距离圈样式
const getDistanceCircleStyle = () => {
  const centerX = ((distanceForm.longitude - mapBounds.minLng) / (mapBounds.maxLng - mapBounds.minLng)) * 100
  const centerY = ((mapBounds.maxLat - distanceForm.latitude) / (mapBounds.maxLat - mapBounds.minLat)) * 100
  
  // 简化计算，假设1度约等于111km
  const radius = distanceForm.unit === 'km' ? distanceForm.radius : distanceForm.radius / 1000
  const radiusPercent = (radius / 111) / (mapBounds.maxLng - mapBounds.minLng) * 100
  
  return {
    left: `${centerX - radiusPercent}%`,
    top: `${centerY - radiusPercent}%`,
    width: `${radiusPercent * 2}%`,
    height: `${radiusPercent * 2}%`
  }
}

onMounted(() => {
  loadFacilities()
  // 先初始化地图，然后自动定位
  setTimeout(() => {
    initMap()
    // 地图初始化后自动获取当前位置并定位
    setTimeout(() => {
      getCurrentLocation()
    }, 500)
  }, 500)
})

// 监听设施数据变化，更新地图标记
watch(facilities, () => {
  updateFacilityMarkers()
}, { deep: true })

// 监听当前位置变化，更新标记和距离圈
watch([() => distanceForm.latitude, () => distanceForm.longitude], () => {
  updateCurrentLocationMarker()
})

// 监听距离圈参数变化
watch([() => distanceForm.radius, () => distanceForm.unit], () => {
  if (showDistanceCircle.value) {
    updateDistanceCircle()
  }
})
</script>

<style scoped>
.map-simple-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-panel h3 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.query-tabs {
  margin-bottom: 20px;
}

.query-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}

.emergency-mode {
  margin-top: 16px;
  padding: 12px;
  background: #fef0f0;
  border-radius: 6px;
  display: flex;
  align-items: center;
}

.route-info {
  margin-top: 16px;
}

.stats {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.map-container {
  flex: 1;
  min-height: 600px;
}

.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.map-content {
  height: 600px;
  display: flex;
  gap: 16px;
}

.map-content.map .map-area {
  width: 100%;
}

.map-content.list .list-area {
  width: 100%;
}

.map-content.split .map-area {
  width: 60%;
}

.map-content.split .list-area {
  width: 40%;
}

.map-area {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
}

.map-container-inner {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  background: linear-gradient(135deg, #e3f2fd 0%, #f8f9fa 100%);
  position: relative;
  overflow: hidden;
}

/* Leaflet地图样式 */
.map-container-inner :deep(.leaflet-container) {
  height: 100%;
  width: 100%;
  border-radius: 8px;
}

.map-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.map-placeholder p {
  margin: 16px 0;
  font-size: 16px;
}

.simple-map {
  width: 100%;
  height: 100%;
  position: relative;
}

.map-grid {
  width: 100%;
  height: 100%;
  position: relative;
  background-image: 
    linear-gradient(rgba(0,0,0,0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,0,0,0.1) 1px, transparent 1px);
  background-size: 50px 50px;
}

.facility-dot {
  position: absolute;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: bold;
  cursor: pointer;
  transform: translate(-50%, -50%);
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
  border: 2px solid white;
}

.facility-dot:hover {
  transform: translate(-50%, -50%) scale(1.2);
  z-index: 10;
}

.facility-dot.normal {
  background: #67c23a;
}

.facility-dot.damaged {
  background: #f56c6c;
}

.facility-dot.repairing {
  background: #e6a23c;
}

.facility-dot.selected {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.3);
  z-index: 20;
}

.distance-label {
  position: absolute;
  top: -25px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0,0,0,0.8);
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  white-space: nowrap;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.facility-dot:hover .distance-label {
  opacity: 1;
}

.current-position {
  position: absolute;
  width: 24px;
  height: 24px;
  background: #409eff;
  border: 3px solid white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  transform: translate(-50%, -50%);
  box-shadow: 0 2px 8px rgba(0,0,0,0.3);
  z-index: 15;
}

.distance-circle {
  position: absolute;
  border: 2px solid #409eff;
  border-radius: 50%;
  background: rgba(64, 158, 255, 0.1);
  transform: translate(-50%, -50%);
  pointer-events: none;
  z-index: 5;
}

.map-controls {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 1000;
}

.map-legend {
  position: absolute;
  bottom: 10px;
  left: 10px;
  background: rgba(255, 255, 255, 0.9);
  padding: 12px;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.legend-item {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  font-size: 12px;
}

.legend-item:last-child {
  margin-bottom: 0;
}

.legend-icon {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
  border: 1px solid #fff;
}

.legend-icon.normal {
  background-color: #67c23a;
}

.legend-icon.damaged {
  background-color: #f56c6c;
}

.legend-icon.repairing {
  background-color: #e6a23c;
}

.legend-icon.current {
  background-color: #409eff;
}

.list-area {
  overflow-y: auto;
  border-radius: 8px;
  background: #f8f9fa;
  padding: 16px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-weight: 600;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.facility-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 16px;
}

.facility-item {
  transition: all 0.3s ease;
  border-radius: 8px;
  overflow: hidden;
}

.facility-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.facility-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.facility-header h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.facility-info {
  margin-bottom: 12px;
}

.facility-info p {
  margin: 8px 0;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.distance-info {
  color: #409eff !important;
  font-weight: 500;
}

.facility-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.facility-actions .el-button {
  flex: 1;
  min-width: 80px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .facility-grid {
    grid-template-columns: 1fr;
  }
  
  .stats {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .query-tabs :deep(.el-form--inline) .el-form-item {
    display: block;
    margin-right: 0;
    margin-bottom: 12px;
  }
}

/* 距离标签样式 */
.facility-info .el-tag {
  margin-left: 8px;
}

/* 紧急模式高亮 */
.facility-item.emergency-highlight {
  border: 2px solid #ff4757;
  background: linear-gradient(135deg, #fff5f5 0%, #ffffff 100%);
}

.facility-item.emergency-highlight .facility-header h4 {
  color: #ff4757;
}

.facility-item.selected {
  border: 2px solid #409eff;
  background: linear-gradient(135deg, #f0f9ff 0%, #ffffff 100%);
}

.facility-item.selected .facility-header h4 {
  color: #409eff;
}

/* 加载状态优化 */
.list-card :deep(.el-loading-mask) {
  border-radius: 8px;
}

/* 标签页内容间距 */
.query-tabs :deep(.el-tab-pane) {
  padding-top: 8px;
}

/* 表单项间距优化 */
.el-form--inline .el-form-item {
  margin-right: 16px;
  margin-bottom: 12px;
}
</style>
