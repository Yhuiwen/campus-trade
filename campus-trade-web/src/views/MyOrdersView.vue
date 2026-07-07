<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelOrder, completeOrder, getBuyOrders, getSellOrders } from '../api/order'
import { createReview } from '../api/review'

const active = ref('buy')
const buy = ref([])
const sell = ref([])
const reviewDialog = ref(false)
const review = reactive({ orderId: null, rating: 5, content: '' })
const load = async () => { [buy.value, sell.value] = await Promise.all([getBuyOrders(), getSellOrders()]) }
onMounted(load)
const cancel = async id => { await ElMessageBox.confirm('确定取消该订单？'); await cancelOrder(id); ElMessage.success('订单已取消'); load() }
const complete = async id => { await ElMessageBox.confirm('确认交易已经完成？'); await completeOrder(id); ElMessage.success('交易完成'); load() }
const openReview = order => { review.orderId = order.id; review.rating = 5; review.content = ''; reviewDialog.value = true }
const submitReview = async () => { await createReview(review); ElMessage.success('评价已提交，卖家信用分已更新'); reviewDialog.value = false; load() }
</script>

<template>
  <div class="page panel">
    <div class="section-title compact-title">
      <div>
        <h2>我的订单</h2>
        <span class="muted">查看购买、出售和评价进度，保持交易记录完整。</span>
      </div>
    </div>
    <el-tabs v-model="active">
      <el-tab-pane label="我购买的" name="buy">
        <el-card v-for="item in buy" :key="item.id" class="order-card">
          <div class="order-row"><img class="order-image" :src="item.goodsImageUrl || 'https://placehold.co/150'"><div><b>{{ item.goodsTitle }}</b><p class="muted">订单号 {{ item.orderNo }} · 卖家 {{ item.sellerNickname }}</p><span class="price" style="font-size:18px">¥{{ item.amount }}</span></div><div><el-tag>{{ item.status }}</el-tag><br><el-button v-if="item.status==='CREATED'" link type="danger" @click="cancel(item.id)">取消订单</el-button><el-button v-if="item.status==='COMPLETED' && !item.reviewed" link type="primary" @click="openReview(item)">评价卖家</el-button></div></div>
        </el-card>
        <el-empty v-if="!buy.length" class="friendly-empty" description="暂无购买订单，去首页发现感兴趣的商品吧" />
      </el-tab-pane>
      <el-tab-pane label="我出售的" name="sell">
        <el-card v-for="item in sell" :key="item.id" class="order-card">
          <div class="order-row"><img class="order-image" :src="item.goodsImageUrl || 'https://placehold.co/150'"><div><b>{{ item.goodsTitle }}</b><p class="muted">订单号 {{ item.orderNo }} · 买家 {{ item.buyerNickname }}</p><span class="price" style="font-size:18px">¥{{ item.amount }}</span></div><div><el-tag>{{ item.status }}</el-tag><br><el-button v-if="item.status==='CREATED'" link type="primary" @click="complete(item.id)">确认完成</el-button></div></div>
        </el-card>
        <el-empty v-if="!sell.length" class="friendly-empty" description="暂无出售订单，发布闲置后订单会显示在这里" />
      </el-tab-pane>
    </el-tabs>
    <el-dialog v-model="reviewDialog" title="评价卖家" width="500px">
      <el-form label-position="top"><el-form-item label="评分"><el-rate v-model="review.rating" /></el-form-item><el-form-item label="评价内容"><el-input v-model="review.content" type="textarea" :rows="4" /></el-form-item></el-form>
      <template #footer><el-button @click="reviewDialog=false">取消</el-button><el-button type="primary" @click="submitReview">提交评价</el-button></template>
    </el-dialog>
  </div>
</template>
