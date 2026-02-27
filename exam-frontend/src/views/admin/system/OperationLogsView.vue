<template>
  <div class="op-logs">
    <el-card shadow="never" header="操作日志">
      <el-form :model="query" inline>
        <el-form-item><el-input v-model="query.username" placeholder="用户名" clearable /></el-form-item>
        <el-form-item><el-input v-model="query.module" placeholder="模块" clearable /></el-form-item>
        <el-form-item><el-button type="primary" @click="loadData">搜索</el-button></el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" border size="small">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="operation" label="操作" />
        <el-table-column prop="requestMethod" label="方法" width="80" />
        <el-table-column prop="requestIp" label="IP" width="130" />
        <el-table-column prop="costTime" label="耗时(ms)" width="90" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="160" />
      </el-table>
      <el-pagination v-model:current-page="query.pageNum" :total="total" layout="total, prev, pager, next" class="mt-16" @current-change="loadData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ username: '', module: '', pageNum: 1, pageSize: 20 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/system/logs/page', { params: query })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

onMounted(loadData)
</script>

<style scoped>
.mt-16 { margin-top: 16px; text-align: right; }
</style>
