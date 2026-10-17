<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { api, message } from '@/services/api'
import { session } from '@/services/session'

const router = useRouter()
const error = ref('')
const busy = ref(false)

async function logout() {
  busy.value = true
  error.value = ''
  try {
    await api('/auth/logout', 'POST')
    session.user = null
    await router.push('/login')
  } catch (e) {
    error.value = message(e)
  } finally {
    busy.value = false
  }
}

function expired() {
  session.user = null
  error.value = '登入已逾時，請重新登入'
  router.push('/login')
}

onMounted(() => window.addEventListener('session-expired', expired))
onUnmounted(() => window.removeEventListener('session-expired', expired))
</script>

<template>
  <header>
    <h1>金融商品喜好系統</h1>
    <nav v-if="session.user">
      <RouterLink to="/products">商品列表</RouterLink>
      <RouterLink to="/likes">我的喜好</RouterLink>
      <span>{{ session.user.userName }}</span>
      <button :disabled="busy" @click="logout">登出</button>
    </nav>
  </header>
  <main>
    <p v-if="error" class="error" role="alert">{{ error }}</p>
    <RouterView />
  </main>
</template>
