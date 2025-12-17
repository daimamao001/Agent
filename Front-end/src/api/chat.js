import axios from 'axios'

const chatClient = axios.create({
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json'
  }
})

chatClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

chatClient.interceptors.response.use(
  (response) => response.data,
  (error) => Promise.reject(error)
)

export async function sendChat(messages, options = {}) {
  const payload = { messages, ...options }
  const res = await chatClient.post('/api/chat', payload)
  // 兼容 ApiResponse 包装
  if (res && res.code === 200) return res.data
  return res
}
