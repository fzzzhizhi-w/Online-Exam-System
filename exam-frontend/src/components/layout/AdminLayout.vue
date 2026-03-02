<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo-area">
        <el-icon class="logo-icon" size="28"><Collection /></el-icon>
        <transition name="logo-fade">
          <span v-if="!isCollapsed" class="logo-text">捷评智航</span>
        </transition>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          background-color="transparent"
          text-color="rgba(255,255,255,0.7)"
          active-text-color="#ffffff"
          router
          :collapse-transition="false"
          class="sidebar-menu"
        >
          <!-- 控制台 -->
          <el-menu-item index="/dashboard">
            <el-icon><DataBoard /></el-icon>
            <template #title>控制台</template>
          </el-menu-item>

          <!-- 用户管理 -->
          <el-menu-item v-if="authStore.isAdmin" index="/user/list">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>

          <!-- 题库管理 -->
          <el-sub-menu v-if="authStore.isTeacher" index="question">
            <template #title>
              <el-icon><EditPen /></el-icon>
              <span>题库管理</span>
            </template>
            <el-menu-item index="/question/list">题目管理</el-menu-item>
          </el-sub-menu>

          <!-- 试卷管理 -->
          <el-menu-item v-if="authStore.isTeacher" index="/paper/list">
            <el-icon><Document /></el-icon>
            <template #title>试卷管理</template>
          </el-menu-item>

          <!-- 考试管理 -->
          <el-menu-item v-if="authStore.isTeacher" index="/exam/list">
            <el-icon><Calendar /></el-icon>
            <template #title>考试管理</template>
          </el-menu-item>

          <!-- 评卷中心（教师/评卷员/管理员可见） -->
          <el-menu-item v-if="!authStore.isStudent" index="/grade/tasks">
            <el-icon><Checked /></el-icon>
            <template #title>评卷中心</template>
          </el-menu-item>

          <!-- 学生：我的考试 -->
          <el-menu-item v-if="authStore.isStudent" index="/student/exams">
            <el-icon><Calendar /></el-icon>
            <template #title>我的考试</template>
          </el-menu-item>

          <!-- 学生：我的成绩 -->
          <el-menu-item v-if="authStore.isStudent" index="/student/records">
            <el-icon><TrophyBase /></el-icon>
            <template #title>我的成绩</template>
          </el-menu-item>

          <!-- 系统管理 -->
          <el-sub-menu v-if="authStore.isAdmin" index="system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/logs">操作日志</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-button
            :icon="isCollapsed ? 'Expand' : 'Fold'"
            text
            size="large"
            class="collapse-btn"
            @click="toggleCollapse"
          />
          <!-- 面包屑 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-info">
              <div class="avatar-wrapper">
                <el-avatar :size="32" class="avatar" :style="{ background: roleColor }">
                  {{ authStore.userInfo?.realName?.charAt(0) || 'U' }}
                </el-avatar>
                <span class="role-badge" :style="{ background: roleColor }">
                  {{ roleShortName }}
                </span>
              </div>
              <span class="username">{{ authStore.userInfo?.realName }}</span>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><UserFilled /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 页面内容 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isCollapsed = ref(false)
const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title)

const roleColor = computed(() => {
  const code = authStore.userInfo?.roleCode
  const map = {
    SUPER_ADMIN: '#1D2129',
    ORG_ADMIN: '#165DFF',
    TEACHER: '#165DFF',
    GRADER: '#722ED1',
    STUDENT: '#722ED1',
  }
  return map[code] || '#165DFF'
})

const roleShortName = computed(() => {
  const code = authStore.userInfo?.roleCode
  const map = {
    SUPER_ADMIN: '管',
    ORG_ADMIN: '管',
    TEACHER: '师',
    GRADER: '评',
    STUDENT: '生',
  }
  return map[code] || '用'
})

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', {
      type: 'warning',
    })
    authStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.admin-layout {
  height: 100vh;
  background: var(--bg-secondary);
}

/* ============ 侧边栏 ============ */
.sidebar {
  background: linear-gradient(180deg, var(--sidebar-bg) 0%, var(--sidebar-bg-end) 100%);
  overflow: hidden;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;

  .logo-area {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    color: #fff;
    font-size: 18px;
    font-weight: 700;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    flex-shrink: 0;
    letter-spacing: 0.5px;

    .logo-icon {
      color: #4080FF;
      flex-shrink: 0;
      filter: drop-shadow(0 0 6px rgba(64, 128, 255, 0.5));
    }

    .logo-text {
      white-space: nowrap;
      overflow: hidden;
      color: #fff;
      background: linear-gradient(135deg, #fff 0%, #a5b4fc 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
  }
}

.logo-fade-enter-active,
.logo-fade-leave-active {
  transition: opacity 0.2s ease;
}
.logo-fade-enter-from,
.logo-fade-leave-to {
  opacity: 0;
}

/* 菜单全局覆盖 */
:deep(.sidebar-menu) {
  border-right: none !important;
  padding: 8px 0;

  .el-menu-item,
  .el-sub-menu__title {
    margin: 2px 8px;
    border-radius: 6px;
    height: 42px;
    line-height: 42px;
    transition: all 0.2s ease;
    position: relative;

    &:hover {
      background: rgba(255, 255, 255, 0.08) !important;
      color: #fff !important;
    }
  }

  .el-menu-item.is-active {
    background: rgba(22, 93, 255, 0.25) !important;
    color: #fff !important;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 6px;
      bottom: 6px;
      width: 3px;
      border-radius: 0 3px 3px 0;
      background: #4080FF;
    }

    .el-icon {
      color: #4080FF !important;
    }
  }

  /* 子菜单弹出层 */
  .el-sub-menu__title {
    &:hover {
      background: rgba(255, 255, 255, 0.08) !important;
      color: #fff !important;
    }
  }

  .el-menu--inline {
    background: transparent !important;

    .el-menu-item {
      margin-left: 8px;
      padding-left: 40px !important;
      color: rgba(255, 255, 255, 0.6) !important;
      font-size: 13px;

      &:hover {
        color: #fff !important;
      }

      &.is-active {
        color: #fff !important;
        background: rgba(22, 93, 255, 0.2) !important;
      }
    }
  }

  /* 折叠后的弹出菜单 */
  &.el-menu--collapse {
    .el-menu-item,
    .el-sub-menu__title {
      margin: 2px 4px;
    }
  }
}

/* ============ 顶部 Header ============ */
.header {
  height: 60px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.06);

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .collapse-btn {
      color: var(--text-secondary);
      &:hover { color: var(--primary-color); }
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      padding: 6px 10px;
      border-radius: var(--radius-lg);
      transition: background 0.2s ease;

      &:hover {
        background: var(--bg-secondary);
      }

      .avatar-wrapper {
        position: relative;
        .avatar {
          font-size: 14px;
          font-weight: 600;
        }
        .role-badge {
          position: absolute;
          bottom: -2px;
          right: -4px;
          width: 16px;
          height: 16px;
          border-radius: 50%;
          font-size: 9px;
          color: #fff;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 700;
          border: 1.5px solid #fff;
        }
      }

      .username {
        font-size: 14px;
        color: var(--text-primary);
        font-weight: 500;
      }

      .arrow-icon {
        color: var(--text-tertiary);
        font-size: 12px;
      }
    }
  }
}

/* ============ 主内容区 ============ */
.main-content {
  background: var(--bg-secondary);
  padding: 20px;
  overflow-y: auto;
}
</style>
