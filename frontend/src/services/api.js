import axios from 'axios';
import { TokenStorage } from '../utils/tokenStorage';

// Axios 인스턴스 생성
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터 - 모든 요청에 JWT 토큰 추가
api.interceptors.request.use(
  (config) => {
    const token = TokenStorage.getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터 - 토큰 만료 시 자동 갱신
api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = TokenStorage.getRefreshToken();
        if (refreshToken) {
          const response = await axios.post('http://localhost:8080/api/auth/refresh', {
            refreshToken: refreshToken
          });

          const { accessToken, refreshToken: newRefreshToken } = response.data;
          TokenStorage.setAccessToken(accessToken);
          TokenStorage.setRefreshToken(newRefreshToken);

          // 원래 요청 재시도
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          return api(originalRequest);
        }
      } catch (refreshError) {
        console.error('토큰 갱신 실패:', refreshError);
        TokenStorage.clearTokens();
        window.location.href = '/';
      }
    }

    return Promise.reject(error);
  }
);

// API 함수들
export const ApiService = {
  // 공개 API
  hello: () => api.get('/hello'),

  // 인증이 필요한 API
  getCurrentUser: () => api.get('/me'),
  getUser: (id) => api.get(`/user/${id}`),

  // 토큰 관련 API
  refreshToken: (refreshToken) => 
    axios.post('http://localhost:8080/api/auth/refresh', { refreshToken }),
  
  validateToken: (token) => 
    axios.post('http://localhost:8080/api/auth/validate', { token }),
};

export default api;