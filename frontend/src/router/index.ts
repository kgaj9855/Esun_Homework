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
router.beforeEach(async to => {
  try { await loadSession() } catch { /* The page shows a useful connection error. */ }
  const publicPage = ['/login', '/register'].includes(to.path)
  if (!session.user && !publicPage) return '/login'
  if (session.user && publicPage) return '/products'
})

export default router
