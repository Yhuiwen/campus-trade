import request from './request'

export const getMyHistory = () => request.get('/history/my')
