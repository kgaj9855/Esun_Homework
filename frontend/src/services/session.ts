import { reactive } from 'vue'
import { api, ApiError, type User } from './api'
export const session = reactive({ user: null as User | null, ready: false })
let loading: Promise<void> | null = null
export async function loadSession() {
  if (session.ready) return
  loading ??= (async () => {
    try { session.user = await api<User>('/auth/me') }
    catch (e) { if (!(e instanceof ApiError && e.status === 401)) throw e }
    finally { loading = null }
    session.ready = true
  })()
  await loading
}
