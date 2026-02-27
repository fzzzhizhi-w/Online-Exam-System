<template>
  <div class="monitor-view">
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>实时监考中心</span>
          <el-button type="primary" @click="loadData">刷新</el-button>
        </div>
      </template>

      <el-alert type="info" :closable="false" class="mb-16">
        <template #title>
          监考员可实时查看考生答题状态，异常行为将自动预警，支持强制收卷
        </template>
      </el-alert>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="userId" label="考生ID" width="90" />
        <el-table-column prop="switchCount" label="切屏次数" width="100">
          <template #default="{ row }">
            <el-tag :type="row.switchCount > 2 ? 'danger' : 'success'" size="small">{{ row.switchCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'primary' : 'success'" size="small">
              {{ row.status === 0 ? '答题中' : '已交卷' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" size="small" text type="danger"
              @click="handleForceSubmit(row)">强制收卷</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageRecords, forceSubmit } from '@/api/exam'

const route = useRoute()
const examId = route.params.examId
const loading = ref(false)
const tableData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageRecords({ examId, pageNum: 1, pageSize: 100 })
    tableData.value = res.data?.records || []
  } finally { loading.value = false }
}

const handleForceSubmit = async (row) => {
  await ElMessageBox.confirm(`确定强制收卷 考生ID=${row.userId} 的答卷？`, '警告', { type: 'warning' })
  await forceSubmit(row.id)
  ElMessage.success('已强制收卷')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.mb-16 { margin-bottom: 16px; }
</style>
