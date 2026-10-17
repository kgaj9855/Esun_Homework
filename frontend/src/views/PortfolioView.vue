<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { api, message, money, rate } from '@/services/api'
import LikeDialog from '@/components/LikeDialog.vue'
import DeleteDialog from '@/components/DeleteDialog.vue'

const route = useRoute()
const isLikes = computed(() => route.path === '/likes')
const products = ref([])
const likes = ref([])
const loading = ref(false)
const error = ref('')
const notice = ref('')
const query = ref('')
const sort = ref('default')
const modal = ref(null)
const deleting = ref(null)
const busy = ref(false)

const total = computed(() => {
  let amount = 0
  for (const item of likes.value) {
    amount += Number(item.totalAmount)
  }
  return amount
})

const filteredProducts = computed(() => {
  const result = products.value.filter((product) =>
    product.productName.includes(query.value.trim()),
  )
  if (sort.value === 'price') {
    result.sort((a, b) => a.price - b.price)
  } else if (sort.value === 'fee') {
    result.sort((a, b) => a.feeRate - b.feeRate)
  }
  return result
})
const filteredLikes = computed(() =>
  likes.value.filter((item) => item.productName.includes(query.value.trim())),
)

async function load() {
  loading.value = true
  error.value = ''
  try {
    products.value = await api('/products')
    likes.value = await api('/likes')
  } catch (e) {
    error.value = message(e)
  } finally {
    loading.value = false
  }
}

async function saved() {
  modal.value = null
  notice.value = '喜好清單已更新'
  await load()
}

async function remove() {
  if (deleting.value === null) {
    return
  }
  busy.value = true
  error.value = ''
  try {
    await api('/likes/' + deleting.value.sn, 'DELETE')
    deleting.value = null
    notice.value = '已刪除收藏'
    await load()
  } catch (e) {
    error.value = message(e)
    deleting.value = null
  } finally {
    busy.value = false
  }
}

onMounted(load)
</script>

<template>
  <section>
    <h2>{{ isLikes ? '我的喜好' : '商品列表' }}</h2>
    <p>已收藏 {{ likes.length }} 筆，預計扣款總金額：{{ money(total) }}</p>
    <p v-if="error" class="error" role="alert">
      {{ error }} <button @click="load">重新載入</button>
    </p>
    <p v-if="notice" class="success" role="status">{{ notice }}</p>
    <div class="toolbar">
      <label>搜尋商品<input v-model="query" type="search" aria-label="搜尋商品名稱" /></label>
      <label v-if="!isLikes"
        >排序<select v-model="sort">
          <option value="default">預設排序</option>
          <option value="price">單價由低至高</option>
          <option value="fee">手續費率由低至高</option>
        </select></label
      >
    </div>
    <p v-if="loading" role="status">載入中…</p>
    <template v-else-if="!error">
      <div v-if="!isLikes" class="product-list">
        <article v-for="product in filteredProducts" :key="product.no" class="product-card">
          <h3>{{ product.productName }}</h3>
          <p>商品編號：{{ product.no }}</p>
          <p>單價：{{ money(product.price) }}</p>
          <p>手續費率：{{ rate(product.feeRate) }}</p>
          <button @click="modal = { productNo: product.no }">加入喜好</button>
        </article>
        <p v-if="filteredProducts.length === 0">沒有符合的商品。</p>
      </div>
      <div v-else-if="filteredLikes.length" class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>商品</th>
              <th>單價／費率</th>
              <th>數量</th>
              <th>扣款帳號／Email</th>
              <th>預計扣款總額</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in filteredLikes" :key="item.sn">
              <td>{{ item.productName }}</td>
              <td>
                {{ money(item.price) }}<small>{{ rate(item.feeRate) }}</small>
              </td>
              <td>{{ item.purchaseQuantity }}</td>
              <td>
                {{ item.account }}<small>{{ item.email }}</small>
              </td>
              <td class="total-cell">
                {{ money(item.totalAmount) }}<small>手續費 {{ money(item.totalFee) }}</small>
              </td>
              <td>
                <button
                  :aria-label="'編輯' + item.productName"
                  @click="modal = { productNo: item.productNo, existing: item }"
                >
                  編輯
                </button>
                <button :aria-label="'刪除' + item.productName" @click="deleting = item">
                  刪除
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <p v-else>目前沒有符合的收藏。</p>
    </template>
    <p class="note">商品與金額僅供展示，不會實際申購或扣款。</p>
    <LikeDialog
      v-if="modal"
      :products="products"
      :product-no="modal.productNo"
      :existing="modal.existing"
      @close="modal = null"
      @saved="saved"
    />
    <DeleteDialog
      v-if="deleting"
      :item="deleting"
      :busy="busy"
      @close="deleting = null"
      @confirm="remove"
    />
  </section>
</template>
