import React, { useState, useEffect } from 'react';
import { ApiService } from '../services/api';
import { TokenStorage } from '../utils/tokenStorage';

const UserProfile = ({ onLogout }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchCurrentUser();
  }, []);

  const fetchCurrentUser = async () => {
    try {
      setLoading(true);
      const response = await ApiService.getCurrentUser();
      setUser(response.data);
    } catch (error) {
      console.error('사용자 정보 조회 실패:', error);
      setError('사용자 정보를 불러올 수 없습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    TokenStorage.clearTokens();
    setUser(null);
    onLogout();
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center p-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
        <span className="ml-2">로딩 중...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
        {error}
      </div>
    );
  }

  if (!user) {
    return null;
  }

  return (
    <div className="bg-white rounded-lg shadow-lg p-6 max-w-md mx-auto">
      <div className="flex items-center space-x-4">
        {user.picture && (
          <img
            src={user.picture}
            alt="프로필"
            className="w-16 h-16 rounded-full"
          />
        )}
        <div className="flex-1">
          <h2 className="text-xl font-bold text-gray-800">{user.name}</h2>
          <p className="text-gray-600">{user.email}</p>
          <span className="inline-block bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded-full mt-1">
            {user.role}
          </span>
        </div>
      </div>
      
      <div className="mt-6 space-y-2">
        <div className="text-sm text-gray-500">
          <strong>가입일:</strong> {new Date(user.createdDate).toLocaleDateString('ko-KR')}
        </div>
        <div className="text-sm text-gray-500">
          <strong>마지막 수정:</strong> {new Date(user.modifiedDate).toLocaleDateString('ko-KR')}
        </div>
      </div>

      <div className="mt-6 flex space-x-3">
        <button
          onClick={fetchCurrentUser}
          className="flex-1 bg-blue-500 hover:bg-blue-600 text-white py-2 px-4 rounded transition duration-200"
        >
          새로고침
        </button>
        <button
          onClick={handleLogout}
          className="flex-1 bg-red-500 hover:bg-red-600 text-white py-2 px-4 rounded transition duration-200"
        >
          로그아웃
        </button>
      </div>
    </div>
  );
};

export default UserProfile;