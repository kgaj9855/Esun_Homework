<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { api, message, money, rate, type Product, type Like } from '@/services/api'
import { session } from '@/services/session'
import LikeDialog from '@/components/LikeDialog.vue'
import DeleteDialog from '@/components/DeleteDialog.vue'
const route = useRoute()
const isLikes = computed(() => route.path === '/likes')
const products = ref<Product[]>([]), likes = ref<Like[]>([])
const loading = ref(true), error = ref(''), notice = ref(''), query = ref(''), sort = ref('default')
const modal = ref<{ productNo: number; existing?: Like } | null>(null)
const deleting = ref<Like | null>(null), busy = ref(false)
const total = computed(() => likes.value.reduce((sum, l) => sum + Number(l.totalAmount), 0))
const fees = computed(() => likes.value.reduce((sum, l) => sum + Number(l.totalFee), 0))
const filtered = computed(() => {
  const rows = products.value.filter(p => p.productName.includes(query.value.trim()))
  return sort.value === 'price' ? rows.sort((a,b) => a.price - b.price) : sort.value === 'fee' ? rows.sort((a,b) => a.feeRate - b.feeRate) : rows
})
const filteredLikes = computed(() => likes.value.filter(p => p.productName.includes(query.value.trim())))
async function load() {
  loading.value = true; error.value = ''
  try { const result = await Promise.all([api<Product[]>('/products'), api<Like[]>('/likes')]); products.value = result[0]; likes.value = result[1] }
  catch (e) { error.value = message(e) } finally { loading.value = false }
}
onMounted(load)
async function saved() { modal.value = null; notice.value = '喜好清單已更新'; await load() }
async function remove() {
  if (!deleting.value) return
  busy.value = true; error.value = ''
  try { await api(`/likes/${deleting.value.sn}`, 'DELETE'); deleting.value = null; notice.value = '已移除喜好商品'; await load() }
  catch (e) { error.value = message(e); deleting.value = null } finally { busy.value = false }
}
const categories = (name: string) => name.includes('債券') ? '債券基金' : name.includes('多元') ? '多元資產' : '股票基金'
</script>
<template>
  <div class="workspace">
    <div class="breadcrumb">個人金融 <span>/</span> {{ isLikes ? '我的喜好' : '商品探索' }}</div>
    <section class="page-heading"><div><div class="section-kicker">{{ isLikes ? 'MY COLLECTION' : 'EXPLORE YOUR POSSIBILITIES' }}</div><h1>{{ isLikes ? '我的喜好清單' : '探索適合你的金融商品' }}<span class="heading-dot">.</span></h1><p class="muted">{{ isLikes ? '每一份收藏，都是對未來的一份想像。隨時調整，從容規劃。' : `${session.user?.userName ?? ''}，從感興趣的商品開始，慢慢構築你的理財藍圖。` }}</p></div><RouterLink v-if="isLikes" class="button primary" to="/products">＋ 探索更多商品</RouterLink><div v-else class="page-badge"><span class="status-dot"></span> 個人專屬空間</div></section>
    <section class="summary-grid" aria-label="喜好清單摘要">
      <article class="summary-card"><div class="summary-top"><span>已收藏商品</span><span class="summary-symbol">♡</span></div><div class="summary-number">{{ loading ? '—' : likes.length }}<small>筆</small></div><div class="summary-caption">讓每一份喜好都有跡可循</div></article>
      <article class="summary-card"><div class="summary-top"><span>預計扣款總金額</span><span class="summary-symbol">↗</span></div><div class="summary-number amount">{{ loading ? '—' : money(total) }}</div><div class="summary-caption">TWD · 含預計手續費 {{ money(fees) }}</div></article>
      <article class="summary-card accent"><div class="summary-top"><span>好選擇，從了解開始</span><span class="summary-symbol">✧</span></div><p>先收藏，再比較。<br>找到屬於自己的理財步調。</p><RouterLink :to="isLikes ? '/products' : '/likes'">{{ isLikes ? '繼續探索' : '查看我的喜好' }} <span>→</span></RouterLink></article>
    </section>
    <div v-if="error" class="alert error" role="alert">{{ error }} <button class="text-button" @click="load">重新載入</button></div>
    <div v-if="notice" class="alert success" role="status">✓ {{ notice }}<button class="text-button" @click="notice = ''" aria-label="關閉通知">×</button></div>
    <section class="catalog">
      <div class="catalog-heading"><div><h2>{{ isLikes ? '收藏的金融商品' : '精選金融商品' }} <span class="count-pill">{{ isLikes ? likes.length : products.length }}</span></h2><p class="muted">{{ isLikes ? '管理商品、數量與預估費用，清楚掌握每個選擇。' : '一次瀏覽參考單價與手續費，收藏你感興趣的選擇。' }}</p></div></div>
      <div class="toolbar"><label class="search-field"><span aria-hidden="true">⌕</span><input v-model="query" type="search" placeholder="搜尋商品名稱…" aria-label="搜尋商品名稱"></label><label v-if="!isLikes" class="sort-field">排序<select v-model="sort"><option value="default">預設排序</option><option value="price">單價由低至高</option><option value="fee">手續費率由低至高</option></select></label><span v-else class="muted fine">共 {{ filteredLikes.length }} 筆收藏</span></div>
      <div v-if="loading" class="empty-state" role="status"><div class="loader"></div><h3>正在整理你的金融商品…</h3></div>
      <template v-else-if="!error">
        <div v-if="!isLikes && filtered.length" class="product-grid">
          <article v-for="(p, i) in filtered" :key="p.no" class="product-card">
            <div class="product-top"><span class="product-icon" :class="'tone-' + i % 3">{{ p.productName.includes('債券') ? '▥' : p.productName.includes('多元') ? '◈' : '↗' }}</span><span class="product-type">{{ categories(p.productName) }}</span><span v-if="likes.some(l => l.productNo === p.no)" class="saved-badge">♥ 已收藏</span></div>
            <h3>{{ p.productName }}</h3><p class="product-code">商品編號 · {{ String(p.no).padStart(4, '0') }}</p>
            <div class="product-price"><span>參考單價 <small>TWD</small></span><strong>{{ money(p.price) }}</strong></div>
            <div class="product-bottom"><span>手續費率 <strong>{{ rate(p.feeRate) }}</strong></span><button class="button add-button" @click="modal = { productNo: p.no }"><span>＋</span> 加入喜好</button></div>
          </article>
        </div>
        <div v-else-if="isLikes && filteredLikes.length" class="table-wrap"><table><thead><tr><th>金融商品</th><th>單價／費率</th><th>數量</th><th>扣款帳號／Email</th><th class="numeric">預計扣款總額</th><th>操作</th></tr></thead><tbody><tr v-for="l in filteredLikes" :key="l.sn"><td><strong>{{ l.productName }}</strong><small>收藏編號 #{{ String(l.sn).padStart(4, '0') }}</small></td><td>{{ money(l.price) }}<small>手續費率 {{ rate(l.feeRate) }}</small></td><td>{{ l.purchaseQuantity.toLocaleString() }}</td><td class="account-cell">{{ l.account }}<small>{{ l.email }}</small></td><td class="numeric total-cell">{{ money(l.totalAmount) }}<small>含手續費 {{ money(l.totalFee) }}</small></td><td><div class="row-actions"><button class="text-button" @click="modal = { productNo: l.productNo, existing: l }" :aria-label="`編輯${l.productName}`">編輯</button><button class="text-button danger" @click="deleting = l" :aria-label="`刪除${l.productName}`">刪除</button></div></td></tr></tbody></table></div>
        <div v-else class="empty-state"><div class="empty-icon">♡</div><h3>{{ query ? '沒有符合的商品' : isLikes ? '你的下一個喜好，從這裡開始' : '目前尚無商品' }}</h3><p class="muted">{{ query ? '試試其他關鍵字，探索更多可能。' : isLikes ? '瀏覽金融商品，將感興趣的選擇加入專屬清單。' : '商品上架後會顯示於此。' }}</p><RouterLink v-if="isLikes && !query" to="/products" class="button primary">探索金融商品 →</RouterLink></div>
      </template>
    </section>
    <div class="disclosure"><span>ⓘ</span><p>本系統提供金融商品喜好紀錄與費用試算，無實際交易或扣款功能。所有商品與價格皆為示範資料，非即時報價或投資建議。</p></div>
    <LikeDialog v-if="modal" :products="products" :product-no="modal.productNo" :existing="modal.existing" @close="modal = null" @saved="saved" />
    <DeleteDialog v-if="deleting" :item="deleting" :busy="busy" @close="deleting = null" @confirm="remove" />
  </div>
</template>

