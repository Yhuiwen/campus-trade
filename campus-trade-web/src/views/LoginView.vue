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
const demoAccounts = [
  ['管理员', 'admin', '123456'],
  ['用户', 'student1', '123456'],
  ['用户', 'student2', '123456']
]

const submit = async () => {
  loading.value = true
  try {
    await store.login(form)
    ElMessage.success('欢迎回来')
    router.push(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-layout">
    <section class="auth-intro">
      <div class="brand-lockup">
        <span class="brand-mark">C</span>
        <strong>校园集市</strong>
      </div>
      <h1>校园二手交易与信用评价平台</h1>
      <p>支持商品发布、审核、下单、评价、收藏、浏览历史与智能交易助手。</p>
      <div class="auth-feature-grid">
        <span>信用评价</span>
        <span>安全交易</span>
        <span>智能助手</span>
      </div>
    </section>

    <section class="auth-card">
      <div class="auth-heading">
        <span>Welcome back</span>
        <h2>登录校园集市</h2>
        <p class="muted">继续发现校园里的好物</p>
      </div>
      <el-form label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" size="large" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password size="large" placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="submit">登录平台</el-button>
      </el-form>

      <div class="demo-card">
        <strong>演示账号</strong>
        <div v-for="item in demoAccounts" :key="item[1]" class="demo-row">
          <span>{{ item[0] }}</span>
          <code>{{ item[1] }} / {{ item[2] }}</code>
        </div>
      </div>
      <p class="register-line">还没有账号？<el-link type="primary" @click="$router.push('/register')">立即注册</el-link></p>
    </section>
  </div>
</template>
