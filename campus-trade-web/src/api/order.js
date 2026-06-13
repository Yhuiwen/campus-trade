import request from './request'
export const createOrder = goodsId => request.post('/orders', { goodsId })
export const getBuyOrders = () => request.get('/orders/buy')
export const getSellOrders = () => request.get('/orders/sell')
export const cancelOrder = id => request.put(`/orders/${id}/cancel`)
export const completeOrder = id => request.put(`/orders/${id}/complete`)
