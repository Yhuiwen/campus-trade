<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminOffShelf, approveGoods, banUser, getAllGoods, getCategoryGoodsStat,
  getGoodsStatusStat, getOrderTrendStat, getPendingGoods, getStatOverview,
  getUsers, rejectGoods, unbanUser
} from '../api/admin'

const users = ref([])
const goods = ref([])
const allGoods = ref([])
const overview = ref({ userCount: 0, goodsCount: 0, orderCount: 0, totalAmount: 0 })
const statusChartRef = ref()
const categoryChartRef = ref()
const trendChartRef = ref()
const charts = []

const load = async () => {
  const [userData, pendingData, allGoodsData, overviewData, statusData, categoryData, trendData] = await Promise.all([
    getUsers(), getPendingGoods(), getAllGoods(), getStatOverview(),
    getGoodsStatusStat(), getCategoryGoodsStat(), getOrderTrendStat()
  ])
  users.value = userData
  goods.value = pendingData
  allGoods.value = allGoodsData
  overview.value = overviewData
  await nextTick()
  renderCharts(statusData, categoryData, trendData)
}

const renderCharts = (statusData, categoryData, trendData) => {
  charts.splice(0).forEach(chart => chart.dispose())
  const statusChart = echarts.init(statusChartRef.value)
  const categoryChart = echarts.init(categoryChartRef.value)
  const trendChart = echarts.init(trendChartRef.value)
  charts.push(statusChart, categoryChart, trendChart)
  statusChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: ['42%', '70%'], data: statusData, label: { formatter: '{b}: {c}' } }]
  })
  categoryChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 45, right: 20, top: 25, bottom: 50 },
    xAxis: { type: 'category', data: categoryData.map(item => item.name), axisLabel: { rotate: 25 } },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ type: 'bar', data: categoryData.map(item => item.value), itemStyle: { color: '#4f46e5', borderRadius: [6, 6, 0, 0] } }]
  })
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 45, right: 20, top: 25, bottom: 35 },
    xAxis: { type: 'category', data: trendData.map(item => item.date.slice(5)) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ type: 'line', smooth: true, data: trendData.map(item => item.value), areaStyle: { opacity: 0.15 }, lineStyle: { width: 3, color: '#06b6d4' }, itemStyle: { color: '#06b6d4' } }]
  })
}

const resizeCharts = () => charts.forEach(chart => chart.resize())

onMounted(() => {
  window.addEventListener('resize', resizeCharts)
  load()
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  charts.forEach(chart => chart.dispose())
})
const changeUser = async user => {
  await ElMessageBox.confirm(`确定${user.status === 'NORMAL' ? '封禁' : '解封'}用户 ${user.username}？`)
  await (user.status === 'NORMAL' ? banUser(user.id) : unbanUser(user.id))
  ElMessage.success('用户状态已更新'); load()
}
const audit = async (id, pass) => { await (pass ? approveGoods(id) : rejectGoods(id)); ElMessage.success(pass ? '审核通过' : '已驳回'); load() }
const offShelf = async id => { await ElMessageBox.confirm('确定下架该违规商品？'); await adminOffShelf(id); ElMessage.success('商品已下架'); load() }
</script>

<template>
  <div class="page">
    <div class="hero" style="padding:35px"><h1 style="font-size:32px">平台管理中心</h1><p>维护可信、安全的校园交易环境</p></div>
    <div class="stat-grid">
      <el-card class="stat-card"><span>用户总数</span><strong>{{ overview.userCount }}</strong></el-card>
      <el-card class="stat-card"><span>商品总数</span><strong>{{ overview.goodsCount }}</strong></el-card>
      <el-card class="stat-card"><span>订单总数</span><strong>{{ overview.orderCount }}</strong></el-card>
      <el-card class="stat-card"><span>交易总金额</span><strong>¥{{ overview.totalAmount }}</strong></el-card>
    </div>
    <div class="chart-grid">
      <el-card><template #header><b>商品状态分布</b></template><div ref="statusChartRef" class="chart"></div></el-card>
      <el-card><template #header><b>分类商品数量</b></template><div ref="categoryChartRef" class="chart"></div></el-card>
      <el-card class="trend-card"><template #header><b>最近 7 天订单趋势</b></template><div ref="trendChartRef" class="chart"></div></el-card>
    </div>
    <el-tabs class="panel">
      <el-tab-pane label="商品审核">
        <el-table :data="goods">
          <el-table-column label="商品" min-width="320"><template #default="{row}"><div style="display:flex;gap:12px;align-items:center"><img :src="row.imageUrl || 'https://placehold.co/100'" style="width:75px;height:60px;object-fit:cover;border-radius:8px"><div><b>{{ row.title }}</b><div class="muted">¥{{ row.price }}</div></div></div></template></el-table-column>
          <el-table-column prop="description" label="描述" show-overflow-tooltip />
          <el-table-column prop="createTime" label="发布时间" width="180" />
          <el-table-column label="审核" width="170"><template #default="{row}"><el-button type="success" link @click="audit(row.id,true)">通过</el-button><el-button type="danger" link @click="audit(row.id,false)">驳回</el-button></template></el-table-column>
        </el-table><el-empty v-if="!goods.length" description="没有待审核商品" />
      </el-tab-pane>
      <el-tab-pane label="用户管理">
        <el-table :data="users"><el-table-column prop="username" label="用户名" /><el-table-column prop="nickname" label="昵称" /><el-table-column prop="role" label="角色" /><el-table-column prop="creditScore" label="信用分" /><el-table-column prop="status" label="状态"><template #default="{row}"><el-tag :type="row.status==='NORMAL'?'success':'danger'">{{ row.status }}</el-tag></template></el-table-column><el-table-column label="操作"><template #default="{row}"><el-button v-if="row.role!=='ADMIN'" link :type="row.status==='NORMAL'?'danger':'success'" @click="changeUser(row)">{{ row.status==='NORMAL'?'封禁':'解封' }}</el-button></template></el-table-column></el-table>
      </el-tab-pane>
      <el-tab-pane label="全部商品">
        <el-table :data="allGoods">
          <el-table-column prop="title" label="商品标题" min-width="260" />
          <el-table-column prop="price" label="售价"><template #default="{row}">¥{{ row.price }}</template></el-table-column>
          <el-table-column prop="status" label="状态"><template #default="{row}"><el-tag>{{ row.status }}</el-tag></template></el-table-column>
          <el-table-column prop="viewCount" label="浏览量" />
          <el-table-column label="操作"><template #default="{row}"><el-button v-if="row.status==='ON_SALE'" link type="danger" @click="offShelf(row.id)">违规下架</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
