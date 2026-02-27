<template>
  <div class="dashboard">
    <!-- 统计概览卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6" v-for="card in statCards" :key="card.key">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-inner">
            <div class="stat-info">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
            <div class="stat-icon" :style="{ background: card.color }">
              <el-icon size="28" color="#fff">
                <component :is="card.icon" />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 近期考试 & 公告 -->
    <el-row :gutter="16" class="content-row">
      <el-col :span="16">
        <el-card header="近期考试安排" shadow="hover">
          <el-table :data="recentExams" size="small">
            <el-table-column prop="name" label="考试名称" />
            <el-table-column prop="startTime" label="开始时间" width="160" />
            <el-table-column prop="endTime" label="结束时间" width="160" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="getExamStatusType(row.status)" size="small">
                  {{ getExamStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card header="系统公告" shadow="hover">
          <div class="notice-list">
            <div v-for="notice in notices" :key="notice.id" class="notice-item">
              <el-tag size="small" type="info">公告</el-tag>
              <span class="notice-text">{{ notice.content }}</span>
              <span class="notice-time">{{ notice.time }}</span>
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

const statCards = ref([
  { key: 'users', label: '系统用户数', value: '---', icon: 'User', color: '#409EFF' },
  { key: 'questions', label: '题库题目数', value: '---', icon: 'EditPen', color: '#67C23A' },
  { key: 'exams', label: '考试场次', value: '---', icon: 'Calendar', color: '#E6A23C' },
  { key: 'records', label: '答卷总数', value: '---', icon: 'Document', color: '#F56C6C' },
])

const recentExams = ref([])
const notices = ref([
  { id: 1, content: '欢迎使用捷评智航考试系统！', time: '今天' },
  { id: 2, content: '系统已升级到最新版本', time: '2天前' },
])

const getExamStatusType = (status) => {
  const map = { 0: 'info', 1: 'primary', 2: 'success', 3: 'danger', 4: 'warning', 5: '' }
  return map[status] || 'info'
}

const getExamStatusLabel = (status) => {
  const map = { 0: '草稿', 1: '已发布', 2: '进行中', 3: '已结束', 4: '评卷中', 5: '已完成' }
  return map[status] || '未知'
}

onMounted(async () => {
  try {
    const res = await pageExams({ pageNum: 1, pageSize: 5 })
    recentExams.value = res.data?.records || []
  } catch (e) {}
})
</script>

<style scoped lang="scss">
.dashboard {
  .stat-cards {
    margin-bottom: 16px;
  }

  .stat-card {
    .stat-card-inner {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .stat-info {
        .stat-value {
          font-size: 28px;
          font-weight: 700;
          color: #333;
        }
        .stat-label {
          font-size: 13px;
          color: #999;
          margin-top: 4px;
        }
      }

      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }

  .content-row {
    margin-top: 16px;
  }

  .notice-list {
    .notice-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 10px 0;
      border-bottom: 1px dashed #eee;

      &:last-child {
        border-bottom: none;
      }

      .notice-text {
        flex: 1;
        font-size: 13px;
        color: #333;
      }

      .notice-time {
        font-size: 12px;
        color: #999;
      }
    }
  }
}
</style>
