import apiClient from './axiosClient';

export const generateSummary = (periodType, selection) => {
  const params = { periodType };
  if (typeof selection === 'string') {
    params.referenceDate = selection;
  } else if (selection) {
    if (selection.referenceDate) params.referenceDate = selection.referenceDate;
    if (selection.startDate) params.startDate = selection.startDate;
    if (selection.endDate) params.endDate = selection.endDate;
  }
  return apiClient.post('/analytics/summary', null, { params }).then((res) => res.data);
};

export const getSummaryHistory = () => apiClient.get('/analytics/summary/history').then((res) => {
  const data = res.data;
  if (!Array.isArray(data)) throw new Error(data?.error || 'Invalid analytics history response.');
  return data;
});

export const getAnalyticsCharts = (startDate, endDate) => apiClient.get('/analytics/charts', { params: startDate && endDate ? { startDate, endDate } : undefined }).then((res) => res.data || {});
