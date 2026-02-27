<template>
  <div class="grade-tasks">
    <el-card shadow="never">
      <template #header>
        <span>评卷任务列表</span>
      </template>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="任务ID" width="80" />
        <el-table-column prop="recordId" label="答卷ID" width="100" />
        <el-table-column prop="questionId" label="题目ID" width="100" />
        <el-table-column prop="aiPreScore" label="AI预评分" width="100" />
        <el-table-column prop="score1" label="评分1" width="90" />
        <el-table-column prop="score2" label="评分2" width="90" />
        <el-table-column prop="finalScore" label="最终得分" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" size="small" text type="primary" @click="handleGrade(row)">评分</el-button>
            <el-button v-if="row.status === 2" size="small" text type="warning" @click="handleArbitrate(row)">仲裁</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        :total="total"
        layout="total, prev, pager, next"
        class="mt-16"
        @current-change="loadData"
      />
    </el-card>

    <!-- 评分弹窗 -->
    <el-dialog v-model="gradeDialogVisible" title="人工评分" width="480px">
      <el-form label-width="90px">
        <el-form-item label="AI预评分">
          <el-tag>{{ currentTask?.aiPreScore || '--' }}</el-tag>
          <span class="hint ml-8">供参考</span>
        </el-form-item>
        <el-form-item label="得分">
          <el-input-number v-model="gradeScore" :min="0" :max="100" :step="0.5" />
        </el-form-item>
        <el-form-item label="评语">
          <el-input v-model="gradeComment" type="textarea" :rows="3" placeholder="输入评语（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="gradeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGrade">确认评分</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getGradingTasks, manualGrade, arbitrateGrade } from '@/api/grade'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const gradeDialogVisible = ref(false)
const currentTask = ref(null)
const gradeScore = ref(0)
const gradeComment = ref('')
const isArbitrate = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getGradingTasks({ pageNum: pageNum.value, pageSize: 10 })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleGrade = (row) => {
  currentTask.value = row
  gradeScore.value = row.aiPreScore || 0
  gradeComment.value = ''
  isArbitrate.value = false
  gradeDialogVisible.value = true
}

const handleArbitrate = (row) => {
  currentTask.value = row
  gradeScore.value = Math.round((row.score1 + row.score2) / 2 * 10) / 10
  gradeComment.value = ''
  isArbitrate.value = true
  gradeDialogVisible.value = true
}

const submitGrade = async () => {
  const id = currentTask.value.id
  if (isArbitrate.value) {
    await arbitrateGrade(id, { score: gradeScore.value })
  } else {
    await manualGrade(id, { score: gradeScore.value, comment: gradeComment.value })
  }
  ElMessage.success('评分成功')
  gradeDialogVisible.value = false
  loadData()
}

const getStatusType = (s) => ({ 0: 'info', 1: 'success', 2: 'danger', 3: 'warning' }[s] || 'info')
const getStatusName = (s) => ({ 0: '待评', 1: '已评', 2: '待仲裁', 3: '已仲裁' }[s] || s)

onMounted(loadData)
</script>

<style scoped>
.mt-16 { margin-top: 16px; text-align: right; }
.hint { color: #999; font-size: 12px; }
.ml-8 { margin-left: 8px; }
</style>
