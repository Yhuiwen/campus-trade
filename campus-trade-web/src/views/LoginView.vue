<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const form = reactive({ username: '', password: '' })
const loading = ref(false)
const router = useRouter()
const route = useRoute()
const store = useUserStore()
const submit = async () => {
  loading.value = true
  try {
    await store.login(form)
    ElMessage.success('欢迎回来')
    router.push(route.query.redirect || '/')
  } finally { loading.value = false }
}
</script>

<template>
  <div class="auth-shell">
    <el-card class="auth-card">
      <h1>登录校园集市</h1><p class="muted">继续发现校园里的好物</p>
      <el-form label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名"><el-input v-model="form.username" size="large" placeholder="请输入用户名" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password size="large" /></el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="submit">登录</el-button>
      </el-form>
      <p style="text-align:center">还没有账号？<el-link type="primary" @click="$router.push('/register')">立即注册</el-link></p>
    </el-card>
  </div>
</template>
