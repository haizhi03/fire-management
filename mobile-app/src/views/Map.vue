<template>
  <div class="map-page">
    <!-- 顶部搜索栏 -->
    <div class="search-bar">
      <van-dropdown-menu>
        <van-dropdown-item v-model="searchForm.facilityType" :options="facilityTypeOptions" @change="loadFacilities" />
        <van-dropdown-item v-model="searchForm.radius" :options="radiusOptions" @change="loadNearbyFacilities" />
      </van-dropdown-menu>
    </div>

    <!-- 地图容器 -->
    <div id="map-container" class="map-container"></div>

    <!-- 底部统计信息 -->
    <div class="stats-bar" v-if="!routeInfo">
      <van-tag type="success" size="medium">正常: {{ stats.normal }}</van-tag>
      <van-tag type="danger" size="medium">损坏: {{ stats.damaged }}</van-tag>
      <van-tag type="warning" size="medium">维修: {{ stats.repairing }}</van-tag>
      <van-tag type="primary" size="medium">总计: {{ stats.total }}</van-tag>
    </div>

    <!-- 路径信息栏 -->
    <div class="route-info-bar" v-if="routeInfo">
      <div class="route-info">
        <span class="route-distance">{{ formatDistance(routeInfo.distance) }}</span>
        <span class="route-time">约 {{ formatTime(routeInfo.time) }}</span>
      </div>
      <div class="route-actions">
        <van-button size="small" type="success" @click="startAmapNav">高德导航</van-button>
        <van-button size="small" type="danger" @click="clearRoute">取消</van-button>
      </div>
    </div>

    <!-- 定位按钮 -->
    <div class="location-btn" @click="locateMe">
      <van-icon name="aim" size="24" />
    </div>

    <!-- 设施详情弹窗 -->
    <van-popup v-model:show="detailVisible" position="bottom" round :style="{ height: '55%' }">
      <div class="detail-popup" v-if="currentFacility">
        <div class="detail-header">
          <h3>{{ currentFacility.facilityName }}</h3>
          <van-tag :type="getStatusType(currentFacility.status)">
            {{ getStatusText(currentFacility.status) }}
          </van-tag>
        </div>
        
        <van-cell-group inset>
          <van-cell title="设施类型" :value="currentFacility.facilityType === 1 ? '消防栓' : '消防车'" />
          <van-cell title="详细地址" :value="currentFacility.address || '暂无'" />
          <van-cell title="经度" :value="currentFacility.longitude" />
          <van-cell title="纬度" :value="currentFacility.latitude" />
          <van-cell title="工作压力" :value="currentFacility.pressure ? currentFacility.pressure + ' MPa' : '暂无'" v-if="currentFacility.facilityType === 1" />
          <van-cell title="距离" :value="formatDistance(currentFacility.distance)" v-if="currentFacility.distance" />
        </van-cell-group>

        <div class="detail-actions">
          <div class="nav-buttons">
            <van-button type="primary" @click="startLocalNav">
              <van-icon name="guide-o" /> 本地导航
            </van-button>
            <van-button type="success" @click="startAmapNav">
              <van-icon name="location-o" /> 高德导航
            </van-button>
          </div>
        </div>
      </div>
    </van-popup>



    <!-- 加载提示 -->
    <van-overlay :show="loading">
      <div class="loading-wrapper">
        <van-loading type="spinner" color="#1989fa" />
        <span>加载中...</span>
      </div>
    </van-overlay>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { showToast } from 'vant'
import request from '@/utils/request'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

// 修复 Leaflet 默认图标问题
delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png'
})

const map = ref(null)
const markers = ref([])
const currentLocation = ref(null)
const detailVisible = ref(false)
const currentFacility = ref(null)
const loading = ref(false)
const currentLocationMarker = ref(null)
const routeLine = ref(null)
const routeInfo = ref(null)
const watchId = ref(null) // 位置监听ID


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

// 下拉选项
const facilityTypeOptions = [
  { text: '全部类型', value: null },
  { text: '消防栓', value: 1 },
  { text: '消防车', value: 2 }
]

const radiusOptions = [
  { text: '500米', value: 500 },
  { text: '1公里', value: 1000 },
  { text: '2公里', value: 2000 },
  { text: '5公里', value: 5000 }
]

// 初始化地图
const initMap = () => {
  const defaultCenter = [39.90923, 116.397428]
  
  map.value = L.map('map-container', {
    center: defaultCenter,
    zoom: 13,
    zoomControl: false
  })

  L.tileLayer('https://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}', {
    subdomains: ['1', '2', '3', '4'],
    maxZoom: 18,
    attribution: '&copy; 高德地图'
  }).addTo(map.value)

  locateMe()
}

// 更新位置标记（不移动地图视图）
const updateLocationMarker = (latitude, longitude) => {
  if (currentLocationMarker.value) {
    currentLocationMarker.value.setLatLng([latitude, longitude])
  } else {
    const myIcon = L.divIcon({
      className: 'my-location-icon',
      html: '<div class="pulse-marker"></div>',
      iconSize: [20, 20],
      iconAnchor: [10, 10]
    })
    currentLocationMarker.value = L.marker([latitude, longitude], { 
      icon: myIcon,
      draggable: false // 禁止拖动
    })
      .addTo(map.value)
      .bindPopup('我的位置')
  }
}

