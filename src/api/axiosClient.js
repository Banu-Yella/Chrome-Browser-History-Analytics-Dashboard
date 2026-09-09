import axios from 'axios';

// Every API call in this app goes through this one instance. If the backend
// ever moves (different port, deployed host, etc.), this is the only line to change.
const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

export default apiClient;
