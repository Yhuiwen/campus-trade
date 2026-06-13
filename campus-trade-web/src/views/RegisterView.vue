<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../api/auth'

const form = reactive({ username: '', password: '', nickname: '', phone: '', email: '' })
const loading = ref(false)
const router = useRouter()
const submit = async () => {
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally { loading.value = false }
}
</script>

<template>
  <div class="auth-shell">
    <el-card class="auth-card">
      <h1>创建新账号</h1><p class="muted">加入可信、轻松的校园交易社区</p>
      <el-form label-position="top">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" maxlength="100" show-word-limit /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
        </el-row>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="submit">注册</el-button>
      </el-form>
    </el-card>
  </div>
</template>
