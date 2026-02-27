<template>
  <div class="exam-report">
    <el-card class="report-card" v-loading="loading">
      <template #header>
        <div class="report-header">
          <h2>{{ report?.examName || '成绩报告' }}</h2>
        </div>
      </template>

      <div v-if="report" class="report-content">
        <!-- 总分展示 -->
        <div class="score-section">
          <div class="total-score-circle" :class="report.passed ? 'pass' : 'fail'">
            <div class="score-value">{{ report.totalScore || '--' }}</div>
            <div class="score-label">总分</div>
          </div>
          <div class="score-details">
            <div class="score-item">
              <span class="item-label">客观题得分</span>
              <span class="item-value">{{ report.objectiveScore || 0 }}</span>
            </div>
            <div class="score-item">
              <span class="item-label">主观题得分</span>
              <span class="item-value">{{ report.subjectiveScore || 0 }}</span>
            </div>
            <div class="score-item">
              <span class="item-label">考试结果</span>
              <el-tag :type="report.passed ? 'success' : 'danger'" size="large">
                {{ report.passed ? '通过' : '未通过' }}
              </el-tag>
            </div>
            <div class="score-item">
              <span class="item-label">用时</span>
              <span class="item-value">{{ formatTime(report.usedTime) }}</span>
            </div>
          </div>
        </div>

        <!-- ECharts图表 -->
        <div ref="chartRef" class="chart-container" />

        <el-button type="primary" @click="$router.back()">返回</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getMyReport } from '@/api/stats'
import * as echarts from 'echarts'

const route = useRoute()
const recordId = route.params.recordId
const report = ref(null)
const loading = ref(false)
const chartRef = ref()
let chartInstance = null

onMounted(async () => {
  loading.value = true
  try {
    const res = await getMyReport(recordId)
    report.value = res.data
    await nextTick()
    initChart()
  } catch (e) {} finally {
    loading.value = false
  }
})

const initChart = () => {
  if (!chartRef.value || !report.value) return
  chartInstance = echarts.init(chartRef.value)
  const option = {
    title: { text: '得分分布', left: 'center' },
    tooltip: {},
    series: [{
      type: 'pie',
      radius: '60%',
      data: [
        { name: '客观题', value: report.value.objectiveScore || 0 },
        { name: '主观题', value: report.value.subjectiveScore || 0 },
      ],
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } },
    }],
  }
  chartInstance.setOption(option)
}

const formatTime = (seconds) => {
  if (!seconds) return '--'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}分${s}秒`
}
</script>

<style scoped lang="scss">
.exam-report {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 16px;
}

.report-header h2 { text-align: center; }

.score-section {
  display: flex;
  align-items: center;
  gap: 40px;
  margin-bottom: 32px;
  padding: 24px;
  background: #f9f9f9;
  border-radius: 12px;
}

.total-score-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.pass { background: linear-gradient(135deg, #52c41a, #73d13d); }
  &.fail { background: linear-gradient(135deg, #ff4d4f, #ff7875); }

  .score-value { font-size: 36px; font-weight: bold; color: #fff; }
  .score-label { font-size: 14px; color: rgba(255,255,255,0.8); }
}

.score-details {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;

  .score-item {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .item-label { font-size: 13px; color: #999; }
    .item-value { font-size: 18px; font-weight: bold; color: #333; }
  }
}

.chart-container {
  height: 300px;
  margin-bottom: 24px;
}
</style>
