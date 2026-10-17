<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { api, message } from '@/services/api'
import { session } from '@/services/session'
import './style.css'
const router = useRouter(), error = ref(''), busy = ref(false)
async function logout() {
  busy.value = true; error.value = ''
  try { await api('/auth/logout', 'POST'); session.user = null; await router.push('/login') }
  catch (e) { error.value = message(e) } finally { busy.value = false }
}
function expired() { session.user = null; error.value = '登入已逾時，請重新登入'; void router.push('/login') }
onMounted(() => window.addEventListener('session-expired', expired))
onUnmounted(() => window.removeEventListener('session-expired', expired))
</script>
<template>
  <header class="site-header"><div class="header-inner">
    <RouterLink class="brand" to="/products" aria-label="金融商品喜好系統首頁"><strong>金融商品喜好系統</strong></RouterLink>
    <nav v-if="session.user" class="main-nav" aria-label="主要導覽"><RouterLink to="/products">商品探索</RouterLink><RouterLink to="/likes">我的喜好</RouterLink></nav>
    <div v-if="session.user" class="user-menu"><span class="avatar">{{ session.user.userName.slice(0,1) }}</span><span class="user-name">{{ session.user.userName }}</span><span class="header-divider"></span><button class="text-button logout" :disabled="busy" @click="logout">{{ busy ? '登出中…' : '登出' }} ↗</button></div><span v-else class="header-tag">你的理財靈感</span>
  </div></header>
  <div v-if="error" class="global-error alert error" role="alert">{{ error }} <button class="text-button" @click="error = ''">關閉</button></div>
  <main><RouterView /></main>
  <footer class="site-footer"><span>© {{ new Date().getFullYear() }} 金融商品喜好系統</span><span>從喜好出發，讓選擇更有方向。</span></footer>
</template>

