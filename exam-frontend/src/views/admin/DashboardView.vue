<template>
  <div class="dashboard">
    <!-- 统计概览卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6" v-for="card in statCards" :key="card.key">
        <div class="stat-card hover-float" :style="{ '--card-color': card.color, '--card-color-end': card.colorEnd }">
          <div class="stat-card-inner">
            <div class="stat-info">
              <div class="stat-value">
                <span class="count-animate" :key="card.value">{{ card.value }}</span>
              </div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
            <div class="stat-icon-wrap">
              <el-icon size="30" color="#fff">
                <component :is="card.icon" />
              </el-icon>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 近期考试 & 公告 -->
    <el-row :gutter="16" class="content-row">
      <el-col :span="16">
        <el-card shadow="never" class="content-card">
          <template #header>
            <div class="card-header">
              <div class="flex-center gap-8">
                <el-icon color="var(--primary-color)"><Calendar /></el-icon>
                <span class="section-title">近期考试安排</span>
              </div>
              <el-button type="primary" link size="small" @click="$router.push('/exam/list')">
                查看全部 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>

          <div v-if="recentExams.length === 0" class="empty-tip">
            <el-empty description="暂无近期考试" :image-size="80" />
          </div>
          <div v-else class="exam-list">
            <div
              v-for="exam in recentExams"
              :key="exam.id"
              class="exam-item"
              :class="{ 'exam-active': exam.status === 2, 'exam-ended': exam.status === 3 || exam.status === 5 }"
            >
              <div class="exam-item-left">
                <div class="exam-name">{{ exam.name }}</div>
                <div class="exam-time">
                  <el-icon size="12"><Clock /></el-icon>
                  {{ formatDateTime(exam.startTime) }} — {{ formatDateTime(exam.endTime) }}
                </div>
              </div>
              <div class="exam-item-right">
                <span class="status-tag" :class="getExamStatusClass(exam.status)">
                  <span v-if="exam.status === 2" class="pulse-dot"></span>
                  {{ getExamStatusLabel(exam.status) }}
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="content-card">
          <template #header>
            <div class="card-header">
              <div class="flex-center gap-8">
                <el-icon color="var(--warning-color)"><Bell /></el-icon>
                <span class="section-title">系统公告</span>
              </div>
            </div>
          </template>
          <div class="notice-list">
            <div v-for="notice in notices" :key="notice.id" class="notice-item">
              <div class="notice-dot"></div>
              <div class="notice-body">
                <div class="notice-text">{{ notice.content }}</div>
                <div class="notice-time">{{ notice.time }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { pageExams } from '@/api/exam'
import { getDashboard } from '@/api/stats'
import { formatDateTime } from '@/utils/format'

const statCards = ref([
  { key: 'userCount',     label: '系统用户数', value: '--', icon: 'User',    color: '#165DFF', colorEnd: '#4080FF' },
  { key: 'questionCount', label: '题库题目数', value: '--', icon: 'EditPen', color: '#00B42A', colorEnd: '#23D057' },
  { key: 'examCount',     label: '考试场次',   value: '--', icon: 'Calendar',color: '#FF7D00', colorEnd: '#FFAA00' },
  { key: 'recordCount',   label: '答卷总数',   value: '--', icon: 'Document',color: '#722ED1', colorEnd: '#9B59B6' },
])

const recentExams = ref([])
const notices = ref([
  { id: 1, content: '欢迎使用捷评智航考试系统！', time: '今天' },
  { id: 2, content: '系统已升级到最新版本，体验更流畅', time: '2天前' },
])

const getExamStatusClass = (status) => {
  const map = { 0: 'tag-info', 1: 'tag-primary', 2: 'tag-warning', 3: 'tag-info', 4: 'tag-primary', 5: 'tag-success' }
  return map[status] || 'tag-info'
}

const getExamStatusLabel = (status) => {
  const map = { 0: '草稿', 1: '已发布', 2: '进行中', 3: '已结束', 4: '评卷中', 5: '已完成' }
  return map[status] || '未知'
}

onMounted(async () => {
  try {
    const [dashRes, examRes] = await Promise.all([
      getDashboard(),
      pageExams({ pageNum: 1, pageSize: 5 }),
    ])
    const stats = dashRes.data || {}
    statCards.value.forEach((card) => {
      if (stats[card.key] !== undefined) {
        card.value = stats[card.key]
      }
    })
    recentExams.value = examRes.data?.records || []
  } catch (e) {}
})
</script>

<style scoped lang="scss">
.dashboard {
  .stat-cards {
    margin-bottom: 20px;
  }

  /* 统计卡片 */
  .stat-card {
    border-radius: var(--radius-lg);
    background: linear-gradient(135deg, var(--card-color) 0%, var(--card-color-end) 100%);
    padding: 20px;
    cursor: default;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);

    .stat-card-inner {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .stat-info {
        .stat-value {
          font-size: 32px;
          font-weight: 700;
          color: #fff;
          line-height: 1.2;
          letter-spacing: -0.5px;
        }
        .stat-label {
          font-size: 13px;
          color: rgba(255, 255, 255, 0.8);
          margin-top: 6px;
        }
      }

      .stat-icon-wrap {
        width: 56px;
        height: 56px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.2);
        display: flex;
        align-items: center;
        justify-content: center;
        backdrop-filter: blur(4px);
      }
    }
  }

  /* 内容区卡片 */
  .content-row {
    margin-top: 0;
  }

  .content-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  /* 考试列表 */
  .exam-list {
    display: flex;
    flex-direction: column;
    gap: 0;
  }

  .exam-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid var(--border-light);
    transition: background 0.2s ease;
    border-radius: var(--radius-md);
    padding-left: 8px;
    padding-right: 8px;

    &:last-child {
      border-bottom: none;
    }

    &.exam-active {
      background: var(--warning-light);
      border-left: 3px solid var(--warning-color);
      padding-left: 10px;
      border-bottom-color: transparent;
      margin-bottom: 4px;
    }

    &.exam-ended {
      opacity: 0.6;
    }

    .exam-item-left {
      flex: 1;
      .exam-name {
        font-size: 14px;
        font-weight: 500;
        color: var(--text-primary);
        margin-bottom: 4px;
      }
      .exam-time {
        font-size: 12px;
        color: var(--text-tertiary);
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }

    .exam-item-right {
      flex-shrink: 0;
      margin-left: 12px;
    }
  }

  /* 公告列表 */
  .notice-list {
    .notice-item {
      display: flex;
      gap: 10px;
      padding: 10px 0;
      border-bottom: 1px dashed var(--border-light);

      &:last-child {
        border-bottom: none;
      }

      .notice-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: var(--primary-color);
        flex-shrink: 0;
        margin-top: 5px;
      }

      .notice-body {
        flex: 1;
        .notice-text {
          font-size: 13px;
          color: var(--text-secondary);
          line-height: 1.5;
        }
        .notice-time {
          font-size: 11px;
          color: var(--text-tertiary);
          margin-top: 3px;
        }
      }
    }
  }

  .empty-tip {
    padding: 20px 0;
  }
}
</style>
