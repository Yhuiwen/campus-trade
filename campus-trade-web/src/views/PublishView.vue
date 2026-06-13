<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createGoods, getCategories, uploadImage } from '../api/goods'
import { getPublishAdvice } from '../api/agent'

const categories = ref([])
const loading = ref(false)
const adviceLoading = ref(false)
const advice = ref(null)
const router = useRouter()
const form = reactive({ title: '', description: '', categoryId: null, price: null, originalPrice: null, imageUrl: '' })
onMounted(async () => { categories.value = await getCategories() })
const upload = async ({ file }) => { form.imageUrl = (await uploadImage(file)).url }
const getAdvice = async () => {
  if (!form.categoryId) return ElMessage.warning('请先选择商品分类')
  adviceLoading.value = true
  try {
    advice.value = await getPublishAdvice({
      title: form.title,
      description: form.description,
      categoryId: form.categoryId,
      originalPrice: form.originalPrice,
      expectedPrice: form.price
    })
  } finally { adviceLoading.value = false }
}
const applyAdvice = () => {
  form.title = advice.value.optimizedTitle
  form.description = advice.value.optimizedDescription
  form.price = Number(advice.value.suggestedPrice)
  ElMessage.success('已应用智能建议')
}
const submit = async () => {
  loading.value = true
  try {
    await createGoods(form)
    ElMessage.success('发布成功，等待管理员审核')
    router.push('/my/goods')
  } finally { loading.value = false }
}
</script>

<template>
  <div class="form-page panel">
    <h1>发布一件闲置</h1><p class="muted">真实描述物品状况，更容易获得买家信任</p>
    <el-form label-position="top">
      <el-form-item label="商品图片">
        <el-upload :show-file-list="false" :http-request="upload" accept=".jpg,.jpeg,.png">
          <img v-if="form.imageUrl" :src="form.imageUrl" style="width:220px;height:160px;object-fit:cover;border-radius:12px">
          <el-button v-else size="large">选择图片上传</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item label="商品标题"><el-input v-model="form.title" maxlength="100" show-word-limit /></el-form-item>
      <el-form-item label="商品描述"><el-input v-model="form.description" type="textarea" :rows="6" /></el-form-item>
      <el-form-item label="分类"><el-select v-model="form.categoryId" style="width:100%"><el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
      <el-row :gutter="16">
        <el-col :span="12"><el-form-item label="售价"><el-input-number v-model="form.price" :min="0.01" :precision="2" style="width:100%" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="原价"><el-input-number v-model="form.originalPrice" :min="0.01" :precision="2" style="width:100%" /></el-form-item></el-col>
      </el-row>
      <el-button plain type="primary" size="large" style="width:100%;margin-bottom:14px" :loading="adviceLoading" @click="getAdvice">
        智能生成发布建议
      </el-button>
      <el-card v-if="advice" class="agent-advice-card" shadow="never">
        <template #header><div style="display:flex;justify-content:space-between"><b>Agent 发布建议</b><el-tag :type="advice.canPublish ? 'success' : 'warning'">{{ advice.canPublish ? '建议发布' : '建议补充' }}</el-tag></div></template>
        <p><b>推荐标题：</b>{{ advice.optimizedTitle }}</p>
        <p><b>推荐价格：</b>¥{{ advice.suggestedPrice }}（{{ advice.priceRange }}）</p>
        <p><b>推荐描述：</b></p><div class="advice-description">{{ advice.optimizedDescription }}</div>
        <ul><li v-for="item in advice.adviceList" :key="item">{{ item }}</li></ul>
        <el-button type="primary" @click="applyAdvice">一键应用建议</el-button>
      </el-card>
      <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="submit">提交审核</el-button>
    </el-form>
  </div>
</template>
