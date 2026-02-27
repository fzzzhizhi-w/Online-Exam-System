import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const roleCode = computed(() => userInfo.value?.roleCode || '')
  const isAdmin = computed(() => ['SUPER_ADMIN', 'ORG_ADMIN'].includes(roleCode.value))
  const isTeacher = computed(() => ['SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER'].includes(roleCode.value))

  async function login(username, password) {
    const res = await request.post('/auth/login', { username, password })
    token.value = res.data.token
    userInfo.value = res.data.userInfo
    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    return res.data
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  async function fetchUserInfo() {
    const res = await request.get('/users/me')
    userInfo.value = res.data
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    roleCode,
    isAdmin,
    isTeacher,
    login,
    logout,
    fetchUserInfo,
  }
}, {
  persist: false,
})
