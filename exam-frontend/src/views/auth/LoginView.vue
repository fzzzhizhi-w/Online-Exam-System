<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <el-icon size="40" color="#165DFF"><Collection /></el-icon>
        </div>
        <h1>捷评智航考试系统</h1>
        <p class="subtitle">高可用智能化在线考试与智能评卷平台</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        size="large"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            prefix-icon="User"
            placeholder="请输入用户名"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            prefix-icon="Lock"
            placeholder="请输入密码"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-tips">
        <p>测试账号：</p>
        <ul>
          <li>超级管理员：superadmin / exam@123456</li>
          <li>教师：teacher01 / exam@123456</li>
          <li>学员：student01 / exam@123456</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authStore.login(loginForm.username, loginForm.password)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/dashboard'
    router.push(redirect)
  } catch (e) {
    // 错误已由请求拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0F172A 0%, #1E3A5F 50%, #165DFF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-box {
  width: 420px;
  background: rgba(255, 255, 255, 0.97);
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

  .logo {
    margin-bottom: 12px;
  }

  h1 {
    font-size: 24px;
    font-weight: 700;
    color: #1D2129;
    margin-bottom: 8px;
  }

  .subtitle {
    font-size: 13px;
    color: #86909C;
  }
}

.login-form {
  .login-btn {
    width: 100%;
    height: 44px;
    font-size: 16px;
    letter-spacing: 4px;
    background: #165DFF;
    border-color: #165DFF;
    border-radius: 8px;

    &:hover {
      background: #4080FF;
      border-color: #4080FF;
    }
  }
}

.login-tips {
  margin-top: 20px;
  padding: 12px 16px;
  background: #F2F3F5;
  border-radius: 8px;
  font-size: 12px;
  color: #4E5969;

  p {
    margin-bottom: 6px;
    font-weight: bold;
  }

  ul {
    padding-left: 16px;
    li {
      margin-bottom: 4px;
    }
  }
}
</style>
