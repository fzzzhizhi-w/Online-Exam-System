import request from '@/utils/request'

// 考试整体统计
export const getExamOverview = (examId) => request.get(`/stats/exam/${examId}/overview`)

// 分数段分布
export const getScoreDistribution = (examId) =>
  request.get(`/stats/exam/${examId}/score-distribution`)

// 知识点掌握度
export const getKnowledgeMastery = (examId) =>
  request.get(`/stats/exam/${examId}/knowledge-mastery`)

// 个人成绩报告
export const getMyReport = (recordId) => request.get(`/stats/my/report/${recordId}`)

// 我的考试历史
export const getMyHistory = (params) => request.get('/stats/my/history', { params })
