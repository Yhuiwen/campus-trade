<script setup>
import { nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { chatWithAgent } from '../api/agent'

const router = useRouter()
const question = ref('')
const goodsId = ref(null)
const loading = ref(false)
const messages = ref([
  {
    role: 'agent',
    text: '你好，我是本地规则版智能交易助手。可以帮你判断商品风险、评估价格、推荐商品和解答信用分问题。',
    suggestions: ['这个商品值得买吗', '这个价格合理吗', '给我推荐一些数码产品', '怎么提高信用分'],
    relatedGoods: []
  }
])
const chatBox = ref()

const send = async preset => {
  const text = preset || question.value.trim()
  if (!text) return ElMessage.warning('请输入问题')
  messages.value.push({ role: 'user', text })
  question.value = ''
  loading.value = true
  try {
    const result = await chatWithAgent({ question: text, goodsId: goodsId.value || null })
    messages.value.push({ role: 'agent', text: result.answer, suggestions: result.suggestions, relatedGoods: result.relatedGoods || [] })
    await nextTick()
    chatBox.value?.scrollTo({ top: chatBox.value.scrollHeight, behavior: 'smooth' })
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page agent-layout">
    <section class="agent-main panel">
      <div class="agent-heading">
        <div><h1>智能交易助手 Agent</h1><p class="muted">基于平台实时数据与本地规则运行，无需外部大模型 API</p></div>
        <el-tag type="success" effect="dark" round>本地规则引擎</el-tag>
      </div>
      <div ref="chatBox" class="chat-box">
        <div v-for="(message, index) in messages" :key="index" :class="['chat-message', message.role]">
          <div class="chat-bubble">
            <b>{{ message.role === 'agent' ? '智能助手' : '我' }}</b>
            <p>{{ message.text }}</p>
            <div v-if="message.suggestions?.length" class="suggestion-list">
              <el-tag v-for="item in message.suggestions" :key="item" effect="plain" @click="send(item)">{{ item }}</el-tag>
            </div>
            <div v-if="message.relatedGoods?.length" class="agent-goods">
              <el-card v-for="item in message.relatedGoods" :key="item.id" shadow="hover" @click="router.push(`/goods/${item.id}`)">
                <img :src="item.imageUrl || 'https://placehold.co/180x120?text=Goods'">
                <b>{{ item.title }}</b><span>¥{{ item.price }}</span>
              </el-card>
            </div>
          </div>
        </div>
        <div v-if="loading" class="chat-message agent"><div class="chat-bubble">正在分析平台数据...</div></div>
      </div>
      <div class="chat-input">
        <el-input-number v-model="goodsId" :min="1" controls-position="right" placeholder="商品ID（可选）" />
        <el-input v-model="question" size="large" placeholder="例如：这个商品值得买吗？" @keyup.enter="send()" />
        <el-button type="primary" size="large" :loading="loading" @click="send()">发送</el-button>
      </div>
    </section>
    <aside class="panel agent-guide">
      <h3>我可以帮你</h3>
      <el-button plain @click="send('给我推荐一些数码产品')">发现相关商品</el-button>
      <el-button plain @click="send('怎么提高信用分')">了解信用规则</el-button>
      <el-button plain @click="send('我的商品应该卖多少钱')">商品定价建议</el-button>
      <el-alert title="分析具体商品时，请填写商品 ID。商品 ID 可从详情页网址中查看。" type="info" :closable="false" />
    </aside>
  </div>
</template>
