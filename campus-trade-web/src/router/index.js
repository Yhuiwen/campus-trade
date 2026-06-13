import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', component: () => import('../views/HomeView.vue') },
  { path: '/login', component: () => import('../views/LoginView.vue'), meta: { guest: true } },
  { path: '/register', component: () => import('../views/RegisterView.vue'), meta: { guest: true } },
  { path: '/goods/:id', component: () => import('../views/GoodsDetailView.vue') },
  { path: '/publish', component: () => import('../views/PublishView.vue'), meta: { auth: true } },
  { path: '/my/goods', component: () => import('../views/MyGoodsView.vue'), meta: { auth: true } },
  { path: '/my/orders', component: () => import('../views/MyOrdersView.vue'), meta: { auth: true } },
  { path: '/my/favorites', component: () => import('../views/MyFavoritesView.vue'), meta: { auth: true } },
  { path: '/my/history', component: () => import('../views/BrowseHistoryView.vue'), meta: { auth: true } },
  { path: '/agent', component: () => import('../views/AgentView.vue'), meta: { auth: true } },
  { path: '/admin', component: () => import('../views/AdminView.vue'), meta: { auth: true, admin: true } }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach(to => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (to.meta.auth && !token) return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  if (to.meta.admin && user?.role !== 'ADMIN') return '/'
  if (to.meta.guest && token) return '/'
})

export default router
