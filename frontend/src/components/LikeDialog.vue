<script setup>
import { computed, onMounted, ref } from 'vue'
import { api, message, money, rate } from '@/services/api'
import { session } from '@/services/session'

const props = defineProps({ products: Array, productNo: Number, existing: Object })
const emit = defineEmits(['close', 'saved'])
const selected = ref(props.productNo)
const quantity = ref(1)
const error = ref('')
const busy = ref(false)
const dialog = ref(null)
if (props.existing) {
  quantity.value = props.existing.purchaseQuantity
}

const product = computed(() => props.products.find((item) => item.no === selected.value))
const subtotal = computed(() => {
  if (!product.value) return 0
  return product.value.price * quantity.value
})
const fee = computed(() => {
  if (!product.value) return 0
  return Math.round(subtotal.value * product.value.feeRate * 100) / 100
})

async function save() {
  busy.value = true
  error.value = ''
  let path = '/likes'
  let method = 'POST'
  if (props.existing) {
    path = '/likes/' + props.existing.sn
    method = 'PUT'
  }
  try {
    await api(path, method, { productNo: selected.value, purchaseQuantity: quantity.value })
    emit('saved')
  } catch (e) {
    error.value = message(e)
  } finally {
    busy.value = false
  }
}

function close() {
  if (!busy.value) emit('close')
}
onMounted(() => dialog.value.showModal())
</script>

<template>
  <dialog ref="dialog" aria-labelledby="dialog-title" @cancel.prevent="close">
    <h2 id="dialog-title">{{ existing ? '編輯喜好' : '加入喜好' }}</h2>
    <form @submit.prevent="save">
      <p v-if="error" class="error" role="alert">{{ error }}</p>
      <label
        >金融商品<select v-model="selected" required>
          <option v-for="item in products" :key="item.no" :value="item.no">
            {{ item.productName }}
          </option>
        </select></label
      >
      <p v-if="product">單價：{{ money(product.price) }}，手續費率：{{ rate(product.feeRate) }}</p>
      <label
        >購買數量<input
          v-model.number="quantity"
          type="number"
          min="1"
          max="1000000"
          step="1"
          required
      /></label>
      <label>本人扣款帳號<input :value="session.user.account" readonly /></label>
      <p>預計手續費：{{ money(fee) }}</p>
      <p>預計總金額：{{ money(subtotal + fee) }}</p>
      <small>預估金額以伺服器儲存結果為準。</small>
      <div class="actions">
        <button type="button" :disabled="busy" @click="close">取消</button>
        <button :disabled="busy || !product">
          {{ busy ? '儲存中…' : existing ? '儲存變更' : '確認加入喜好' }}
        </button>
      </div>
    </form>
  </dialog>
</template>
