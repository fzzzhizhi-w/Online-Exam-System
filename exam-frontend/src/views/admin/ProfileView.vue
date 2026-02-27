<template>
  <div class="profile-view">
    <el-card header="个人中心" style="max-width:600px;margin:0 auto">
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名"><el-input :value="authStore.userInfo?.username" disabled /></el-form-item>
        <el-form-item label="真实姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSave">保存</el-button></el-form-item>
      </el-form>
      <el-divider>修改密码</el-divider>
      <el-form :model="pwdForm" label-width="90px">
        <el-form-item label="原密码"><el-input v-model="pwdForm.oldPassword" type="password" /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" /></el-form-item>
        <el-form-item><el-button type="warning" @click="handleChangePwd">修改密码</el-button></el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { updateProfile, changePassword } from '@/api/user'

const authStore = useAuthStore()
const form = reactive({ realName: authStore.userInfo?.realName, phone: authStore.userInfo?.phone, email: authStore.userInfo?.email })
const pwdForm = reactive({ oldPassword: '', newPassword: '' })

const handleSave = async () => {
  await updateProfile(form)
  ElMessage.success('保存成功')
  await authStore.fetchUserInfo()
}

const handleChangePwd = async () => {
  await changePassword(pwdForm)
  ElMessage.success('密码修改成功')
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
}
</script>
