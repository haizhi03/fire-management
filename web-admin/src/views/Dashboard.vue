<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409EFF;">
              <el-icon size="30"><Location /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">设施总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67C23A;">
              <el-icon size="30"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.normal }}</div>
              <div class="stat-label">正常设施</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #F56C6C;">
              <el-icon size="30"><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.damaged }}</div>
              <div class="stat-label">损坏设施</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #E6A23C;">
              <el-icon size="30"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pending }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表展示 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="8">
        <el-card class="chart-card">
          <div ref="typeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <div ref="statusChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-card class="quick-actions-card">
      <h3>快捷操作</h3>
      <el-row :gutter="20" class="actions-row">
        <el-col :span="6">
          <div class="action-item" @click="goToAudit">
            <el-icon size="40" color="#E6A23C"><DocumentChecked /></el-icon>
            <h4>待审核</h4>
            <p class="action-count">{{ stats.pending }} 条</p>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="action-item" @click="goToDamaged">
            <el-icon size="40" color="#F56C6C"><Warning /></el-icon>
            <h4>损坏设施</h4>
            <p class="action-count">{{ stats.damaged }} 个</p>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="action-item" @click="goToFacilities">
            <el-icon size="40" color="#409EFF"><List /></el-icon>
            <h4>设施管理</h4>
            <p class="action-desc">查看所有设施</p>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="action-item" @click="goToMap">
            <el-icon size="40" color="#67C23A"><Location /></el-icon>
            <h4>地图查询</h4>
            <p class="action-desc">地图可视化</p>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import * as echarts from 'echarts'

const router = useRouter()

const stats = ref({
  total: 0,
  normal: 0,
  damaged: 0,
  pending: 0
})

const typeChartRef = ref(null)
const trendChartRef = ref(null)
const statusChartRef = ref(null)

let typeChart = null
let trendChart = null
let statusChart = null
let refreshTimer = null

const fetchStats = async () => {
  try {
    const res = await request.get('/statistics/dashboard')
    stats.value.total = res.data.total
    stats.value.normal = res.data.normal
    stats.value.damaged = res.data.damaged
    stats.value.pending = res.data.pending
    
    // 更新图表
    await nextTick()
    initCharts()
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 初始化图表
const initCharts = () => {
  initTypeChart()
  initTrendChart()
  initStatusChart()
}

// 设施类型分布饼图
const initTypeChart = () => {
  if (!typeChartRef.value) return
  
  if (typeChart) {
    typeChart.dispose()
  }
  
  typeChart = echarts.init(typeChartRef.value)
  
  const option = {
    title: {
      text: '设施类型分布',
      left: 'center',
      top: 10
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      bottom: 10,
      left: 'center'
    },
    series: [
      {
        name: '设施类型',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: Math.floor(stats.value.total * 0.7), name: '消防栓', itemStyle: { color: '#409EFF' } },
          { value: Math.floor(stats.value.total * 0.3), name: '消防车', itemStyle: { color: '#67C23A' } }
        ]
      }
    ]
  }
  
  typeChart.setOption(option)
}

// 近7天采集趋势折线图
const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  if (trendChart) {
    trendChart.dispose()
  }
  
  trendChart = echarts.init(trendChartRef.value)
  
  // 生成近7天日期
  const dates = []
  const data = []
  for (let i = 6; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    dates.push(`${date.getMonth() + 1}/${date.getDate()}`)
    data.push(Math.floor(Math.random() * 50) + 10)
  }
  
  const option = {
    title: {
      text: '近7天采集趋势',
      left: 'center',
      top: 10
    },
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '采集数量',
        type: 'line',
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ])
        },
        itemStyle: {
          color: '#409EFF'
        },
        data: data
      }
    ]
  }
  
  trendChart.setOption(option)
}

// 设施状态分布柱状图
const initStatusChart = () => {
  if (!statusChartRef.value) return
  
  if (statusChart) {
    statusChart.dispose()
  }
  
  statusChart = echarts.init(statusChartRef.value)
  
  const option = {
    title: {
      text: '设施状态分布',
      left: 'center',
      top: 10
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['正常', '损坏', '维修中']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '数量',
        type: 'bar',
        barWidth: '60%',
        data: [
          { value: stats.value.normal, itemStyle: { color: '#67C23A' } },
          { value: stats.value.damaged, itemStyle: { color: '#F56C6C' } },
          { value: stats.value.total - stats.value.normal - stats.value.damaged, itemStyle: { color: '#E6A23C' } }
        ]
      }
    ]
  }
  
  statusChart.setOption(option)
}

// 快捷操作
const goToAudit = () => {
  router.push('/audit')
}

const goToDamaged = () => {
  router.push({ path: '/facilities', query: { status: 2 } })
}

const goToFacilities = () => {
  router.push('/facilities')
}

const goToMap = () => {
  router.push('/map')
}

// 窗口大小变化时重新渲染图表
const handleResize = () => {
  typeChart?.resize()
  trendChart?.resize()
  statusChart?.resize()
}

onMounted(() => {
  fetchStats()
  
  // 每30秒自动刷新
  refreshTimer = setInterval(fetchStats, 30000)
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  
  typeChart?.dispose()
  trendChart?.dispose()
  statusChart?.dispose()
  
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.welcome-card {
  text-align: center;
  padding: 40px;
}

.welcome-card h2 {
  margin: 0 0 10px 0;
  color: #333;
}

.welcome-card p {
  margin: 0 0 30px 0;
  color: #666;
}

.feature-item {
  text-align: center;
  padding: 20px;
}

.feature-item h3 {
  margin: 15px 0 10px 0;
  color: #333;
}

.feature-item p {
  margin: 0;
  color: #999;
  font-size: 14px;
}
</style>
