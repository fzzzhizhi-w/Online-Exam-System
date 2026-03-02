<template>
  <div class="student-exam-list">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>我的考试</span>
          <el-input
            v-model="keyword"
            placeholder="搜索考试名称"
            clearable
            style="width: 220px"
            @change="loadExams"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
      </template>

      <el-table :data="exams" v-loading="loading" stripe>
        <el-table-column prop="name" label="考试名称" min-width="180" />
        <el-table-column label="开始时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="duration" label="时长(分钟)" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1 || row.status === 2"
              type="primary"
              size="small"
              @click="enterExam(row)"
            >进入考试</el-button>
            <el-tag v-else-if="row.status === 3 || row.status === 4 || row.status === 5" type="info" size="small">已结束</el-tag>
            <el-tag v-else type="info" size="small">未开始</el-tag>
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
          @change="loadExams"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { pageExams } from '@/api/exam'
import { formatDateTime } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const exams = ref([])
const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'primary', 2: 'success', 3: '', 4: 'warning', 5: '' }
  return map[status] ?? 'info'
}
const getStatusLabel = (status) => {
  const map = { 0: '草稿', 1: '已发布', 2: '进行中', 3: '已结束', 4: '评卷中', 5: '已完成' }
  return map[status] ?? '未知'
}

const loadExams = async () => {
  loading.value = true
  try {
    const res = await pageExams({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      status: undefined,
    })
    const data = res.data || {}
    exams.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载考试列表失败')
  } finally {
    loading.value = false
  }
}

const enterExam = (row) => {
  router.push(`/exam/take/${row.id}`)
}

onMounted(loadExams)
</script>

<style scoped lang="scss">
.student-exam-list {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
