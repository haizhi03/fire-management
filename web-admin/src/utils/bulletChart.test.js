import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'
import { getBarColor, calculateMetrics, formatPercent, getBulletChartOption } from './bulletChart'

describe('bulletChart utils', () => {
  /**
   * **Feature: dashboard-bullet-chart, Property 2: 颜色选择逻辑正确性**
   * *For any* 实际值和目标值的组合，当实际值 >= 目标值时，getBarColor 函数 SHALL 返回绿色 '#67C23A'；
   * 当实际值 < 目标值时，SHALL 返回橙色 '#E6A23C'
   * **Validates: Requirements 2.1, 2.2**
   */
  describe('getBarColor - Property 2', () => {
    it('should return green when actual >= target', () => {
      fc.assert(
        fc.property(
          fc.integer({ min: 0, max: 100 }),
          fc.integer({ min: 0, max: 100 }),
          (actual, target) => {
            fc.pre(actual >= target)
            return getBarColor(actual, target) === '#67C23A'
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should return orange when actual < target', () => {
      fc.assert(
        fc.property(
          fc.integer({ min: 0, max: 100 }),
          fc.integer({ min: 0, max: 100 }),
          (actual, target) => {
            fc.pre(actual < target)
            return getBarColor(actual, target) === '#E6A23C'
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * **Feature: dashboard-bullet-chart, Property 4: 指标计算正确性**
   * *For any* 有效的统计数据（total > 0），calculateMetrics 函数 SHALL 返回正确的完好率（normal/total * 100）
   * **Validates: Requirements 5.2**
   */
  describe('calculateMetrics - Property 4', () => {
    it('should calculate health rate correctly for any valid stats', () => {
      fc.assert(
        fc.property(
          fc.integer({ min: 1, max: 10000 }),
          fc.integer({ min: 0, max: 10000 }),
          fc.integer({ min: 0, max: 10000 }),
          fc.integer({ min: 0, max: 10000 }),
          (total, normal, damaged, pending) => {
            // 确保 normal 不超过 total
            const validNormal = Math.min(normal, total)
            const validPending = Math.min(pending, total)
            
            const stats = { total, normal: validNormal, damaged, pending: validPending }
            const metrics = calculateMetrics(stats)
            
            const expectedHealthRate = Math.round((validNormal / total) * 100)
            const expectedApprovalRate = Math.round(((total - validPending) / total) * 100)
            
            return metrics[0].actual === expectedHealthRate &&
                   metrics[1].actual === expectedApprovalRate
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should return 0 rates when total is 0', () => {
      const stats = { total: 0, normal: 0, damaged: 0, pending: 0 }
      const metrics = calculateMetrics(stats)
      
      expect(metrics[0].actual).toBe(0)
      expect(metrics[1].actual).toBe(0)
    })
  })

  /**
   * **Feature: dashboard-bullet-chart, Property 3: 百分比格式化正确性**
   * *For any* 0-100 范围内的数值，格式化函数 SHALL 返回带有 '%' 后缀的字符串表示
   * **Validates: Requirements 3.2**
   */
  describe('formatPercent - Property 3', () => {
    it('should format any number with % suffix', () => {
      fc.assert(
        fc.property(
          fc.integer({ min: 0, max: 100 }),
          (value) => {
            const result = formatPercent(value)
            return result === `${value}%` && result.endsWith('%')
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * **Feature: dashboard-bullet-chart, Property 1: 子弹图配置包含必要视觉元素**
   * *For any* 有效的指标数据数组，生成的子弹图配置 SHALL 包含背景区间系列、实际值条系列和目标线系列
   * **Validates: Requirements 1.3**
   */
  describe('getBulletChartOption - Property 1', () => {
    it('should contain background ranges, actual bar, and target line series', () => {
      fc.assert(
        fc.property(
          fc.array(
            fc.record({
              name: fc.string({ minLength: 1, maxLength: 20 }),
              actual: fc.integer({ min: 0, max: 100 }),
              target: fc.integer({ min: 0, max: 100 })
            }),
            { minLength: 1, maxLength: 5 }
          ),
          (metrics) => {
            const option = getBulletChartOption(metrics)
            
            // 验证 series 存在且包含 5 个系列
            if (!option.series || option.series.length !== 5) return false
            
            // 验证背景区间 (3个灰色系列)
            const backgroundSeries = option.series.slice(0, 3)
            const hasBackgrounds = backgroundSeries.every(s => 
              s.type === 'bar' && s.stack === 'background'
            )
            
            // 验证实际值条
            const actualSeries = option.series[3]
            const hasActualBar = actualSeries.type === 'bar' && actualSeries.name === '实际值'
            
            // 验证目标线
            const targetSeries = option.series[4]
            const hasTargetLine = targetSeries.type === 'scatter' && targetSeries.name === '目标值'
            
            return hasBackgrounds && hasActualBar && hasTargetLine
          }
        ),
        { numRuns: 100 }
      )
    })
  })
})
