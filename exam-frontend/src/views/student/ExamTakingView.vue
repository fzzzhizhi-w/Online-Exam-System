<template>
  <div class="exam-taking" @contextmenu.prevent @copy.prevent @paste.prevent>
    <!-- 顶部信息栏 -->
    <div class="exam-header">
      <div class="exam-title">{{ examInfo?.name }}</div>
      <div class="exam-timer" :class="{ 'warning': remainingSeconds <= 300 }">
        <el-icon><Timer /></el-icon>
        剩余时间：{{ formattedTime }}
      </div>
      <div class="exam-actions">
        <el-button type="danger" size="large" @click="handleSubmit">交 卷</el-button>
      </div>
    </div>

    <!-- 动态水印 -->
    <div class="watermark" aria-hidden="true">
      {{ watermarkText }}
    </div>

    <div class="exam-body" v-if="paperContent">
      <!-- 题目导航区 -->
      <div class="question-nav">
        <div class="nav-title">答题卡</div>
        <div class="nav-grid">
          <div
            v-for="(question, idx) in allQuestions"
            :key="question.id"
            class="nav-item"
            :class="{
              answered: answers[question.id],
              flagged: flaggedItems.has(question.id),
              current: currentIndex === idx,
            }"
            @click="scrollToQuestion(idx)"
          >
            {{ idx + 1 }}
          </div>
        </div>
        <div class="nav-legend">
          <span class="dot answered"></span>已答
          <span class="dot"></span>未答
          <span class="dot flagged"></span>已标记
        </div>
      </div>

      <!-- 题目作答区 -->
      <div class="question-content" ref="contentRef">
        <div
          v-for="(question, idx) in allQuestions"
          :key="question.id"
          class="question-item"
          :data-idx="idx"
          :ref="el => questionRefs[idx] = el"
        >
          <div class="question-header">
            <span class="q-num">{{ idx + 1 }}.（{{ question.score }}分）</span>
            <el-tag size="small">{{ getTypeName(question.type) }}</el-tag>
            <el-button
              text
              :type="flaggedItems.has(question.id) ? 'warning' : ''"
              size="small"
              @click="toggleFlag(question.id)"
            >
              {{ flaggedItems.has(question.id) ? '取消标记' : '标记' }}
            </el-button>
          </div>

          <!-- 题目内容 -->
          <div class="q-content" v-html="question.content" />

          <!-- 单选题 -->
          <template v-if="question.type === 1">
            <el-radio-group v-model="answers[question.id]" class="options-group" @change="onAnswerChange">
              <el-radio
                v-for="opt in parseOptions(question.options)"
                :key="opt.label"
                :label="opt.label"
                class="option-item"
              >
                {{ opt.label }}. {{ opt.content }}
              </el-radio>
            </el-radio-group>
          </template>

          <!-- 多选题 -->
          <template v-else-if="question.type === 2">
            <el-checkbox-group v-model="multiAnswers[question.id]" class="options-group" @change="onMultiAnswerChange(question.id)">
              <el-checkbox
                v-for="opt in parseOptions(question.options)"
                :key="opt.label"
                :label="opt.label"
                class="option-item"
              >
                {{ opt.label }}. {{ opt.content }}
              </el-checkbox>
            </el-checkbox-group>
          </template>

          <!-- 判断题 -->
          <template v-else-if="question.type === 3">
            <el-radio-group v-model="answers[question.id]" class="options-group" @change="onAnswerChange">
              <el-radio label="T" class="option-item">正确</el-radio>
              <el-radio label="F" class="option-item">错误</el-radio>
            </el-radio-group>
          </template>

          <!-- 填空题/简答题/案例 -->
          <template v-else>
            <el-input
              v-model="answers[question.id]"
              type="textarea"
              :rows="question.type >= 5 ? 8 : 2"
              :placeholder="question.type === 4 ? '请填写答案' : '请输入作答内容'"
              @input="onAnswerChange"
            />
          </template>
        </div>
      </div>
    </div>

    <!-- 交卷确认弹窗 -->
    <el-dialog v-model="submitDialogVisible" title="确认交卷" width="400px">
      <div class="submit-info">
        <p>总题数：<strong>{{ allQuestions.length }}</strong></p>
        <p>已答题：<strong>{{ answeredCount }}</strong></p>
        <p>未答题：<strong class="danger">{{ allQuestions.length - answeredCount }}</strong></p>
        <el-alert v-if="allQuestions.length > answeredCount" type="warning"
          :title="`还有 ${allQuestions.length - answeredCount} 道题未作答，确定交卷？`"
          :closable="false" />
      </div>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="confirmSubmit">确认交卷</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { enterExam, saveAnswerProgress, submitExam, reportSwitchScreen } from '@/api/exam'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const examId = computed(() => Number(route.params.examId))

