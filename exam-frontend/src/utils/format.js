import dayjs from 'dayjs'

/**
 * 格式化日期时间为 YYYY-MM-DD HH:mm
 */
export const formatDateTime = (val) => {
  if (!val) return '--'
  return dayjs(val).format('YYYY-MM-DD HH:mm')
}

/**
 * 格式化日期为 YYYY-MM-DD
 */
export const formatDate = (val) => {
  if (!val) return '--'
  return dayjs(val).format('YYYY-MM-DD')
}

/**
 * 格式化秒数为 x分x秒
 */
export const formatDuration = (seconds) => {
  if (!seconds) return '--'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}分${s}秒`
}

/**
 * 创建防抖函数
 */
export const debounce = (fn, delay = 300) => {
  let timer = null
  return (...args) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }
}
