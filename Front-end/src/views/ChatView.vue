<template>
  <div class="chat-page">
    <aside class="sidebar">
      <div class="logo-row">
        <div class="logo">G</div>
        <div class="titles">
          <div class="title">ChatGPT 风格</div>
          <div class="subtitle">Tencent Hunyuan</div>
        </div>
      </div>
      <button class="primary block" @click="newSession">+ 新对话</button>

      <div class="session-list">
        <div
          v-for="session in sessions"
          :key="session.id"
          class="session-item"
          :class="{ active: session.id === activeId }"
          @click="selectSession(session.id)"
        >
          <div class="session-title">{{ session.title }}</div>
          <div class="session-meta">{{ session.messages.length }} 条消息</div>
        </div>
      </div>

      <div class="sidebar-bottom">
        <div class="pill">{{ modelName }}</div>
        <button class="ghost block" v-if="isAuthed" @click="logout">退出登录</button>
        <button class="ghost block" v-else @click="goLogin">去登录</button>
      </div>
    </aside>

    <main class="chat-main">
      <div class="chat-header">
        <div>
          <div class="chat-title">{{ currentSession?.title || '新的对话' }}</div>
          <div class="chat-subtitle">对话保存在本地，不涉及云端存储</div>
        </div>
        <div class="header-actions">
          <button class="ghost" @click="clearSession">清空</button>
        </div>
      </div>

      <section class="chat-wrapper">
        <ChatPanel
          :messages="currentMessages"
          :sending="sending"
          @send="handleSend"
          @clear="clearSession"
        />
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ChatPanel from '../components/ChatPanel.vue'
import { sendChat } from '../api/chat'

const router = useRouter()
const isAuthed = computed(() => !!localStorage.getItem('token'))
const modelName = 'hunyuan-turbos-latest'

const sessions = ref([
  {
    id: Date.now(),
    title: '新的对话',
    messages: [{ role: 'assistant', content: '你好，我是你的智能助手。有任何问题随时问我。' }]
  }
])
const activeId = ref(sessions.value[0].id)
const sending = ref(false)

const currentSession = computed(() => sessions.value.find((s) => s.id === activeId.value))
const currentMessages = computed(() => currentSession.value?.messages || [])

const selectSession = (id) => {
  activeId.value = id
}

const newSession = () => {
  const id = Date.now()
  sessions.value.unshift({
    id,
    title: '新的对话',
    messages: [{ role: 'assistant', content: '你好，我是你的智能助手。有任何问题随时问我。' }]
  })
  activeId.value = id
}

const clearSession = () => {
  if (!currentSession.value) return
  currentSession.value.messages = [{ role: 'assistant', content: '好的，我们重新开始。' }]
}

const handleSend = async (content) => {
  if (!currentSession.value) return
  const session = currentSession.value
  session.messages.push({ role: 'user', content })
  // 更新标题为首条用户消息
  if (session.title === '新的对话') {
    session.title = content.slice(0, 18) || '新的对话'
  }

  sending.value = true
  try {
    const payload = session.messages.map((m) => ({ role: m.role, content: m.content }))
    const res = await sendChat(payload)
    const reply = res?.content || res?.data?.content || res?.response || '（未获取到回复）'
    session.messages.push({ role: 'assistant', content: reply })
    if (!res?.content && !res?.data?.content) {
      console.warn('聊天返回缺少content字段，原始响应:', res)
    }
  } catch (err) {
    console.error(err)
    ElMessage.error('对话失败，请稍后重试')
    session.messages.push({ role: 'assistant', content: '抱歉，服务暂时不可用。' })
  } finally {
    sending.value = false
  }
}

const goLogin = () => {
  router.push({ name: 'login' })
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userRole')
  localStorage.removeItem('userInfo')
  goLogin()
}
</script>

<style scoped>
.chat-page { min-height: 100vh; background: #0b1021; color: #e5e7eb; display: grid; grid-template-columns: 280px 1fr; }
.sidebar { background: #0f152d; border-right: 1px solid rgba(255,255,255,0.06); padding: 18px; display: flex; flex-direction: column; gap: 14px; }
.logo-row { display: flex; align-items: center; gap: 10px; padding-bottom: 6px; }
.logo { width: 42px; height: 42px; border-radius: 12px; background: linear-gradient(135deg, #22d3ee, #818cf8); display: flex; align-items: center; justify-content: center; font-weight: 800; color: #0b1021; font-size: 18px; box-shadow: 0 10px 40px rgba(0,0,0,0.35); }
.titles .title { font-size: 16px; font-weight: 700; }
.titles .subtitle { font-size: 12px; color: #9ca3af; }
.primary.block, .ghost.block { width: 100%; }
.primary { border: none; background: linear-gradient(135deg, #22d3ee, #818cf8); color: #0b1021; border-radius: 10px; padding: 10px 12px; font-weight: 700; cursor: pointer; box-shadow: 0 10px 30px rgba(0,0,0,0.35); }
.ghost { border: 1px solid rgba(255,255,255,0.18); background: transparent; color: #e5e7eb; border-radius: 10px; padding: 10px 12px; font-weight: 600; cursor: pointer; }
.ghost:hover { border-color: rgba(255,255,255,0.35); }
.session-list { flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 6px; }
.session-item { padding: 10px 12px; border-radius: 10px; border: 1px solid rgba(255,255,255,0.06); background: rgba(255,255,255,0.02); cursor: pointer; }
.session-item.active { border-color: rgba(34,211,238,0.7); background: rgba(34,211,238,0.06); }
.session-title { font-size: 14px; font-weight: 600; color: #e5e7eb; }
.session-meta { font-size: 12px; color: #9ca3af; margin-top: 4px; }
.sidebar-bottom { border-top: 1px solid rgba(255,255,255,0.06); padding-top: 12px; display: flex; flex-direction: column; gap: 10px; }
.pill { padding: 8px 12px; border-radius: 999px; background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.12); font-size: 12px; color: #cbd5e1; text-align: center; }

.chat-main { display: flex; flex-direction: column; padding: 20px; gap: 14px; }
.chat-header { display: flex; align-items: center; justify-content: space-between; }
.chat-title { font-size: 18px; font-weight: 700; }
.chat-subtitle { font-size: 12px; color: #9ca3af; margin-top: 2px; }
.header-actions { display: flex; gap: 10px; }
.chat-wrapper { width: min(1100px, 100%); margin: 0 auto; }

@media (max-width: 900px) {
  .chat-page { grid-template-columns: 1fr; }
  .sidebar { display: none; }
  .chat-wrapper { width: 100%; }
}
</style>