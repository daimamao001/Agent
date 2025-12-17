<template>
  <Layout :userInfo="userStore.userInfo">
    <div class="home-grid">
      <div class="left">
        <el-card class="profile-card" shadow="hover">
          <template #header>
            <div class="profile-header">
              <h2>个人信息</h2>
            </div>
          </template>

          <div class="profile-body">
            <div class="avatar-area">
              <el-avatar :size="80" :src="userStore.userInfo?.avatar">{{ initials }}</el-avatar>
            </div>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="用户名">{{ userStore.userInfo?.username || '-' }}</el-descriptions-item>
              <el-descriptions-item label="昵称">{{ userStore.userInfo?.nickname || '-' }}</el-descriptions-item>
              <el-descriptions-item label="角色">{{ userStore.role || userStore.userInfo?.role || 'user' }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ userStore.userInfo?.email || '-' }}</el-descriptions-item>
              <el-descriptions-item label="手机">{{ userStore.userInfo?.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ userStore.userInfo?.gender ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="生日">{{ userStore.userInfo?.birthdate ?? '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </div>
      <div class="right">
        <ChatPanel />
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import Layout from '../components/Layout.vue'
import ChatPanel from '../components/ChatPanel.vue'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

const initials = computed(() => {
  const name = userStore.userInfo?.nickname || userStore.userInfo?.username || 'U'
  return name.slice(0, 1).toUpperCase()
})

onMounted(async () => {
  try {
    await userStore.initializeUserInfo()
  } catch (e) {
    // 忽略初始化失败（路由守卫会确保已登录）
  }
})
</script>
<style scoped>
.home-grid { padding: 24px; background-color: #f5f7fa; min-height: calc(100vh - 64px); display: grid; grid-template-columns: 380px 1fr; gap: 16px; }
.left { min-width: 0; }
.right { min-width: 0; }
.profile-card { }
.profile-header { display: flex; align-items: center; justify-content: space-between; }
.profile-body { display: grid; grid-template-columns: 120px 1fr; gap: 20px; align-items: start; }
.avatar-area { display: flex; align-items: center; justify-content: center; }

@media (max-width: 768px) {
  .home-grid { grid-template-columns: 1fr; }
  .profile-body { grid-template-columns: 1fr; }
}
</style>