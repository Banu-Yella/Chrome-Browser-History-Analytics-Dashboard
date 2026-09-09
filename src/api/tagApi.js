import apiClient from './axiosClient';

export const getAllTags = async () => {
  const data = (await apiClient.get('/tags')).data;
  if (!Array.isArray(data)) throw new Error(data?.error || 'Invalid tags response.');
  return data;
};
export const getTagById = (id) => apiClient.get(`/tags/${id}`).then((res) => res.data);
export const createTag = (payload) => apiClient.post('/tags', payload).then((res) => res.data);
export const updateTag = (id, payload) => apiClient.put(`/tags/${id}`, payload).then((res) => res.data);
export const deleteTag = (id) => apiClient.delete(`/tags/${id}`);