// 定位当前位置（点击定位按钮时调用，会移动地图视图）
const locateMe = () => {
  loading.value = true
  
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords
        currentLocation.value = { latitude, longitude }
        
        // 移动地图到当前位置
        map.value.setView([latitude, longitude], 15)
        updateLocationMarker(latitude, longitude)
        
        loading.value = false
        showToast('定位成功')
        loadNearbyFacilities()
      },
      (error) => {
        loading.value = false
        console.error('定位失败:', error)
        showToast('定位失败，使用默认位置')
        // 使用默认位置并显示标记
        const defaultLat = 39.90923
        const defaultLon = 116.397428
        currentLocation.value = { latitude: defaultLat, longitude: defaultLon }
        map.value.setView([defaultLat, defaultLon], 13)
        updateLocationMarker(defaultLat, defaultLon)
        loadFacilities()
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    )
  } else {
    loading.value = false
    showToast('浏览器不支持定位')
    // 使用默认位置并显示标记
    const defaultLat = 39.90923
    const defaultLon = 116.397428
    currentLocation.value = { latitude: defaultLat, longitude: defaultLon }
    map.value.setView([defaultLat, defaultLon], 13)
    updateLocationMarker(defaultLat, defaultLon)
    loadFacilities()
  }
}

// 开始实时位置监听
const startWatchingPosition = () => {
  if (navigator.geolocation && !watchId.value) {
    watchId.value = navigator.geolocation.watchPosition(
      (position) => {
        const { latitude, longitude } = position.coords
        currentLocation.value = { latitude, longitude }
        // 只更新标记位置，不移动地图视图
        updateLocationMarker(latitude, longitude)
      },
      (error) => {
        console.error('位置监听失败:', error)
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 5000 }
    )
  }
}

// 停止位置监听
const stopWatchingPosition = () => {
  if (watchId.value) {
    navigator.geolocation.clearWatch(watchId.value)
    watchId.value = null
  }
}

// 加载所有设施
const loadFacilities = async () => {
  try {
    loading.value = true
    const res = await request.get('/facilities', {
      params: { pageNum: 1, pageSize: 500, facilityType: searchForm.facilityType, auditStatus: 1 }
    })
    clearMarkers()
    updateStats(res.data.records)
    addMarkersToMap(res.data.records)
    loading.value = false
    showToast(stats.total > 0 ? `已加载 ${stats.total} 个设施` : '暂无设施数据')
  } catch (error) {
    loading.value = false
    console.error('加载设施失败:', error)
    showToast('加载设施失败')
  }
}

// 加载周边设施
const loadNearbyFacilities = async () => {
  if (!currentLocation.value) {
    showToast('请先获取当前位置')
    return
  }
  try {
    loading.value = true
    const res = await request.get('/facilities/nearby', {
      params: {
        latitude: currentLocation.value.latitude,
        longitude: currentLocation.value.longitude,
        radius: searchForm.radius,
        facilityType: searchForm.facilityType
      }
    })
    clearMarkers()
    updateStats(res.data)
    addMarkersToMap(res.data)
    loading.value = false
    showToast(stats.total > 0 ? `找到 ${stats.total} 个周边设施` : '周边暂无设施')
  } catch (error) {
    loading.value = false
    console.error('查询周边设施失败:', error)
    showToast('查询周边设施失败')
  }
}

// 清除标记
const clearMarkers = () => {
  markers.value.forEach(marker => map.value.removeLayer(marker))
  markers.value = []
}

// 更新统计数据
const updateStats = (facilities) => {
  stats.normal = 0
  stats.damaged = 0
  stats.repairing = 0
  stats.total = facilities.length
  facilities.forEach(f => {
    if (f.status === 1) stats.normal++
    else if (f.status === 2) stats.damaged++
    else if (f.status === 3) stats.repairing++
  })
}

// 添加标记到地图
const addMarkersToMap = (facilities) => {
  facilities.forEach(facility => {
    const color = getStatusColor(facility.status)
    const icon = L.divIcon({
      className: 'facility-marker',
      html: `<div class="marker-content" style="background-color: ${color}">${facility.facilityType === 1 ? '栓' : '车'}</div>`,
      iconSize: [30, 30],
      iconAnchor: [15, 15]
    })
    const marker = L.marker([facility.latitude, facility.longitude], { icon })
      .addTo(map.value)
      .on('click', () => showFacilityDetail(facility))
    markers.value.push(marker)
  })
}

// 显示设施详情
const showFacilityDetail = async (facility) => {
  try {
    const res = await request.get(`/facilities/${facility.facilityId}`)
    currentFacility.value = { ...res.data, distance: facility.distance }
    detailVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    showToast('获取详情失败')
  }
}

