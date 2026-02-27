import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import NProgress from 'nprogress'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { title: '登录', noAuth: true },
  },
  {
    path: '/',
    component: () => import('@/components/layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: '控制台' },
      },
      // 用户管理
      {
        path: 'user/list',
        name: 'UserList',
        component: () => import('@/views/admin/user/UserListView.vue'),
        meta: { title: '用户管理', roles: ['SUPER_ADMIN', 'ORG_ADMIN'] },
      },
      // 题库管理
      {
        path: 'question/list',
        name: 'QuestionList',
        component: () => import('@/views/admin/question/QuestionListView.vue'),
        meta: { title: '题库管理', roles: ['SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER'] },
      },
      // 试卷管理
      {
        path: 'paper/list',
        name: 'PaperList',
        component: () => import('@/views/admin/paper/PaperListView.vue'),
        meta: { title: '试卷管理', roles: ['SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER'] },
      },
      // 考试管理
      {
        path: 'exam/list',
        name: 'ExamList',
        component: () => import('@/views/admin/exam/ExamListView.vue'),
        meta: { title: '考试管理', roles: ['SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER'] },
      },
      // 监考中心
      {
        path: 'exam/monitor/:examId',
        name: 'ExamMonitor',
        component: () => import('@/views/admin/exam/ExamMonitorView.vue'),
        meta: { title: '监考中心', roles: ['SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER'] },
      },
      // 评卷中心
      {
        path: 'grade/tasks',
        name: 'GradeTaskList',
        component: () => import('@/views/admin/grade/GradeTaskListView.vue'),
        meta: { title: '评卷中心', roles: ['SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER', 'GRADER'] },
      },
      // 统计分析
      {
        path: 'stats/exam/:examId',
        name: 'ExamStats',
        component: () => import('@/views/admin/stats/ExamStatsView.vue'),
        meta: { title: '考试统计', roles: ['SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER'] },
      },
      // 操作日志
      {
        path: 'system/logs',
        name: 'OperationLogs',
        component: () => import('@/views/admin/system/OperationLogsView.vue'),
        meta: { title: '操作日志', roles: ['SUPER_ADMIN', 'ORG_ADMIN'] },
      },
      // 个人中心
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/admin/ProfileView.vue'),
        meta: { title: '个人中心' },
      },
    ],
  },
  // 考生答题页面（独立布局）
  {
    path: '/exam/take/:examId',
    name: 'ExamTaking',
    component: () => import('@/views/student/ExamTakingView.vue'),
    meta: { title: '在线考试' },
  },
  // 成绩报告页面
  {
    path: '/exam/report/:recordId',
    name: 'ExamReport',
    component: () => import('@/views/student/ExamReportView.vue'),
    meta: { title: '成绩报告' },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
  NProgress.start()
  const authStore = useAuthStore()

  if (to.meta.noAuth) {
    if (authStore.isLoggedIn && to.name === 'Login') {
      next({ name: 'Dashboard' })
    } else {
      next()
    }
    return
  }

  if (!authStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 角色权限校验
  if (to.meta.roles && !to.meta.roles.includes(authStore.roleCode)) {
    next({ name: 'Dashboard' })
    return
  }

  document.title = `${to.meta.title || '捷评智航'} - 捷评智航考试系统`
  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
