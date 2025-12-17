/**
 * 子弹图工具函数
 * 用于数据看板的子弹图组件
 */

/**
 * 根据实际值和目标值返回对应的颜色
 * @param {number} actual - 实际值
 * @param {number} target - 目标值
 * @returns {string} 颜色值 - 达标返回绿色，未达标返回橙色
 */
export function getBarColor(actual, target) {
  return actual >= target ? '#67C23A' : '#E6A23C'
}

/**
 * 根据统计数据计算指标
 * @param {Object} stats - 统计数据
 * @param {number} stats.total - 设施总数
 * @param {number} stats.normal - 正常设施数
 * @param {number} stats.damaged - 损坏设施数
 * @param {number} stats.pending - 待审核数
 * @returns {Array<{name: string, actual: number, target: number}>} 指标数组
 */
export function calculateMetrics(stats) {
  const { total, normal, pending } = stats
  
  // 处理 total 为 0 的边界情况
  if (total === 0) {
    return [
      { name: '设施完好率', actual: 0, target: 90 },
      { name: '审核通过率', actual: 0, target: 85 }
    ]
  }
  
  // 设施完好率 = 正常设施数 / 总数 * 100
  const healthRate = Math.round((normal / total) * 100)
  
  // 审核通过率 = (总数 - 待审核数) / 总数 * 100
  const approvalRate = Math.round(((total - pending) / total) * 100)
  
  return [
    { name: '设施完好率', actual: healthRate, target: 90 },
    { name: '审核通过率', actual: approvalRate, target: 85 }
  ]
}

/**
 * 格式化百分比
 * @param {number} value - 数值 (0-100)
 * @returns {string} 带百分号的字符串
 */
export function formatPercent(value) {
  return `${value}%`
}

/**
 * 生成子弹图 ECharts 配置
 * @param {Array<{name: string, actual: number, target: number}>} metrics - 指标数组
 * @returns {Object} ECharts 配置对象
 */
export function getBulletChartOption(metrics) {
  const names = metrics.map(m => m.name)
  const actuals = metrics.map(m => m.actual)
  const targets = metrics.map(m => m.target)
  
  return {
    title: {
      text: '关键指标达成情况',
      left: 'center',
      top: 10
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: function(params) {
        const index = params[0].dataIndex
        const metric = metrics[index]
        return `${metric.name}<br/>实际值: ${formatPercent(metric.actual)}<br/>目标值: ${formatPercent(metric.target)}`
      }
    },
    grid: {
      left: '3%',
      right: '8%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    },
    yAxis: {
      type: 'category',
      data: names,
      axisLine: { show: false },
      axisTick: { show: false }
    },
    series: [
      // 背景区间 - 差 (0-60)
      {
        name: '差',
        type: 'bar',
        stack: 'background',
        silent: true,
        barWidth: 30,
        itemStyle: { color: '#E0E0E0' },
        data: metrics.map(() => 60)
      },
      // 背景区间 - 中 (60-80)
      {
        name: '中',
        type: 'bar',
        stack: 'background',
        silent: true,
        barWidth: 30,
        itemStyle: { color: '#C0C0C0' },
        data: metrics.map(() => 20)
      },
      // 背景区间 - 良 (80-100)
      {
        name: '良',
        type: 'bar',
        stack: 'background',
        silent: true,
        barWidth: 30,
        itemStyle: { color: '#A0A0A0' },
        data: metrics.map(() => 20)
      },
      // 实际值条
      {
        name: '实际值',
        type: 'bar',
        barWidth: 12,
        z: 10,
        data: metrics.map((m, i) => ({
          value: actuals[i],
          itemStyle: { color: getBarColor(m.actual, m.target) }
        }))
      },
      // 目标线
      {
        name: '目标值',
        type: 'scatter',
        symbol: 'rect',
        symbolSize: [4, 30],
        z: 20,
        itemStyle: { color: '#F56C6C' },
        data: targets
      }
    ]
  }
}
