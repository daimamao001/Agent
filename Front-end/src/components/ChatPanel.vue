<template>
  <div class="chat-shell">
    <div class="chat-header">
      <div class="title">
        <el-icon><Message /></el-icon>
        <div class="title-text">
          <div class="primary">对话</div>
          <div class="sub">腾讯混元 · ChatGPT 风格</div>
        </div>
      </div>
      <div class="header-actions">
        <el-button size="small" text @click="emit('clear')">清空</el-button>
      </div>
    </div>

    <div class="chat-body" ref="listRef">
      <div
        v-for="(m, i) in messages"
        :key="i"
        class="msg-row"
        :class="m.role === 'user' ? 'me' : 'bot'"
      >
        <div class="avatar">
          <el-avatar :size="36" :class="m.role">
            {{ m.role === 'user' ? '我' : 'AI' }}
          </el-avatar>
        </div>
        <div class="bubble" v-html="formatContent(m.content)"></div>
      </div>
      <div v-if="sending" class="typing">AI 正在思考…</div>
    </div>

    <div class="chat-input">
      <el-input
        v-model="input"
        type="textarea"
        :rows="3"
        maxlength="2000"
        placeholder="输入你的问题，Enter 发送"
        @keyup.enter.exact.prevent="send"
      />
      <div class="input-actions">
        <div class="hint">已接入腾讯云 API，内容会话保持在当前页面。</div>
        <el-button type="primary" :loading="sending" @click="send">
          <el-icon><Promotion /></el-icon>
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  sending: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['send', 'clear'])

const input = ref('')
const listRef = ref(null)

const scrollToBottom = async () => {
  await nextTick()
  const el = listRef.value
  if (el) el.scrollTop = el.scrollHeight
}

watch(
  () => props.messages,
  async () => {
    await scrollToBottom()
  },
  { deep: true }
)

const formatContent = (text) => {
  const safe = String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
  return safe.replace(/\n/g, '<br/>')
}

const send = async () => {
  const content = input.value.trim()
  if (!content) return
  emit('send', content)
  input.value = ''
}
</script>

<style scoped>
.chat-shell { background: radial-gradient(circle at 20% 20%, #1f2937, #0f172a); border-radius: 14px; padding: 14px; color: #e5e7eb; min-height: calc(100vh - 140px); display: flex; flex-direction: column; box-shadow: 0 20px 80px rgba(0,0,0,0.35); }
.chat-header { display: flex; align-items: center; justify-content: space-between; padding: 6px 6px 10px; border-bottom: 1px solid rgba(255,255,255,0.08); }
.title { display: flex; align-items: center; gap: 10px; }
.title-text .primary { font-size: 16px; font-weight: 600; }
.title-text .sub { font-size: 12px; color: #9ca3af; }
.header-actions { display: flex; gap: 6px; }
.chat-body { flex: 1; overflow-y: auto; padding: 10px 6px; display: flex; flex-direction: column; gap: 10px; }
.msg-row { display: flex; gap: 10px; }
.msg-row.me { flex-direction: row-reverse; }
.bubble { max-width: 80%; padding: 12px 14px; border-radius: 14px; line-height: 1.6; background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.06); color: #e5e7eb; }
.msg-row.me .bubble { background: linear-gradient(120deg, #22d3ee, #818cf8); border: none; color: #0b1021; font-weight: 600; }
.avatar .el-avatar.user { background: #22d3ee; color: #0b1021; }
.avatar .el-avatar.assistant { background: #111827; color: #e5e7eb; border: 1px solid rgba(255,255,255,0.08); }
.typing { color: #9ca3af; font-size: 13px; padding-left: 4px; }
.chat-input { display: flex; flex-direction: column; gap: 8px; padding-top: 8px; border-top: 1px solid rgba(255,255,255,0.08); }
.input-actions { display: flex; align-items: center; justify-content: space-between; color: #9ca3af; font-size: 12px; }
.input-actions .hint { max-width: 70%; }
</style>
