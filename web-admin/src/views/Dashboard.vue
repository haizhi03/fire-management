<template>
  <div class="dashboard-container">
    <!-- 消防安全标语 - 打字机效果 -->
    <div class="slogan-banner">
      <div class="slogan-content">
        <span class="slogan-icon">🔥</span>
        <span class="slogan-text">{{ displayText }}<span class="cursor">|</span></span>
      </div>
    </div>

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

    <!-- 子弹图展示 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="24">
        <el-card class="chart-card">
          <div ref="bulletChartRef" class="chart-container"></div>
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
import { calculateMetrics, getBulletChartOption } from '@/utils/bulletChart'

const router = useRouter()

// 消防安全标语列表
const slogans = [
  '油锅起火不用慌，快关火源盖锅盖',
  '消防安全同心同行，平安中山共建共享',
  '电脑且有防火墙，人脑更须防火心',
  '消防隐患要消除，中山人民才享福',
  '中山拥抱消防，消防助力中山',
  '火星虽小莫轻视，燃尽基业悔断肠',
  '与火灾作斗争，是我们共同的责任',
  '多一点消防意识，谋中山民生福祉',
  '星星之火可燎原，中山防火记心间',
  '消防安全连你我，平安福城美万家'
]

const displayText = ref('')
let sloganIndex = 0
let charIndex = 0
let isDeleting = false
let typewriterTimer = null

// 打字机效果
const typeWriter = () => {
  const currentSlogan = slogans[sloganIndex]
  
  if (!isDeleting) {
    // 打字
    displayText.value = currentSlogan.substring(0, charIndex + 1)
    charIndex++
    
    if (charIndex === currentSlogan.length) {
      // 打完一句，等待后开始删除
      isDeleting = true
      typewriterTimer = setTimeout(typeWriter, 2000)
      return
    }
  } else {
    // 删除
    displayText.value = currentSlogan.substring(0, charIndex - 1)
    charIndex--
    
    if (charIndex === 0) {
      // 删完，切换到下一句
      isDeleting = false
      sloganIndex = (sloganIndex + 1) % slogans.length
    }
  }
  
  typewriterTimer = setTimeout(typeWriter, isDeleting ? 50 : 100)
}

const stats = ref({
  total: 0,
  normal: 0,
  damaged: 0,
  pending: 0
})

const typeChartRef = ref(null)
const trendChartRef = ref(null)
const statusChartRef = ref(null)
const bulletChartRef = ref(null)

let typeChart = null
let trendChart = null
let statusChart = null
let bulletChart = null
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
  initBulletChart()
  initTypeChart()
  initTrendChart()
  initStatusChart()
}

// 子弹图 - 关键指标达成情况
const initBulletChart = () => {
  if (!bulletChartRef.value) return
  
  if (bulletChart) {
    bulletChart.dispose()
  }
  
  bulletChart = echarts.init(bulletChartRef.value)
  
  const metrics = calculateMetrics(stats.value)
  const option = getBulletChartOption(metrics)
  
  bulletChart.setOption(option)
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
  bulletChart?.resize()
  typeChart?.resize()
  trendChart?.resize()
  statusChart?.resize()
}

onMounted(() => {
  fetchStats()
  
  // 启动打字机效果
  typeWriter()
  
  // 每30秒自动刷新
  refreshTimer = setInterval(fetchStats, 30000)
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  
  if (typewriterTimer) {
    clearTimeout(typewriterTimer)
  }
  
  bulletChart?.dispose()
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

.stats-row,
.charts-row,
.quick-actions-card,
.slogan-banner {
  position: relative;
  z-index: 1;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border: none;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
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

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 300px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: none;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.chart-container {
  width: 100%;
  height: 280px;
}

.quick-actions-card {
  margin-top: 20px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: none;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.quick-actions-card h3 {
  margin: 0 0 20px 0;
  color: #333;
}

.action-item {
  text-align: center;
  padding: 20px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s;
}

.action-item:hover {
  background: rgba(64, 158, 255, 0.1);
}

.action-item h4 {
  margin: 15px 0 5px 0;
  color: #333;
}

.action-count {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.action-desc {
  margin: 0;
  color: #999;
  font-size: 14px;
}

/* 消防安全标语样式 */
.slogan-banner {
  background: linear-gradient(135deg, #F56C6C 0%, #E6A23C 100%);
  border-radius: 12px;
  padding: 20px 30px;
  margin-bottom: 20px;
  box-shadow: 0 4px 15px rgba(245, 108, 108, 0.3);
}

.slogan-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.slogan-icon {
  font-size: 28px;
  animation: flame 0.5s ease-in-out infinite alternate;
}

@keyframes flame {
  from {
    transform: scale(1) rotate(-5deg);
  }
  to {
    transform: scale(1.1) rotate(5deg);
  }
}

.slogan-text {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  min-height: 28px;
}

.cursor {
  animation: blink 0.8s infinite;
  font-weight: 300;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}
</style>
