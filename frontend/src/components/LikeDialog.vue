<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { api, message, money, rate, type Product, type Like } from '@/services/api'
import { session } from '@/services/session'
const props = defineProps<{ products: Product[]; productNo: number; existing?: Like }>()
const emit = defineEmits<{ close: []; saved: [] }>()
const selected = ref(props.existing?.productNo ?? props.productNo)
const quantity = ref(props.existing?.purchaseQuantity ?? 1)
const error = ref(''), busy = ref(false), dialog = ref<HTMLDialogElement>()
const product = computed(() => props.products.find(p => p.no === selected.value))
const subtotal = computed(() => (product.value?.price ?? 0) * (quantity.value || 0))
const fee = computed(() => Math.round((subtotal.value * (product.value?.feeRate ?? 0) + Number.EPSILON) * 100) / 100)
onMounted(() => dialog.value?.showModal())
onUnmounted(() => dialog.value?.close())
async function save() {
  busy.value = true; error.value = ''
  try {
    await api(props.existing ? `/likes/${props.existing.sn}` : '/likes', props.existing ? 'PUT' : 'POST', { productNo: selected.value, purchaseQuantity: quantity.value })
    emit('saved')
  } catch (e) { error.value = message(e) } finally { busy.value = false }
}
</script>
<template>
  <dialog ref="dialog" class="favorite-dialog" aria-labelledby="dialog-title" @cancel.prevent="!busy && emit('close')">
    <div class="dialog-heading"><div><span class="section-kicker">YOUR NEXT CHOICE</span><h2 id="dialog-title">{{ existing ? '編輯喜好商品' : '加入我的喜好' }}</h2></div><button type="button" class="icon-button" aria-label="關閉" :disabled="busy" @click="emit('close')">×</button></div>
    <form @submit.prevent="save">
      <div v-if="error" class="alert error" role="alert">{{ error }}</div>
      <label>金融商品<select v-model="selected" required><option v-for="p in products" :key="p.no" :value="p.no">{{ p.productName }}</option></select></label>
      <div class="quote-line"><span>參考單價 <strong>{{ money(product?.price ?? 0) }}</strong></span><span>手續費率 <strong>{{ rate(product?.feeRate ?? 0) }}</strong></span></div>
      <label>購買數量<input v-model.number="quantity" type="number" min="1" max="1000000" step="1" required></label>
      <label>本人扣款帳號<input :value="session.user?.account" readonly><small>使用註冊帳號，僅作喜好紀錄，不會實際扣款。</small></label>
      <div class="estimate"><div><span>商品小計</span><strong>{{ money(subtotal) }}</strong></div><div><span>預計手續費</span><strong>{{ money(fee) }}</strong></div><div class="estimate-total"><span>預計扣款總金額</span><strong>{{ money(subtotal + fee) }}</strong></div></div>
      <p class="muted fine">此處為預估值，儲存時以伺服器計算結果為準。</p>
      <div class="dialog-actions"><button type="button" class="button secondary" :disabled="busy" @click="emit('close')">取消</button><button class="button primary" :disabled="busy || !product">{{ busy ? '儲存中…' : existing ? '儲存變更' : '確認加入喜好' }}</button></div>
    </form>
  </dialog>
</template>
