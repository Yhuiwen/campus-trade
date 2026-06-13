import request from './request'

export const getPublishAdvice = data => request.post('/agent/publish-advice', data)
export const getPriceAdvice = data => request.post('/agent/price-advice', data)
export const getGoodsRisk = goodsId => request.get(`/agent/goods/${goodsId}/risk`)
export const getRecommendGoods = () => request.get('/agent/recommend')
export const chatWithAgent = data => request.post('/agent/chat', data)
