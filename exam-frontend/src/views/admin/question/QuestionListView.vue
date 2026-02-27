<template>
  <div class="question-list">
    <!-- 搜索筛选区 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="搜索题目内容" clearable style="width:200px" />
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="queryForm.type" placeholder="全部" clearable style="width:120px">
            <el-option label="单选题" :value="1" />
            <el-option label="多选题" :value="2" />
            <el-option label="判断题" :value="3" />
            <el-option label="填空题" :value="4" />
            <el-option label="简答题" :value="5" />
            <el-option label="综合案例" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="queryForm.difficulty" placeholder="全部" clearable style="width:100px">
            <el-option label="容易" :value="1" />
            <el-option label="较易" :value="2" />
            <el-option label="中等" :value="3" />
            <el-option label="较难" :value="4" />
            <el-option label="困难" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="queryForm.auditStatus" placeholder="全部" clearable style="width:110px">
            <el-option label="待审核" :value="0" />
            <el-option label="已审核" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮区 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>题目列表</span>
          <div>
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增题目</el-button>
            <el-button :icon="Upload" @click="importDialogVisible = true">批量导入</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="题目内容" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-html="row.content.substring(0, 100)" />
          </template>
        </el-table-column>
        <el-table-column prop="type" label="题型" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="getTypeTag(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="80">
          <template #default="{ row }">
            <el-rate :model-value="row.difficulty" :max="5" disabled size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="70" />
        <el-table-column prop="auditStatus" label="审核" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="getAuditTag(row.auditStatus)">
              {{ getAuditName(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="usedCount" label="使用次数" width="90" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.auditStatus === 0"
              size="small" text type="success"
              @click="handleAudit(row, 1)"
            >审核通过</el-button>
            <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pagination"
        @change="loadData"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editForm.id ? '编辑题目' : '新增题目'"
      width="800px"
      destroy-on-close
    >
      <QuestionForm ref="questionFormRef" :form="editForm" />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入弹窗 -->
    <el-dialog v-model="importDialogVisible" title="批量导入题目" width="500px">
      <el-alert
        title="请下载Excel模板，按模板格式填写后上传"
        type="info"
        :closable="false"
        class="mb-16"
      />
      <el-upload
        ref="uploadRef"
        action="#"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
      >
        <el-button type="primary">选择Excel文件</el-button>
        <template #tip>
          <div class="upload-tip">支持 .xlsx .xls 格式，文件大小不超过10MB</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Upload } from '@element-plus/icons-vue'
import { pageQuestions, deleteQuestion, auditQuestion, importQuestions } from '@/api/question'
import QuestionForm from './QuestionForm.vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const importDialogVisible = ref(false)
const submitLoading = ref(false)
const questionFormRef = ref()
const uploadRef = ref()
const selectedFile = ref(null)

const queryForm = reactive({
  keyword: '',
  type: null,
  difficulty: null,
  auditStatus: null,
  pageNum: 1,
  pageSize: 10,
})

const editForm = ref({})

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageQuestions(queryForm)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  loadData()
}

const handleReset = () => {
  Object.assign(queryForm, { keyword: '', type: null, difficulty: null, auditStatus: null, pageNum: 1 })
  loadData()
}

const handleAdd = () => {
  editForm.value = {}
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editForm.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await questionFormRef.value?.validate()
  if (!valid) return
  submitLoading.value = true
  try {
    const { saveQuestion } = await import('@/api/question')
    await saveQuestion(questionFormRef.value.getFormData())
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除题目 ID=${row.id} 吗？`, '警告', { type: 'warning' })
  await deleteQuestion(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleAudit = async (row, status) => {
  await auditQuestion(row.id, { status, remark: '审核通过' })
  ElMessage.success('审核成功')
  loadData()
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const handleImport = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }
  const formData = new FormData()
  formData.append('file', selectedFile.value)
  formData.append('orgId', '1')
  formData.append('subjectId', '1')
  const res = await importQuestions(formData)
  ElMessage.success(`成功导入 ${res.data} 道题目`)
  importDialogVisible.value = false
  loadData()
}

const getTypeName = (type) => {
  const map = { 1: '单选', 2: '多选', 3: '判断', 4: '填空', 5: '简答', 6: '案例' }
  return map[type] || type
}

const getTypeTag = (type) => {
  const map = { 1: '', 2: 'success', 3: 'warning', 4: 'info', 5: 'danger', 6: 'danger' }
  return map[type] || ''
}

const getAuditName = (status) => {
  const map = { 0: '待审核', 1: '已审核', 2: '已拒绝' }
  return map[status] || status
}

const getAuditTag = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.question-list {
  .search-card { margin-bottom: 16px; }
  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  .pagination { margin-top: 16px; text-align: right; }
  .mb-16 { margin-bottom: 16px; }
  .upload-tip { color: #999; font-size: 12px; margin-top: 8px; }
}
</style>
