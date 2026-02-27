<template>
  <div class="user-list">
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>用户管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
        </div>
      </template>

      <!-- 搜索区 -->
      <el-form :model="query" inline class="mb-16">
        <el-form-item>
          <el-input v-model="query.keyword" placeholder="姓名/用户名" clearable style="width:180px" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="query.roleCode" placeholder="角色" clearable style="width:130px">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="机构管理员" value="ORG_ADMIN" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="评卷员" value="GRADER" />
            <el-option label="考生" value="STUDENT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="() => { query.keyword = ''; query.roleCode = ''; loadData() }">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="roleCode" label="角色" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ getRoleName(row.roleCode) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" text :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" text type="info" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        class="mt-16"
        @change="loadData"
      />
    </el-card>

    <!-- 用户表单弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑用户' : '新增用户'" width="550px">
      <el-form ref="formRef" :model="editForm" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" :disabled="!!editForm.id" />
        </el-form-item>
        <el-form-item label="密码" :prop="editForm.id ? '' : 'password'">
          <el-input v-model="editForm.password" type="password" :placeholder="editForm.id ? '不修改请留空' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="角色" prop="roleCode">
          <el-select v-model="editForm.roleCode" style="width:100%">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="机构管理员" value="ORG_ADMIN" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="评卷员" value="GRADER" />
            <el-option label="考生" value="STUDENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属机构" prop="orgId">
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
import { Plus } from '@element-plus/icons-vue'
import { pageUsers, saveUser, deleteUser, resetPassword, updateStatus } from '@/api/user'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()
const editForm = ref({})

const query = reactive({ keyword: '', roleCode: '', pageNum: 1, pageSize: 10 })

const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  realName: [{ required: true, message: '请输入姓名' }],
  roleCode: [{ required: true, message: '请选择角色' }],
  orgId: [{ required: true, message: '请选择机构' }],
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageUsers(query)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleAdd = () => { editForm.value = { orgId: 1, status: 1 }; dialogVisible.value = true }
const handleEdit = (row) => { editForm.value = { ...row, password: '' }; dialogVisible.value = true }

const handleSubmit = async () => {
  await formRef.value?.validate()
  await saveUser(editForm.value)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除用户 ${row.realName} 吗？`, '警告', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  await updateStatus(row.id, newStatus)
  ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  loadData()
}

const handleResetPwd = async (row) => {
  await ElMessageBox.confirm(`确定重置用户 ${row.realName} 的密码为 exam@123456 吗？`)
  await resetPassword(row.id, 'exam@123456')
  ElMessage.success('密码已重置')
}

const getRoleName = (code) => {
  const map = { SUPER_ADMIN: '超级管理员', ORG_ADMIN: '机构管理员', TEACHER: '教师', GRADER: '评卷员', STUDENT: '考生' }
  return map[code] || code
}

onMounted(loadData)
</script>

<style scoped>
.mb-16 { margin-bottom: 16px; }
.mt-16 { margin-top: 16px; text-align: right; }
</style>
