<template>
  <div class="paper-list">
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>试卷管理</span>
          <el-button type="primary" @click="handleAdd">新建试卷</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="试卷名称" />
        <el-table-column prop="generateMode" label="组卷模式" width="110">
          <template #default="{ row }">
            <el-tag size="small">{{ {1:'固定组卷',2:'随机组卷',3:'自适应'}[row.generateMode] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column prop="duration" label="时长(分)" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" size="small" text type="success" @click="handlePublish(row)">发布</el-button>
            <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑试卷' : '新建试卷'" width="600px">
      <el-form :model="editForm" label-width="100px" ref="formRef">
        <el-form-item label="试卷名称" :rules="[{required:true}]" prop="name">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="组卷模式" prop="generateMode">
          <el-select v-model="editForm.generateMode" style="width:100%">
            <el-option label="固定组卷" :value="1" />
            <el-option label="随机组卷" :value="2" />
            <el-option label="自适应组卷" :value="3" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="总分"><el-input-number v-model="editForm.totalScore" :min="1" :max="1000" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="及格分"><el-input-number v-model="editForm.passScore" :min="1" :max="1000" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="考试时长(分)">
          <el-input-number v-model="editForm.duration" :min="1" :max="600" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="乱序题目">
              <el-switch v-model="editForm.shuffleQuestion" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="乱序选项">
              <el-switch v-model="editForm.shuffleOption" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pagePapers, savePaper, deletePaper, publishPaper } from '@/api/paper'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const editForm = ref({})
const formRef = ref()

const loadData = async () => {
  loading.value = true
  try {
    const res = await pagePapers({ pageNum: 1, pageSize: 20 })
    tableData.value = res.data?.records || []
  } finally { loading.value = false }
}

const handleAdd = () => { editForm.value = { generateMode: 1, totalScore: 100, passScore: 60, duration: 60 }; dialogVisible.value = true }
const handleEdit = (row) => { editForm.value = { ...row }; dialogVisible.value = true }

const handleSubmit = async () => {
  await formRef.value?.validate()
  await savePaper(editForm.value)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const handlePublish = async (row) => {
  await publishPaper(row.id)
  ElMessage.success('发布成功')
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除试卷 "${row.name}"？`, '警告', { type: 'warning' })
  await deletePaper(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