// 路径规划
const planRoute = async () => {
  if (!currentLocation.value || !currentFacility.value) {
    showToast('无法获取位置信息')
    return
  }

  try {
    loading.value = true
    detailVisible.value = false

    const res = await request.get('/navigation/route', {
      params: {
        startLat: currentLocation.value.latitude,
        startLon: currentLocation.value.longitude,
        endLat: currentFacility.value.latitude,
        endLon: currentFacility.value.longitude,
        mode: 'walking'
      }
    })

    if (res.data && res.data.length > 0) {
      const route = res.data[0]
      drawRoute(route)
      routeInfo.value = {
        distance: route.totalDistance,
        time: route.estimatedTime
      }
      showToast('路径规划成功')
    } else {
      showToast('未找到路径')
    }
    loading.value = false
  } catch (error) {
    loading.value = false
    console.error('路径规划失败:', error)
    showToast('路径规划失败')
  }
}

// 绘制路线
const drawRoute = (route) => {
  // 清除旧路线
  if (routeLine.value) {
    map.value.removeLayer(routeLine.value)
  }

  if (route.points && route.points.length >= 2) {
    const latlngs = route.points.map(p => [p.latitude, p.longitude])
    
    // 绘制路线
    routeLine.value = L.polyline(latlngs, {
      color: '#1989fa',
      weight: 6,
      opacity: 0.8,
      dashArray: '10, 10'
    }).addTo(map.value)

    // 调整地图视野以显示整条路线
    map.value.fitBounds(routeLine.value.getBounds(), { padding: [50, 50] })
  }
}

// 清除路线
const clearRoute = () => {
  if (routeLine.value) {
    map.value.removeLayer(routeLine.value)
    routeLine.value = null
  }
  routeInfo.value = null
}

// 本地导航
const startLocalNav = () => {
  detailVisible.value = false
  planRoute()
}

// 高德导航
const startAmapNav = () => {
  if (!currentFacility.value) return
  const { longitude, latitude, facilityName } = currentFacility.value
  // 高德地图导航 URI
  const url = `https://uri.amap.com/navigation?to=${longitude},${latitude},${encodeURIComponent(facilityName || '目的地')}&mode=walking&src=fire-management`
  window.location.href = url
}

// 获取状态颜色
const getStatusColor = (status) => {
  switch (status) {
    case 1: return '#52c41a'
    case 2: return '#ff4d4f'
    case 3: return '#faad14'
    default: return '#1890ff'
  }
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 1: return 'success'
    case 2: return 'danger'
    case 3: return 'warning'
    default: return 'primary'
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

// 格式化距离
const formatDistance = (distance) => {
  if (!distance) return ''
  if (distance < 1000) return `${Math.round(distance)} 米`
  return `${(distance / 1000).toFixed(2)} 公里`
}

// 格式化时间
const formatTime = (seconds) => {
  if (!seconds) return ''
  if (seconds < 60) return `${Math.round(seconds)} 秒`
  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) return `${minutes} 分钟`
  const hours = Math.floor(minutes / 60)
  const remainMinutes = minutes % 60
  return `${hours} 小时 ${remainMinutes} 分钟`
}

onMounted(() => {
  initMap()
  // 开始实时位置监听
  startWatchingPosition()
})

onBeforeUnmount(() => {
  // 停止位置监听
  stopWatchingPosition()
  if (map.value) map.value.remove()
})
</script>


<style scoped>
.map-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
}

.search-bar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: white;
}

.map-container {
  flex: 1;
  width: 100%;
  margin-top: 48px;
  margin-bottom: 40px;
}

.stats-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: white;
  padding: 8px 12px;
  display: flex;
  justify-content: space-around;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
}

.route-info-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: white;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
}

.route-actions {
  display: flex;
  gap: 8px;
}

.route-info {
  display: flex;
  flex-direction: column;
}

.route-distance {
  font-size: 18px;
  font-weight: bold;
  color: #1989fa;
}

.route-time {
  font-size: 14px;
  color: #666;
}

.location-btn {
  position: absolute;
  right: 16px;
  bottom: 60px;
  z-index: 1000;
  width: 44px;
  height: 44px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  cursor: pointer;
}

.location-btn:active {
  background: #f5f5f5;
}

.detail-popup {
  padding: 16px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.detail-header h3 {
  margin: 0;
  font-size: 18px;
}

.detail-actions {
  margin-top: 16px;
  padding: 0 16px;
}

.nav-buttons {
  display: flex;
  gap: 12px;
}

.nav-buttons .van-button {
  flex: 1;
}

.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: white;
}

.loading-wrapper span {
  margin-top: 8px;
}

:deep(.facility-marker) {
  background: transparent;
  border: none;
}

:deep(.marker-content) {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: bold;
}

:deep(.my-location-icon) {
  background: transparent;
  border: none;
}

:deep(.pulse-marker) {
  width: 20px;
  height: 20px;
  background: #1989fa;
  border-radius: 50%;
  border: 3px solid white;
  box-shadow: 0 0 0 rgba(25, 137, 250, 0.4);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(25, 137, 250, 0.4); }
  70% { box-shadow: 0 0 0 15px rgba(25, 137, 250, 0); }
  100% { box-shadow: 0 0 0 0 rgba(25, 137, 250, 0); }
}
</style>
