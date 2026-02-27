<template>
  <div class="exam-list">
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>考试管理</span>
          <el-button type="primary" @click="handleAdd">新建考试</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="name" label="考试名称" />
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" size="small" text type="success" @click="handlePublish(row)">发布</el-button>
            <el-button v-if="row.status === 1 || row.status === 2" size="small" text type="warning" @click="handleEnd(row)">结束</el-button>
            <el-button size="small" text @click="$router.push(`/stats/exam/${row.id}`)">统计</el-button>
            <el-button size="small" text @click="$router.push(`/exam/monitor/${row.id}`)">监考</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        class="mt-16"
        @change="loadData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑考试' : '新建考试'" width="600px">
      <el-form :model="editForm" label-width="100px" ref="formRef">
        <el-form-item label="考试名称" prop="name" :rules="[{required:true,message:'请输入考试名称'}]">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="关联试卷" prop="paperId" :rules="[{required:true,message:'请选择试卷'}]">
          <el-input-number v-model="editForm.paperId" :min="1" placeholder="试卷ID" style="width:100%" />
        </el-form-item>
        <el-form-item label="考试时间" prop="timeRange" :rules="[{required:true,message:'请选择时间'}]">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width:100%"
            @change="handleTimeChange"
          />
        </el-form-item>
        <el-form-item label="防切屏次数">
          <el-input-number v-model="editForm.switchScreenLimit" :min="0" :max="99" />
          <span class="hint">（0=不限制）</span>
        </el-form-item>
        <el-form-item label="展示成绩">
          <el-switch v-model="editForm.showScore" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="展示解析">
          <el-switch v-model="editForm.showAnalysis" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="机构ID" prop="orgId">
          <el-input-number v-model="editForm.orgId" :min="1" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageExams, saveExam, publishExam, endExam } from '@/api/exam'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const editForm = ref({})
const timeRange = ref([])
const formRef = ref()

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageExams({ pageNum: pageNum.value, pageSize: pageSize.value })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editForm.value = { switchScreenLimit: 3, showScore: 1, showAnalysis: 0, orgId: 1 }
  timeRange.value = []
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editForm.value = { ...row }
  timeRange.value = row.startTime && row.endTime ? [new Date(row.startTime), new Date(row.endTime)] : []
  dialogVisible.value = true
}

const handleTimeChange = (val) => {
  if (val) {
    editForm.value.startTime = val[0]
    editForm.value.endTime = val[1]
  }
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  await saveExam(editForm.value)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const handlePublish = async (row) => {
  await ElMessageBox.confirm(`确定发布考试 "${row.name}" 吗？`)
  await publishExam(row.id)
  ElMessage.success('发布成功')
  loadData()
}

const handleEnd = async (row) => {
  await ElMessageBox.confirm(`确定结束考试 "${row.name}" 吗？`, '警告', { type: 'warning' })
  await endExam(row.id)
  ElMessage.success('考试已结束')
  loadData()
}

const getStatusType = (s) => ({ 0: 'info', 1: 'primary', 2: 'success', 3: 'danger', 4: 'warning', 5: '' }[s] || 'info')
const getStatusName = (s) => ({ 0: '草稿', 1: '已发布', 2: '进行中', 3: '已结束', 4: '评卷中', 5: '已完成' }[s] || s)

onMounted(loadData)
</script>

<style scoped>
.mt-16 { margin-top: 16px; text-align: right; }
.hint { color: #999; font-size: 12px; margin-left: 8px; }
</style>