const examInfo = ref(null)
const paperContent = ref(null)
const recordId = ref(null)
const allQuestions = ref([])
const answers = reactive({})
const multiAnswers = reactive({})
const flaggedItems = reactive(new Set())
const currentIndex = ref(0)
const questionRefs = ref([])
const contentRef = ref()
const submitDialogVisible = ref(false)
const submitting = ref(false)
const remainingSeconds = ref(0)
let timer = null
let autoSaveTimer = null

// 水印文字
const watermarkText = computed(() => {
  const user = authStore.userInfo
  return user ? `${user.realName} ${user.jobNumber || ''}` : ''
})

// 已答题数
const answeredCount = computed(() => {
  return allQuestions.value.filter(q => answers[q.id]).length
})

// 格式化剩余时间
const formattedTime = computed(() => {
  const h = Math.floor(remainingSeconds.value / 3600)
  const m = Math.floor((remainingSeconds.value % 3600) / 60)
  const s = remainingSeconds.value % 60
  return `${h > 0 ? h + ':' : ''}${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

// 进入考试
onMounted(async () => {
  try {
    const res = await enterExam(examId.value)
    examInfo.value = res.data.examInfo
    recordId.value = res.data.recordId

    // 恢复保存的答题进度
    if (res.data.savedAnswers) {
      const savedAnswers = res.data.savedAnswers
      for (const [qId, ans] of Object.entries(savedAnswers)) {
        answers[qId] = ans
      }
    }

    // 解析试卷内容
    if (res.data.paperContent) {
      const content = res.data.paperContent
      if (Array.isArray(content)) {
        // 后端返回的已展开题目列表
        allQuestions.value = content
        paperContent.value = content
      } else {
        try {
          paperContent.value = JSON.parse(content)
          allQuestions.value = paperContent.value?.questions || []
        } catch (e) {
          allQuestions.value = []
        }
      }
    }

    // 初始化多选题答案
    allQuestions.value.forEach(q => {
      if (q.type === 2) {
        multiAnswers[q.id] = answers[q.id] ? answers[q.id].split('') : []
      }
    })

    // 计算剩余时间
    const duration = (examInfo.value?.duration || 60) * 60
    const start = new Date(res.data.startTime).getTime()
    const now = Date.now()
    const elapsed = Math.floor((now - start) / 1000)
    remainingSeconds.value = Math.max(0, duration - elapsed)

    // 开始倒计时
    startTimer()

    // 开始自动保存（每30秒）
    startAutoSave()

    // 监听切屏
    document.addEventListener('visibilitychange', handleVisibilityChange)
  } catch (e) {
    ElMessage.error('进入考试失败，请重试')
  }
})

onUnmounted(() => {
  clearInterval(timer)
  clearInterval(autoSaveTimer)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})

const startTimer = () => {
  timer = setInterval(() => {
    if (remainingSeconds.value <= 0) {
      clearInterval(timer)
      ElMessage.warning('考试时间已到，系统自动交卷')
      confirmSubmit()
      return
    }
    remainingSeconds.value--
  }, 1000)
}

const startAutoSave = () => {
  autoSaveTimer = setInterval(() => {
    saveProgress()
  }, 30000)
}

const saveProgress = async () => {
  if (!recordId.value) return
  try {
    await saveAnswerProgress({ recordId: recordId.value, answers: { ...answers } })
  } catch (e) {}
}

const onAnswerChange = () => {
  // 答案变更时立即保存
  saveProgress()
}

const onMultiAnswerChange = (questionId) => {
  answers[questionId] = (multiAnswers[questionId] || []).sort().join('')
  saveProgress()
}

const handleVisibilityChange = () => {
  if (document.hidden && recordId.value) {
    reportSwitchScreen(recordId.value)
  }
}

const toggleFlag = (questionId) => {
  if (flaggedItems.has(questionId)) {
    flaggedItems.delete(questionId)
  } else {
    flaggedItems.add(questionId)
  }
}

const scrollToQuestion = (idx) => {
  currentIndex.value = idx
  questionRefs.value[idx]?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const handleSubmit = () => {
  submitDialogVisible.value = true
}

const confirmSubmit = async () => {
  if (!recordId.value) return
  submitting.value = true
  try {
    await submitExam({ recordId: recordId.value, finalSubmit: true })
    ElMessage.success('交卷成功！')
    router.push(`/exam/report/${recordId.value}`)
  } catch (e) {
    ElMessage.error('交卷失败，请重试')
  } finally {
    submitting.value = false
    submitDialogVisible.value = false
  }
}

const parseOptions = (optionsStr) => {
  try {
    return JSON.parse(optionsStr) || []
  } catch (e) {
    return []
  }
}

const getTypeName = (type) => {
  const map = { 1: '单选', 2: '多选', 3: '判断', 4: '填空', 5: '简答', 6: '案例' }
  return map[type] || '未知'
}
</script>

<style scoped lang="scss">
.exam-taking {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
  position: relative;
  user-select: none;
}

/* 水印 */
.watermark {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 999;
  opacity: 0.06;
  font-size: 20px;
  color: #000;
  transform: rotate(-30deg);
  display: flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
  font-weight: bold;
  background-image: repeating-linear-gradient(
    -45deg,
    transparent,
    transparent 100px,
    rgba(0,0,0,0.02) 100px,
    rgba(0,0,0,0.02) 200px
  );
}

.exam-header {
  height: 60px;
  background: #001529;
  color: #fff;
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 16px;
  flex-shrink: 0;

  .exam-title {
    flex: 1;
    font-size: 18px;
    font-weight: bold;
  }

  .exam-timer {
    font-size: 18px;
    font-family: monospace;
    color: #52c41a;
    display: flex;
    align-items: center;
    gap: 6px;

    &.warning {
      color: #ff4d4f;
      animation: blink 1s infinite;
    }
  }
}

@keyframes blink {
  50% { opacity: 0.5; }
}

.exam-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.question-nav {
  width: 200px;
  flex-shrink: 0;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  padding: 16px;
  overflow-y: auto;

  .nav-title {
    font-weight: bold;
    margin-bottom: 12px;
    font-size: 14px;
  }

  .nav-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 6px;

    .nav-item {
      height: 28px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 4px;
      border: 1px solid #d9d9d9;
      font-size: 12px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover { border-color: #409eff; }
      &.answered { background: #52c41a; color: #fff; border-color: #52c41a; }
      &.flagged { background: #fa8c16; color: #fff; border-color: #fa8c16; }
      &.current { border: 2px solid #409eff; }
    }
  }

  .nav-legend {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-top: 12px;
    font-size: 12px;
    color: #666;
    flex-wrap: wrap;

    .dot {
      width: 12px;
      height: 12px;
      border-radius: 2px;
      border: 1px solid #d9d9d9;
      display: inline-block;

      &.answered { background: #52c41a; }
      &.flagged { background: #fa8c16; }
    }
  }
}

.question-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;

  .question-item {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 16px;

    .question-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;

      .q-num {
        font-weight: bold;
        font-size: 15px;
      }
    }

    .q-content {
      font-size: 15px;
      line-height: 1.8;
      margin-bottom: 16px;
    }

    .options-group {
      display: flex;
      flex-direction: column;
      gap: 10px;

      .option-item {
        height: auto;
        line-height: 1.6;
      }
    }
  }
}

.submit-info {
  p {
    font-size: 15px;
    margin-bottom: 8px;
  }
  .danger { color: #f56c6c; }
}
</style>
