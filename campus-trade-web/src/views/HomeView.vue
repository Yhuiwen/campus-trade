<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories, getGoodsPage, getHotGoods } from '../api/goods'
import { getRecommendGoods } from '../api/agent'
import { useUserStore } from '../stores/user'

const router = useRouter()
const store = useUserStore()
const categories = ref([])
const goods = ref([])
const hot = ref([])
const recommend = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 12, keyword: '', categoryId: null })
const statusText = { ON_SALE: '在售', PENDING: '待审核', LOCKED: '交易中', SOLD: '已售出', OFF_SHELF: '已下架', REJECTED: '已驳回' }
const load = async () => {
  const data = await getGoodsPage(query)
  goods.value = data.records
  total.value = data.total
}
onMounted(async () => {
  ;[categories.value, hot.value] = await Promise.all([getCategories(), getHotGoods()])
  if (store.isLogin) recommend.value = await getRecommendGoods()
  await load()
})
</script>

<template>
  <div class="page">
    <section class="hero">
      <el-tag effect="dark" round color="#06b6d4">校园信用交易</el-tag>
      <h1>好物不闲置，信用有价值</h1>
      <p>教材、数码、生活用品，在同一所校园里找到它们的新主人。</p>
      <div style="display:flex;width:620px;margin-top:25px">
        <el-input v-model="query.keyword" size="large" clearable placeholder="搜索想要的商品" @keyup.enter="load" />
        <el-button type="primary" size="large" @click="load">搜索</el-button>
      </div>
    </section>

    <div class="section-title"><h2>智能推荐</h2><span class="muted">基于收藏、浏览分类与平台热度</span></div>
    <div v-if="store.isLogin && recommend.length" class="goods-grid">
      <el-card v-for="item in recommend" :key="item.id" class="goods-card" :body-style="{padding:'12px'}" @click="router.push(`/goods/${item.id}`)">
        <img class="goods-image" :src="item.imageUrl || 'https://placehold.co/500x360?text=Campus+Market'">
        <div class="goods-body"><h3>{{ item.title }}</h3><span class="price">¥{{ item.price }}</span>
          <div class="card-meta"><span>{{ item.recommendationReason }}</span><el-tag type="primary" size="small">Agent</el-tag></div>
        </div>
      </el-card>
    </div>
    <el-card v-else-if="!store.isLogin" shadow="never" class="agent-login-card">
      <div><b>登录后获得个性化商品推荐</b><p class="muted">Agent 会参考你的收藏和最近浏览分类。</p></div>
      <el-button type="primary" @click="router.push('/login')">登录体验</el-button>
    </el-card>

    <div class="section-title"><h2>本周热门</h2><span class="muted">大家最近都在看</span></div>
    <div class="goods-grid">
      <el-card v-for="item in hot.slice(0,4)" :key="item.id" class="goods-card" :body-style="{padding:'12px'}" @click="router.push(`/goods/${item.id}`)">
        <img class="goods-image" :src="item.imageUrl || 'https://placehold.co/500x360?text=Campus+Market'">
        <div class="goods-body"><h3>{{ item.title }}</h3><span class="price">¥{{ item.price }}</span>
          <div class="card-meta"><span>{{ item.sellerNickname }} · 信用 {{ item.sellerCreditScore }}</span><el-tag size="small">{{ statusText[item.status] }}</el-tag></div>
        </div>
      </el-card>
    </div>

    <div class="section-title"><h2>发现好物</h2></div>
    <div class="toolbar">
      <el-select v-model="query.categoryId" clearable placeholder="全部分类" @change="load">
        <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <span class="muted">共 {{ total }} 件在售商品</span>
    </div>
    <div class="goods-grid">
      <el-card v-for="item in goods" :key="item.id" class="goods-card" :body-style="{padding:'12px'}" @click="router.push(`/goods/${item.id}`)">
        <img class="goods-image" :src="item.imageUrl || 'https://placehold.co/500x360?text=Campus+Market'">
        <div class="goods-body"><h3>{{ item.title }}</h3><span class="price">¥{{ item.price }}</span>
          <div class="card-meta"><span>{{ item.categoryName }} · 信用 {{ item.sellerCreditScore }}</span><el-tag type="success" size="small">在售</el-tag></div>
        </div>
      </el-card>
    </div>
    <el-pagination v-if="total > query.size" v-model:current-page="query.current" style="justify-content:center;margin-top:30px" background layout="prev, pager, next" :total="total" :page-size="query.size" @current-change="load" />
  </div>
</template>
