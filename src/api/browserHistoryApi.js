import apiClient from './axiosClient';

export const syncBrowserHistory = () => apiClient.post('/browser-history/sync').then((res) => res.data);
export const getSyncStatus = () => apiClient.get('/browser-history/sync/status').then((res) => res.data);
export const getAllBrowserHistory = () => apiClient.get('/browser-history').then((res) => res.data);
export const getBrowserHistoryById = (id) => apiClient.get(`/browser-history/${id}`).then((res) => res.data);
export const getTagsForHistory = (id) => apiClient.get(`/browser-history/${id}/tags`).then((res) => res.data);
export const getTopDomains = (startDate, endDate) => apiClient.get('/browser-history/top-domains', { params: startDate && endDate ? { startDate, endDate } : undefined }).then((res) => res.data);
export const getVisitCountStats = () => apiClient.get('/browser-history/stats').then((res) => res.data);
export const getAllChromeUrls = () => apiClient.get('/chrome-urls').then((res) => res.data);
export const getAllVisits = () => apiClient.get('/visits').then((res) => res.data);
export const getAllBrowserHistoryTags = () => apiClient.get('/browser-history-tags').then((res) => res.data);
export const getUser = () => apiClient.get('/users/user').then((res) => res.data);
