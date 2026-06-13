import request from './request'

export const addFavorite = goodsId => request.post(`/favorites/${goodsId}`)
export const removeFavorite = goodsId => request.delete(`/favorites/${goodsId}`)
export const getMyFavorites = () => request.get('/favorites/my')
export const checkFavorite = goodsId => request.get(`/favorites/check/${goodsId}`)
