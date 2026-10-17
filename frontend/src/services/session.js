import { reactive } from 'vue'
import { api, ApiError } from './api'

export const session = reactive({ user: null, ready: false })

// 重新整理網頁後，詢問後端是否仍保持登入。
export async function loadSession() {
  if (session.ready) {
    return
  }
  try {
    session.user = await api('/auth/me')
  } catch (error) {
    if (!(error instanceof ApiError) || error.status !== 401) {
      throw error
    }
    session.user = null
  }
  session.ready = true
}
