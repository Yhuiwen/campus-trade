<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGoods } from '../api/goods'
import { createOrder } from '../api/order'
import { addFavorite, removeFavorite } from '../api/favorite'
import { getGoodsRisk } from '../api/agent'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const goods = ref({})
const risk = ref(null)
onMounted(async () => {
  goods.value = await getGoods(route.params.id)
  if (store.isLogin) risk.value = await getGoodsRisk(route.params.id)
})
const riskType = level => ({ LOW: 'success', MEDIUM: 'warning', HIGH: 'danger' }[level] || 'info')
const statusText = { ON_SALE: '在售', PENDING: '待审核', LOCKED: '交易中', SOLD: '已售出', OFF_SHELF: '已下架', REJECTED: '已驳回' }
const canBuy = status => status === 'ON_SALE'
const buy = async () => {
  if (!store.isLogin) return router.push(`/login?redirect=${route.fullPath}`)
  await ElMessageBox.confirm('确认购买该商品并创建订单？', '确认下单')
  await createOrder(goods.value.id)
  ElMessage.success('下单成功，请与卖家联系')
  router.push('/my/orders')
}
const toggleFavorite = async () => {
  if (!store.isLogin) return router.push(`/login?redirect=${route.fullPath}`)
  if (goods.value.favorited) {
    await removeFavorite(goods.value.id)
    goods.value.favorited = false
    ElMessage.success('已取消收藏')
  } else {
    await addFavorite(goods.value.id)
    goods.value.favorited = true
    ElMessage.success('收藏成功')
  }
}
</script>

<template>
  <div class="page panel detail-layout">
    <img class="detail-image" :src="goods.imageUrl || 'https://placehold.co/700x600?text=Campus+Market'">
    <div class="detail-info">
      <el-tag>{{ goods.categoryName }}</el-tag>
      <h1>{{ goods.title }}</h1>
      <p class="muted">浏览 {{ goods.viewCount }} 次</p>
      <div class="detail-price">¥{{ goods.price }} <del class="muted" style="font-size:16px">¥{{ goods.originalPrice }}</del></div>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="卖家">{{ goods.sellerNickname }}</el-descriptions-item>
        <el-descriptions-item label="信用分"><el-rate :model-value="goods.sellerCreditScore / 30" disabled show-score /></el-descriptions-item>
        <el-descriptions-item label="商品状态">{{ statusText[goods.status] || goods.status }}</el-descriptions-item>
      </el-descriptions>
      <h3>商品描述</h3><p style="line-height:1.8;white-space:pre-wrap">{{ goods.description }}</p>
      <el-card class="risk-card" shadow="never">
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <b>智能交易风险分析</b>
            <el-tag v-if="risk" :type="riskType(risk.riskLevel)" effect="dark">{{ risk.riskLevel }} · {{ risk.score }}分</el-tag>
          </div>
        </template>
        <template v-if="risk">
          <p style="font-weight:600">{{ risk.advice }}</p>
          <ul><li v-for="item in risk.reasons" :key="item">{{ item }}</li></ul>
        </template>
        <el-empty v-else :image-size="50" description="登录后查看个性化风险分析">
          <el-button type="primary" link @click="router.push(`/login?redirect=${route.fullPath}`)">立即登录</el-button>
        </el-empty>
      </el-card>
      <div style="display:flex;gap:12px;margin-top:25px">
        <el-button size="large" style="width:150px" :type="goods.favorited ? 'warning' : 'default'" @click="toggleFavorite">
          {{ goods.favorited ? '已收藏' : '收藏商品' }}
        </el-button>
        <el-button type="primary" size="large" style="flex:1" :disabled="!canBuy(goods.status) || goods.sellerId === store.user?.id" @click="buy">
          {{ goods.sellerId === store.user?.id ? '这是你的商品' : goods.status === 'LOCKED' ? '交易中' : '立即购买' }}
        </el-button>
      </div>
    </div>
  </div>
</template>
