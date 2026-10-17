<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api, message } from '@/services/api'
import { session } from '@/services/session'
const route = useRoute()
const router = useRouter()
const register = computed(() => route.path === '/register')
const form = reactive({ userName: '', email: '', account: '', password: '', confirm: '' })
const busy = ref(false)
const error = ref('')
const success = ref('')
async function submit() {
  error.value = ''
  success.value = ''
  if (register.value && form.password !== form.confirm) {
    error.value = '兩次密碼輸入不一致'
    return
  }
  if (new TextEncoder().encode(form.password).length > 72) {
    error.value = '密碼 UTF-8 長度不得超過 72 bytes'
    return
  }
  busy.value = true
  try {
    if (register.value) {
      await api('/auth/register', 'POST', {
        userName: form.userName,
        email: form.email,
        account: form.account,
        password: form.password,
      })
      form.password = ''
      form.confirm = ''
      await router.push('/login')
      success.value = '註冊成功，請使用 Email 登入。'
    } else {
      session.user = await api('/auth/login', 'POST', {
        email: form.email,
        password: form.password,
      })
      session.ready = true
      await router.push('/products')
    }
  } catch (e) {
    error.value = message(e)
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <section class="auth-form">
    <h2>{{ register ? '會員註冊' : '會員登入' }}</h2>
    <p v-if="error" class="error" role="alert">{{ error }}</p>
    <p v-if="success" class="success" role="status">{{ success }}</p>
    <form @submit.prevent="submit">
      <template v-if="register">
        <label
          >姓名<input v-model="form.userName" required maxlength="100" autocomplete="name"
        /></label>
        <label
          >扣款帳號<input
            v-model="form.account"
            required
            pattern="[0-9]{10,30}"
            maxlength="30"
            inputmode="numeric"
          /><small>請輸入 10–30 位數字，僅供展示，不會實際扣款。</small></label
        >
      </template>
      <label
        >Email<input
          v-model="form.email"
          type="email"
          required
          maxlength="255"
          autocomplete="email"
      /></label>
      <label
        >密碼<input
          v-model="form.password"
          aria-label="密碼"
          type="password"
          required
          :minlength="register ? 10 : 1"
          maxlength="72"
          :autocomplete="register ? 'new-password' : 'current-password'"
        /><small v-if="register">至少 10 個字元。</small></label
      >
      <label v-if="register"
        >確認密碼<input
          v-model="form.confirm"
          type="password"
          required
          maxlength="72"
          autocomplete="new-password"
      /></label>
      <button :disabled="busy">{{ busy ? '處理中…' : register ? '註冊' : '登入' }}</button>
    </form>
    <p v-if="register">已有帳戶？<RouterLink to="/login">前往登入</RouterLink></p>
    <p v-else>還沒有帳戶？<RouterLink to="/register">前往註冊</RouterLink></p>
  </section>
</template>
