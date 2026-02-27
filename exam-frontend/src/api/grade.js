import request from '@/utils/request'

// 获取评卷任务
export const getGradingTasks = (params) => request.get('/grade/tasks', { params })

// 人工评分
export const manualGrade = (detailId, data) =>
  request.post(`/grade/tasks/${detailId}/grade`, data)

// 仲裁评分
export const arbitrateGrade = (detailId, data) =>
  request.post(`/grade/tasks/${detailId}/arbitrate`, data)
