<template>
  <div class="exam-stats">
    <el-row :gutter="16" class="overview-cards">
      <el-col :span="4" v-for="(item, key) in overview" :key="key">
        <el-card shadow="hover" class="overview-card">
          <div class="card-value">{{ item.value }}</div>
          <div class="card-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="charts-row">
      <el-col :span="12">
        <el-card header="分数段分布">
          <div ref="distributionChartRef" class="chart" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="通过率分析">
          <div ref="passRateChartRef" class="chart" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getExamOverview, getScoreDistribution } from '@/api/stats'
import * as echarts from 'echarts'

const route = useRoute()
const examId = route.params.examId

const overview = ref({
  total: { label: '参考人数', value: '--' },
  avgScore: { label: '平均分', value: '--' },
  passRate: { label: '通过率', value: '--' },
  maxScore: { label: '最高分', value: '--' },
  minScore: { label: '最低分', value: '--' },
  passCount: { label: '通过人数', value: '--' },
})

const distributionChartRef = ref()
const passRateChartRef = ref()

onMounted(async () => {
  try {
    const [overviewRes, distRes] = await Promise.all([
      getExamOverview(examId),
      getScoreDistribution(examId),
    ])

    const data = overviewRes.data
    overview.value.total.value = data.total
    overview.value.avgScore.value = data.avgScore
    overview.value.passRate.value = data.passRate + '%'
    overview.value.maxScore.value = data.maxScore
    overview.value.minScore.value = data.minScore
    overview.value.passCount.value = data.passCount

    await nextTick()

    // 分数段分布柱状图
    const distChart = echarts.init(distributionChartRef.value)
    distChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: distRes.data.labels },
      yAxis: { type: 'value', name: '人数' },
      series: [{
        type: 'bar',
        data: distRes.data.counts,
        itemStyle: { color: '#409EFF' },
        label: { show: true, position: 'top' },
      }],
    })

    // 通过率饼图
    const passChart = echarts.init(passRateChartRef.value)
    passChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        type: 'pie',
        radius: '65%',
        data: [
          { name: '通过', value: data.passCount },
          { name: '未通过', value: data.total - data.passCount },
        ],
        itemStyle: {
          color: (params) => params.name === '通过' ? '#52c41a' : '#ff4d4f',
        },
        label: {
          formatter: '{b}: {c}人 ({d}%)',
        },
      }],
    })
  } catch (e) {}
})
</script>

<style scoped lang="scss">
.exam-stats {
  .overview-cards { margin-bottom: 16px; }
  .overview-card {
    text-align: center;
    .card-value { font-size: 24px; font-weight: bold; color: #409EFF; }
    .card-label { font-size: 13px; color: #999; margin-top: 4px; }
  }
  .charts-row { margin-top: 16px; }
  .chart { height: 320px; }
}
</style>
