<template>
  <div class="student-record-list">
    <!-- 顶部标题 -->
    <div class="page-header mb-16">
      <div class="flex-center gap-8">
        <el-icon color="var(--primary-color)" size="20"><TrophyBase /></el-icon>
        <span class="page-title">我的成绩</span>
      </div>
    </div>

    <!-- 成绩记录列表 -->
    <div v-if="loading" class="record-cards">
      <el-card v-for="i in 3" :key="i" shadow="never">
        <el-skeleton :rows="3" animated />
      </el-card>
    </div>

    <div v-else-if="records.length > 0" class="record-cards">
      <div v-for="record in records" :key="record.id" class="record-card hover-float">
        <!-- 顶部：考试名 + 结果 -->
        <div class="record-top">
          <div class="record-exam-info">
            <div class="record-exam-id">考试 #{{ record.examId }}</div>
            <div class="record-times">
              <span><el-icon size="12"><Timer /></el-icon> 开始：{{ formatDateTime(record.startTime) }}</span>
              <span><el-icon size="12"><Finished /></el-icon> 交卷：{{ formatDateTime(record.submitTime) }}</span>
            </div>
          </div>
          <!-- 成绩主显 -->
          <div class="record-score" :class="record.passed ? 'score-pass' : 'score-fail'">
            <div class="score-number">
              {{ record.totalScore != null ? record.totalScore : '--' }}
            </div>
            <div class="score-label">分</div>
          </div>
        </div>

        <!-- 中部：用时 + 结果标签 -->
        <div class="record-body">
          <div class="meta-item">
            <el-icon size="13"><Clock /></el-icon>
            <span>用时：{{ formatDuration(record.usedTime) }}</span>
          </div>
          <div class="meta-item">
            <span v-if="record.status === 3">
              <span class="status-tag" :class="record.passed ? 'tag-success' : 'tag-danger'">
                {{ record.passed ? '✓ 通过' : '✗ 未通过' }}
              </span>
            </span>
            <span v-else-if="record.status === 0">
              <span class="status-tag tag-warning">
                <span class="pulse-dot"></span>考试中
              </span>
            </span>
            <span v-else>
              <span class="status-tag tag-primary">评卷中</span>
            </span>
          </div>
        </div>

        <!-- 底部：操作 -->
        <div class="record-footer">
          <el-button
            v-if="record.status === 3"
            type="primary"
            size="small"
            @click="viewReport(record)"
          >查看报告</el-button>
          <el-button v-else size="small" disabled>
            {{ record.status === 0 ? '考试中' : '评卷中，请耐心等待' }}
          </el-button>
        </div>
      </div>
    </div>

    <el-card v-else shadow="never">
      <el-empty description="暂无成绩记录" :image-size="120" />
    </el-card>

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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { pageRecords } from '@/api/exam'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime, formatDuration } from '@/utils/format'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const records = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

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
  .page-header {
    padding: 4px 0;
  }

  .record-cards {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: 20px;
  }

  .record-card {
    background: var(--bg-primary);
    border-radius: var(--radius-lg);
    padding: 20px;
    box-shadow: var(--shadow-sm);
    border: 1px solid var(--border-color);

    .record-top {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 12px;
      padding-bottom: 12px;
      border-bottom: 1px solid var(--border-light);

      .record-exam-info {
        flex: 1;

        .record-exam-id {
          font-size: 15px;
          font-weight: 600;
          color: var(--text-primary);
          margin-bottom: 6px;
        }

        .record-times {
          display: flex;
          flex-direction: column;
          gap: 3px;
          font-size: 12px;
          color: var(--text-tertiary);

          span {
            display: flex;
            align-items: center;
            gap: 4px;
          }
        }
      }

      .record-score {
        text-align: center;
        min-width: 64px;

        .score-number {
          font-size: 32px;
          font-weight: 700;
          line-height: 1;
        }

        .score-label {
          font-size: 12px;
          margin-top: 2px;
        }

        &.score-pass {
          .score-number { color: var(--success-color); }
          .score-label  { color: var(--success-color); }
        }

        &.score-fail {
          .score-number { color: var(--danger-color); }
          .score-label  { color: var(--danger-color); }
        }
      }
    }

    .record-body {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 12px;

      .meta-item {
        display: flex;
        align-items: center;
        gap: 5px;
        font-size: 13px;
        color: var(--text-secondary);
      }
    }

    .record-footer {
      display: flex;
      justify-content: flex-end;
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
