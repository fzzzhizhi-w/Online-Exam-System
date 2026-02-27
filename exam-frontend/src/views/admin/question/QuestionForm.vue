<template>
  <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
    <el-row :gutter="16">
      <el-col :span="12">
        <el-form-item label="题型" prop="type">
          <el-select v-model="formData.type" placeholder="选择题型" style="width:100%">
            <el-option label="单选题" :value="1" />
            <el-option label="多选题" :value="2" />
            <el-option label="判断题" :value="3" />
            <el-option label="填空题" :value="4" />
            <el-option label="简答题" :value="5" />
            <el-option label="综合案例题" :value="6" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="formData.difficulty" placeholder="选择难度" style="width:100%">
            <el-option label="容易" :value="1" />
            <el-option label="较易" :value="2" />
            <el-option label="中等" :value="3" />
            <el-option label="较难" :value="4" />
            <el-option label="困难" :value="5" />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>

    <el-form-item label="题目内容" prop="content">
      <el-input
        v-model="formData.content"
        type="textarea"
        :rows="4"
        placeholder="输入题目内容（支持HTML富文本）"
      />
    </el-form-item>

    <!-- 客观题选项 -->
    <template v-if="formData.type && formData.type <= 3">
      <el-form-item label="选项" prop="options">
        <div class="options-editor">
          <div v-for="(opt, idx) in options" :key="idx" class="option-item">
            <span class="opt-label">{{ String.fromCharCode(65 + idx) }}.</span>
            <el-input v-model="opt.content" :placeholder="`选项${String.fromCharCode(65 + idx)}`" />
            <el-button icon="Delete" text type="danger" @click="removeOption(idx)" />
          </div>
          <el-button v-if="formData.type <= 2" icon="Plus" text @click="addOption">添加选项</el-button>
        </div>
      </el-form-item>
    </template>

    <el-form-item label="正确答案" prop="answer">
      <!-- 单选题/判断题 -->
      <template v-if="formData.type === 1 || formData.type === 3">
        <el-select v-model="formData.answer" placeholder="选择正确答案" style="width:100%">
          <template v-if="formData.type === 1">
            <el-option v-for="(_, idx) in options" :key="idx" :label="`${String.fromCharCode(65+idx)}`" :value="`${String.fromCharCode(65+idx)}`" />
          </template>
          <template v-else>
            <el-option label="正确" value="T" />
            <el-option label="错误" value="F" />
          </template>
        </el-select>
      </template>
      <!-- 多选题 -->
      <template v-else-if="formData.type === 2">
        <el-checkbox-group v-model="multiAnswers">
          <el-checkbox v-for="(_, idx) in options" :key="idx" :label="`${String.fromCharCode(65+idx)}`">
            {{ String.fromCharCode(65 + idx) }}
          </el-checkbox>
        </el-checkbox-group>
      </template>
      <!-- 其他题型 -->
      <template v-else>
        <el-input v-model="formData.answer" type="textarea" :rows="3" placeholder="输入正确答案或参考答案（多答案用|分隔）" />
      </template>
    </el-form-item>

    <el-form-item label="答案解析">
      <el-input v-model="formData.analysis" type="textarea" :rows="3" placeholder="输入答案解析（可选）" />
    </el-form-item>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-form-item label="默认分值" prop="score">
          <el-input-number v-model="formData.score" :min="0.5" :max="100" :step="0.5" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="知识点标签">
          <el-input v-model="formData.knowledgeTags" placeholder="用逗号分隔多个标签" />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'

const props = defineProps({
  form: { type: Object, default: () => ({}) },
})

const formRef = ref()
const options = ref([
  { label: 'A', content: '' },
  { label: 'B', content: '' },
  { label: 'C', content: '' },
  { label: 'D', content: '' },
])
const multiAnswers = ref([])

const formData = reactive({
  id: null,
  type: 1,
  content: '',
  answer: '',
  analysis: '',
  score: 1,
  difficulty: 3,
  subjectId: 1,
  orgId: 1,
  knowledgeTags: '',
  options: '',
})

// 初始化表单数据
watch(() => props.form, (val) => {
  if (val) {
    Object.assign(formData, val)
    if (val.options) {
      try {
        options.value = JSON.parse(val.options)
      } catch (e) {}
    }
  }
}, { immediate: true })

// 多选答案同步
watch(multiAnswers, (val) => {
  formData.answer = val.sort().join('')
})

const addOption = () => {
  options.value.push({ label: String.fromCharCode(65 + options.value.length), content: '' })
}

const removeOption = (idx) => {
  options.value.splice(idx, 1)
}

const rules = {
  type: [{ required: true, message: '请选择题型', trigger: 'change' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  answer: [{ required: true, message: '请输入正确答案', trigger: 'blur' }],
  score: [{ required: true, message: '请输入分值', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
}

const validate = async () => {
  return await formRef.value?.validate()
}

const getFormData = () => {
  const data = { ...formData }
  if (formData.type <= 2) {
    data.options = JSON.stringify(options.value)
  }
  return data
}

defineExpose({ validate, getFormData })
</script>

<style scoped lang="scss">
.options-editor {
  width: 100%;
  .option-item {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    .opt-label {
      width: 24px;
      font-weight: bold;
      flex-shrink: 0;
    }
  }
}
</style>
