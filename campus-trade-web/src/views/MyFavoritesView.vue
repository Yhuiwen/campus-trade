<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyFavorites, removeFavorite } from '../api/favorite'

const router = useRouter()
const goods = ref([])
const load = async () => { goods.value = await getMyFavorites() }
onMounted(load)
const remove = async (event, id) => {
  event.stopPropagation()
  await removeFavorite(id)
  ElMessage.success('已取消收藏')
  load()
}
</script>

<template>
  <div class="page">
    <div class="section-title"><h2>我的收藏</h2><span class="muted">收藏的好物都在这里，方便后续比价和下单</span></div>
    <div class="goods-grid">
      <el-card v-for="item in goods" :key="item.id" class="goods-card" :body-style="{padding:'12px'}" @click="router.push(`/goods/${item.id}`)">
        <img class="goods-image" :src="item.imageUrl || 'https://placehold.co/500x360?text=Campus+Market'">
        <div class="goods-body">
          <h3>{{ item.title }}</h3>
          <span class="price">¥{{ item.price }}</span>
          <div class="card-meta">
            <span>{{ item.sellerNickname }} · 信用 {{ item.sellerCreditScore }}</span>
            <el-button link type="danger" @click="remove($event, item.id)">取消收藏</el-button>
          </div>
        </div>
      </el-card>
    </div>
    <el-empty v-if="!goods.length" class="friendly-empty" description="还没有收藏商品，看到喜欢的商品可以先收藏" />
  </div>
</template>
