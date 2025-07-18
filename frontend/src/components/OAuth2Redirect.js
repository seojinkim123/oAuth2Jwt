import React, { useEffect } from 'react';
import { TokenStorage } from '../utils/tokenStorage';

const OAuth2Redirect = ({ onLoginSuccess }) => {
  useEffect(() => {
    // URL에서 토큰 파라미터 추출
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    const refreshToken = urlParams.get('refreshToken');

    if (token && refreshToken) {
      // 토큰 저장
      TokenStorage.setAccessToken(token);
      TokenStorage.setRefreshToken(refreshToken);
      
      console.log('OAuth2 로그인 성공, 토큰 저장 완료');
      
      // 부모 컴포넌트에 로그인 성공 알림
      onLoginSuccess();
      
      // URL 정리 (토큰 제거)
      window.history.replaceState({}, document.title, window.location.pathname);
    } else {
      console.error('OAuth2 로그인 실패: 토큰을 찾을 수 없습니다.');
    }
  }, [onLoginSuccess]);

  return (
    <div className="flex justify-center items-center min-h-screen">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
        <p className="mt-4 text-gray-600">로그인 처리 중...</p>
      </div>
    </div>
  );
};

export default OAuth2Redirect;