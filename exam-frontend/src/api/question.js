import request from '@/utils/request'

// 分页查询题目
export const pageQuestions = (params) => request.get('/questions/page', { params })

// 获取题目详情
export const getQuestion = (id) => request.get(`/questions/${id}`)

// 保存题目
export const saveQuestion = (data) => request.post('/questions/save', data)

// 删除题目
export const deleteQuestion = (id) => request.delete(`/questions/${id}`)

// 审核题目
export const auditQuestion = (id, data) => request.post(`/questions/${id}/audit`, data)

// 批量导入题目
export const importQuestions = (formData) =>
  request.post('/questions/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
