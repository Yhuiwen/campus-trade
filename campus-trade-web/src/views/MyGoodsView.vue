<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategories, getMyGoods, offShelfGoods, updateGoods } from '../api/goods'

const goods = ref([])
const categories = ref([])
const dialog = ref(false)
const form = reactive({ id: null, title: '', description: '', categoryId: null, price: 0, originalPrice: 0, imageUrl: '' })
const statusText = { ON_SALE: '在售', PENDING: '待审核', LOCKED: '交易中', SOLD: '已售出', OFF_SHELF: '已下架', REJECTED: '已驳回' }
const load = async () => { goods.value = (await getMyGoods({ size: 100 })).records }
onMounted(async () => { categories.value = await getCategories(); await load() })
const edit = item => { Object.assign(form, item); dialog.value = true }
const save = async () => {
  await updateGoods(form.id, form)
  ElMessage.success('修改成功，商品已重新进入审核')
  dialog.value = false
  load()
}
const offShelf = async id => {
  await ElMessageBox.confirm('确定下架这件商品？')
  await offShelfGoods(id)
  ElMessage.success('已下架')
  load()
}
</script>

<template>
  <div class="page panel">
    <div class="section-title"><h2>我的商品</h2><el-button type="primary" @click="$router.push('/publish')">发布新商品</el-button></div>
    <el-table :data="goods">
      <el-table-column label="商品" min-width="280">
        <template #default="{row}"><div style="display:flex;align-items:center;gap:12px"><img :src="row.imageUrl || 'https://placehold.co/100'" style="width:70px;height:55px;object-fit:cover;border-radius:8px"><b>{{ row.title }}</b></div></template>
      </el-table-column>
      <el-table-column prop="price" label="售价"><template #default="{row}">¥{{ row.price }}</template></el-table-column>
      <el-table-column prop="status" label="状态"><template #default="{row}"><el-tag>{{ statusText[row.status] || row.status }}</el-tag></template></el-table-column>
      <el-table-column prop="viewCount" label="浏览量" />
      <el-table-column label="操作" width="190">
        <template #default="{row}"><el-button link type="primary" :disabled="row.status === 'SOLD'" @click="edit(row)">编辑</el-button><el-button link type="danger" :disabled="['SOLD','OFF_SHELF'].includes(row.status)" @click="offShelf(row.id)">下架</el-button></template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!goods.length" description="你还没有发布商品" />

    <el-dialog v-model="dialog" title="编辑商品" width="600px">
      <el-form label-position="top">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="分类"><el-select v-model="form.categoryId" style="width:100%"><el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
        <el-row :gutter="12"><el-col :span="12"><el-form-item label="售价"><el-input-number v-model="form.price" :min=".01" :precision="2" /></el-form-item></el-col><el-col :span="12"><el-form-item label="原价"><el-input-number v-model="form.originalPrice" :min=".01" :precision="2" /></el-form-item></el-col></el-row>
      </el-form>
      <template #footer><el-button @click="dialog=false">取消</el-button><el-button type="primary" @click="save">保存并重新审核</el-button></template>
    </el-dialog>
  </div>
</template>
