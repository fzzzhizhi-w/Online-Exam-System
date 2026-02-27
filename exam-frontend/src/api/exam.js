import request from '@/utils/request'

// 分页查询考试
export const pageExams = (params) => request.get('/exams/page', { params })

// 获取考试详情
export const getExam = (id) => request.get(`/exams/${id}`)

// 保存考试
export const saveExam = (data) => request.post('/exams/save', data)

// 发布考试
export const publishExam = (id) => request.post(`/exams/${id}/publish`)

// 结束考试
export const endExam = (id) => request.post(`/exams/${id}/end`)

// 进入考试
export const enterExam = (examId) => request.post(`/exams/${examId}/enter`)

// 实时保存答题进度
export const saveAnswerProgress = (data) => request.post('/exams/answer/save', data)

// 交卷
export const submitExam = (data) => request.post('/exams/answer/submit', data)

// 上报切屏事件
export const reportSwitchScreen = (recordId) =>
  request.post(`/exams/record/${recordId}/switch-screen`)

// 强制收卷
export const forceSubmit = (recordId) =>
  request.post(`/exams/record/${recordId}/force-submit`)

// 查询答卷记录
export const pageRecords = (params) => request.get('/exams/records', { params })
