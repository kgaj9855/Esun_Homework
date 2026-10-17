<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api, message, type User } from '@/services/api'
import { session } from '@/services/session'
const route = useRoute(), router = useRouter()
const register = computed(() => route.path === '/register')
const form = reactive({ userName: '', email: '', account: '', password: '', confirm: '' })
const busy = ref(false), error = ref(''), success = ref('')
async function submit() {
  error.value = ''; success.value = ''
  if (register.value && form.password !== form.confirm) { error.value = '兩次密碼輸入不一致'; return }
  if (new TextEncoder().encode(form.password).length > 72) { error.value = '密碼 UTF-8 長度不得超過 72 bytes'; return }
  busy.value = true
  try {
    if (register.value) {
      await api('/auth/register', 'POST', { userName: form.userName, email: form.email, account: form.account, password: form.password })
      form.password = ''; form.confirm = ''
      await router.push('/login'); success.value = '註冊成功，請使用 Email 登入。'
    } else {
      session.user = await api<User>('/auth/login', 'POST', { email: form.email, password: form.password })
      session.ready = true; await router.push('/products')
    }
  } catch (e) { error.value = message(e) } finally { busy.value = false }
}
</script>
<template>
  <div class="auth-layout" :class="{ 'register-layout': register }">
    <section v-if="!register" class="auth-story">
      <span class="eyebrow">YOUR FINANCIAL JOURNEY</span>
      <h1>收藏喜好，<br>為未來多想一步<span>。</span></h1>
      <p>把感興趣的金融商品整理在一起。<br>從每一次比較，找到更適合自己的選擇。</p>
      <div class="orbit-art" aria-hidden="true"><div class="orbit o1"></div><div class="orbit o2"></div><div class="orbit o3"></div><span class="orbit-center">♡</span><span class="orbit-dot"></span><div class="art-label">每個選擇，都有自己的方向 ↗</div></div>
      <div class="story-foot"><span class="small-shield">◇</span> 個人喜好管理 · 安全 Session 驗證</div>
    </section>
    <section class="auth-panel">
      <span class="section-kicker">金融商品喜好系統</span>
      <h2>{{ register ? '建立你的專屬清單' : '歡迎回來' }}</h2>
      <p class="muted">{{ register ? '完成註冊，開始探索與收藏金融商品。' : '登入帳戶，繼續探索你的金融喜好。' }}</p>
      <div v-if="error" class="alert error" role="alert">{{ error }}</div>
      <div v-if="success" class="alert success" role="status">{{ success }}</div>
      <form @submit.prevent="submit" class="auth-form">
        <template v-if="register">
          <label>姓名<input v-model="form.userName" required maxlength="100" autocomplete="name" placeholder="請輸入姓名"></label>
          <label>扣款帳號<input v-model="form.account" required pattern="[0-9]{10,30}" maxlength="30" inputmode="numeric" placeholder="10–30 位數字"><small>僅用於喜好試算，不會進行實際扣款。</small></label>
        </template>
        <label>Email<input v-model="form.email" type="email" required maxlength="255" autocomplete="email" placeholder="name@example.com"></label>
        <label>密碼<input v-model="form.password" type="password" required :minlength="register ? 10 : 1" maxlength="72" :autocomplete="register ? 'new-password' : 'current-password'" :placeholder="register ? '至少 10 個字元' : '請輸入密碼'"></label>
        <label v-if="register">確認密碼<input v-model="form.confirm" type="password" required maxlength="72" autocomplete="new-password" placeholder="再次輸入密碼"></label>
        <button class="button primary full" :disabled="busy">{{ busy ? '處理中…' : register ? '建立帳戶' : '登入帳戶' }} <span>→</span></button>
      </form>
      <p class="auth-switch">{{ register ? '已經有帳戶？' : '還沒有帳戶？' }} <RouterLink :to="register ? '/login' : '/register'">{{ register ? '立即登入' : '立即註冊' }} ↗</RouterLink></p>
      <div class="auth-note">此為金融商品喜好紀錄系統。示範商品與金額僅供功能展示，收藏不代表申購。</div>
    </section>
  </div>
</template>

