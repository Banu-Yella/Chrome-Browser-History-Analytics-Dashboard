import apiClient from './axiosClient';

export const addManualTag = (browserHistoryId, tagId) => apiClient.post('/browser-history-tags', { browserHistoryId, tagId }).then((res) => res.data);
export const removeTagLink = (id) => apiClient.delete(`/browser-history-tags/${id}`);
export const getTagDistribution = (startDate, endDate) => apiClient.get('/browser-history-tags/distribution', { params: startDate && endDate ? { startDate, endDate } : undefined }).then((res) => res.data || {});
export const getTopTags = (limit = 10) => apiClient.get('/browser-history-tags/top', { params: { limit } }).then((res) => res.data);
export const getAverageConfidence = () => apiClient.get('/browser-history-tags/average-confidence').then((res) => res.data);
