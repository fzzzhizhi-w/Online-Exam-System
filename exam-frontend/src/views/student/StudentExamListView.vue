<template>
  <div class="student-exam-list">
    <!-- 顶部搜索 -->
    <el-card shadow="never" class="search-card">
      <div class="flex-between">
        <div class="flex-center gap-8">
          <el-icon color="var(--primary-color)" size="18"><Calendar /></el-icon>
          <span class="page-title">我的考试</span>
        </div>
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
    </el-card>

    <!-- 加载骨架 -->
    <div v-if="loading" class="exam-cards">
      <el-card v-for="i in 4" :key="i" shadow="never" class="exam-card skeleton-card">
        <el-skeleton :rows="3" animated />
      </el-card>
    </div>

    <!-- 卡片列表 -->
    <div v-else-if="exams.length > 0" class="exam-cards">
      <div
        v-for="exam in exams"
        :key="exam.id"
        class="exam-card hover-float"
        :class="{ 'card-active': exam.status === 2, 'card-ended': exam.status === 3 || exam.status === 5 }"
      >
        <!-- 卡片顶部 -->
        <div class="card-top">
          <div class="exam-name">{{ exam.name }}</div>
          <span class="status-tag" :class="getStatusClass(exam.status)">
            <span v-if="exam.status === 2" class="pulse-dot"></span>
            {{ getStatusLabel(exam.status) }}
          </span>
        </div>

        <!-- 卡片中部 -->
        <div class="card-body">
          <div class="info-item">
            <el-icon size="13"><Timer /></el-icon>
            <span>{{ formatDateTime(exam.startTime) }} — {{ formatDateTime(exam.endTime) }}</span>
          </div>
          <div class="info-item" v-if="exam.duration">
            <el-icon size="13"><Clock /></el-icon>
            <span>考试时长：{{ exam.duration }} 分钟</span>
          </div>
          <!-- 倒计时提示 -->
          <div class="countdown-tip" v-if="exam.status === 1">
            <el-icon size="13" color="var(--warning-color)"><AlarmClock /></el-icon>
            <span style="color: var(--warning-color)">考试即将开始</span>
          </div>
          <div class="countdown-tip" v-else-if="exam.status === 2">
            <el-icon size="13" color="var(--danger-color)"><AlarmClock /></el-icon>
            <span style="color: var(--danger-color)">考试进行中</span>
          </div>
        </div>

        <!-- 卡片底部 -->
        <div class="card-footer">
          <el-button
            v-if="exam.status === 1 || exam.status === 2"
            type="primary"
            size="small"
            @click="enterExam(exam)"
          >进入考试</el-button>
          <el-button v-else size="small" disabled>
            {{ exam.status === 3 || exam.status === 5 ? '已结束' : '未开始' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-card v-else shadow="never">
      <el-empty description="暂无考试安排" :image-size="120" />
    </el-card>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="loadExams"
      />
    </div>
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

const getStatusClass = (status) => {
  const map = { 0: 'tag-info', 1: 'tag-primary', 2: 'tag-warning', 3: 'tag-info', 4: 'tag-primary', 5: 'tag-success' }
  return map[status] ?? 'tag-info'
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
  .search-card {
    margin-bottom: 20px;
  }

  .exam-cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 16px;
    margin-bottom: 20px;
  }

  .exam-card {
    background: var(--bg-primary);
    border-radius: var(--radius-lg);
    padding: 20px;
    box-shadow: var(--shadow-sm);
    border: 1px solid var(--border-color);
    cursor: default;
    transition: transform 0.3s ease, box-shadow 0.3s ease;

    &.card-active {
      border-left: 4px solid var(--warning-color);
      background: linear-gradient(135deg, #FFFBE8 0%, #FFF 60%);
    }

    &.card-ended {
      opacity: 0.7;
    }

    .card-top {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 12px;
      gap: 8px;

      .exam-name {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
        line-height: 1.4;
        flex: 1;
      }
    }

    .card-body {
      display: flex;
      flex-direction: column;
      gap: 6px;
      margin-bottom: 16px;
      padding-bottom: 16px;
      border-bottom: 1px solid var(--border-light);

      .info-item {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: var(--text-secondary);
      }

      .countdown-tip {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        font-weight: 500;
        margin-top: 2px;
      }
    }

    .card-footer {
      display: flex;
      justify-content: flex-end;
    }
  }

  .skeleton-card {
    min-height: 150px;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
