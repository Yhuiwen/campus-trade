import request from './request'
export const getGoodsPage = params => request.get('/goods/page', { params })
export const getGoods = id => request.get(`/goods/${id}`)
export const getHotGoods = () => request.get('/goods/hot')
export const createGoods = data => request.post('/goods', data)
export const updateGoods = (id, data) => request.put(`/goods/${id}`, data)
export const offShelfGoods = id => request.put(`/goods/${id}/off-shelf`)
export const getCategories = () => request.get('/categories')
export const uploadImage = file => {
  const data = new FormData()
  data.append('file', file)
  return request.post('/files/upload', data)
}
