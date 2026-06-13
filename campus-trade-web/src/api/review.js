import request from './request'
export const createReview = data => request.post('/reviews', data)
export const getUserReviews = userId => request.get(`/reviews/user/${userId}`)
