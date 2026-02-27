import request from '@/utils/request'

// 登录
export const login = (data) => request.post('/auth/login', data)

// 退出
export const logout = () => request.post('/auth/logout')

// 获取当前用户信息
export const getCurrentUser = () => request.get('/users/me')
