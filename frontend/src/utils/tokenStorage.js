// JWT 토큰 관리 유틸리티
export const TokenStorage = {
  // 액세스 토큰 저장
  setAccessToken: (token) => {
    localStorage.setItem('accessToken', token);
  },

  // 리프레시 토큰 저장
  setRefreshToken: (token) => {
    localStorage.setItem('refreshToken', token);
  },

  // 액세스 토큰 조회
  getAccessToken: () => {
    return localStorage.getItem('accessToken');
  },

  // 리프레시 토큰 조회
  getRefreshToken: () => {
    return localStorage.getItem('refreshToken');
  },

  // 모든 토큰 삭제
  clearTokens: () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  },

  // 토큰 존재 여부 확인
  hasTokens: () => {
    return !!(localStorage.getItem('accessToken') && localStorage.getItem('refreshToken'));
  },

  // JWT 토큰 파싱 (payload 추출)
  parseJWT: (token) => {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('JWT 파싱 오류:', error);
      return null;
    }
  },

  // 토큰 만료 확인
  isTokenExpired: (token) => {
    if (!token) return true;
    
    const decoded = TokenStorage.parseJWT(token);
    if (!decoded || !decoded.exp) return true;
    
    const currentTime = Date.now() / 1000;
    return decoded.exp < currentTime;
  }
};