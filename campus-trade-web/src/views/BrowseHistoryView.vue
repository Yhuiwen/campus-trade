<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getMyHistory } from '../api/history'

const router = useRouter()
const goods = ref([])
onMounted(async () => { goods.value = await getMyHistory() })
</script>

<template>
  <div class="page">
    <div class="section-title"><h2>最近浏览</h2><span class="muted">按最近访问时间排列，最多显示 50 条，便于找回看过的商品</span></div>
    <div class="goods-grid">
      <el-card v-for="item in goods" :key="item.id" class="goods-card" :body-style="{padding:'12px'}" @click="router.push(`/goods/${item.id}`)">
        <img class="goods-image" :src="item.imageUrl || 'https://placehold.co/500x360?text=Campus+Market'">
        <div class="goods-body">
          <h3>{{ item.title }}</h3>
          <span class="price">¥{{ item.price }}</span>
          <div class="card-meta"><span>{{ item.categoryName }}</span><el-tag size="small">{{ item.status }}</el-tag></div>
        </div>
      </el-card>
    </div>
    <el-empty v-if="!goods.length" class="friendly-empty" description="暂无浏览记录，浏览商品后会自动沉淀在这里" />
  </div>
</template>
