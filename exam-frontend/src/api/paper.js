import request from '@/utils/request'

// 分页查询试卷
export const pagePapers = (params) => request.get('/papers/page', { params })

// 获取试卷详情
export const getPaper = (id) => request.get(`/papers/${id}`)

// 保存试卷
export const savePaper = (data) => request.post('/papers/save', data)

// 删除试卷
export const deletePaper = (id) => request.delete(`/papers/${id}`)

// 发布试卷
export const publishPaper = (id) => request.post(`/papers/${id}/publish`)

// 复制试卷
export const copyPaper = (id) => request.post(`/papers/${id}/copy`)
