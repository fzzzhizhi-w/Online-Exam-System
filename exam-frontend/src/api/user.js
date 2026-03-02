import request from '@/utils/request'

// 分页查询用户
export const pageUsers = (params) => request.get('/users/page', { params })

// 获取用户详情
export const getUser = (id) => request.get(`/users/${id}`)

// 保存用户
export const saveUser = (data) => request.post('/users/save', data)

// 删除用户
export const deleteUser = (id) => request.delete(`/users/${id}`)

// 重置密码
export const resetPassword = (id, password) =>
  request.post(`/users/${id}/reset-password`, { password })

// 修改状态
export const updateStatus = (id, status) =>
  request.put(`/users/${id}/status/${status}`)

// 修改个人信息
export const updateProfile = (data) => request.put('/users/me/profile', data)

// 修改密码
export const changePassword = (data) => request.put('/users/me/password', data)

// 批量删除用户
export const batchDeleteUsers = (ids) => request.delete('/users/batch-delete', { data: ids })

// 批量禁用用户
export const batchDisableUsers = (ids) => request.put('/users/batch-disable', ids)

// 批量启用用户
export const batchEnableUsers = (ids) => request.put('/users/batch-enable', ids)

// 检查用户名是否可用
export const checkUsername = (username, excludeId) =>
  request.get('/users/check-username', { params: { username, excludeId } })

// 检查手机号是否可用
export const checkPhone = (phone, excludeId) =>
  request.get('/users/check-phone', { params: { phone, excludeId } })
