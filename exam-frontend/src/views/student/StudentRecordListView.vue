<template>
  <div class="student-record-list">
    <el-card shadow="never">
      <template #header>
        <span>我的成绩</span>
      </template>

      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="examId" label="考试ID" width="80" align="center" />
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="submitTime" label="交卷时间" width="160" />
        <el-table-column label="用时" width="100" align="center">
          <template #default="{ row }">{{ formatTime(row.usedTime) }}</template>
        </el-table-column>
        <el-table-column label="总分" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.totalScore != null">{{ row.totalScore }}</span>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column label="结果" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 3" :type="row.passed ? 'success' : 'danger'" size="small">
              {{ row.passed ? '通过' : '未通过' }}
            </el-tag>
            <el-tag v-else-if="row.status === 0" type="warning" size="small">考试中</el-tag>
            <el-tag v-else type="info" size="small">评卷中</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 3"
              type="primary"
              size="small"
              link
              @click="viewReport(row)"
            >查看报告</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadRecords"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { pageRecords } from '@/api/exam'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const records = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formatTime = (seconds) => {
  if (!seconds) return '--'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}分${s}秒`
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await pageRecords({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      userId: authStore.userInfo?.id,
    })
    const data = res.data || {}
    records.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载成绩记录失败')
  } finally {
    loading.value = false
  }
}

const viewReport = (row) => {
  router.push(`/exam/report/${row.id}`)
}

onMounted(loadRecords)
</script>

<style scoped lang="scss">
.student-record-list {
  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
