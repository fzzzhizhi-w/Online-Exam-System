<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo-area">
        <el-icon class="logo-icon" size="28"><Collection /></el-icon>
        <span v-if="!isCollapsed" class="logo-text">捷评智航</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          background-color="#001529"
          text-color="#c0c4cc"
          active-text-color="#409EFF"
          router
          :collapse-transition="false"
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
            <el-icon><Document /></el-icon>
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
              <el-avatar :size="32" class="avatar">
                {{ authStore.userInfo?.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ authStore.userInfo?.realName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
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
}

.sidebar {
  background-color: #001529;
  overflow: hidden;
  transition: width 0.3s;

  .logo-area {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: #fff;
    font-size: 18px;
    font-weight: bold;
    border-bottom: 1px solid rgba(255,255,255,0.1);

    .logo-icon {
      color: #409EFF;
      flex-shrink: 0;
    }

    .logo-text {
      white-space: nowrap;
      overflow: hidden;
    }
  }
}

.header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08);

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 4px;

      &:hover {
        background: #f5f5f5;
      }

      .username {
        font-size: 14px;
        color: #333;
      }
    }
  }
}

.main-content {
  background: #f0f2f5;
  padding: 16px;
  overflow-y: auto;
}

:deep(.el-menu) {
  border-right: none;
}
</style>
