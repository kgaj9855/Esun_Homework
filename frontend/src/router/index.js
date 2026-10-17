import { createRouter, createWebHashHistory } from 'vue-router'
import { loadSession, session } from '@/services/session'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', redirect: '/products' },
    { path: '/login', component: () => import('@/views/AuthView.vue') },
    { path: '/register', component: () => import('@/views/AuthView.vue') },
    { path: '/products', component: () => import('@/views/PortfolioView.vue') },
    { path: '/likes', component: () => import('@/views/PortfolioView.vue') },
    { path: '/:pathMatch(.*)*', redirect: '/products' },
  ],
})
router.beforeEach(async (to) => {
  try {
    await loadSession()
  } catch {
    /* 連線失敗仍可開啟登入頁，送出表單時顯示錯誤。 */
  }
  const publicPage = ['/login', '/register'].includes(to.path)
  if (!session.user && !publicPage) return '/login'
  if (session.user && publicPage) return '/products'
})

export default router
