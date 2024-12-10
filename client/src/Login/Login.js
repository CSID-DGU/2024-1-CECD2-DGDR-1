import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Login.css';
import { getApiBaseUrl } from '../Config';

const API_BASE_URL =  getApiBaseUrl(); // 서버 주소를 여기에 입력하세요.

const LoginForm = () => {
  const navigate = useNavigate(); // 페이지 이동을 위한 navigate 사용
  const [account, setAccount] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  // 회원가입으로 이동하는 함수
  const handleSignupClick = () => {
    navigate('/signup');
  };

  // 로그인 함수
  const handleLogin = async (event) => {
    event.preventDefault();
    setError(''); // 기존 에러 메세지 초기화

    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/user/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'ngrok-skip-browser-warning': 'true', // ngrok 필터 추가
        },
        body: JSON.stringify({
          id: account,
          password,
        }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        setError(errorData.message || '로그인에 실패했습니다.');
        return;
      }

      const data = await response.json();
      const { accessToken, refreshToken } = data;

      // 토큰이 있으면 로컬 스토리지에 저장하고 메인 페이지로 리다이렉트
      if (accessToken && refreshToken) {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        navigate('/'); // 메인 페이지로 리다이렉트
      } else {
        setError('토큰을 받아오지 못했습니다.');
      }
    } catch (error) {
      setError('서버와의 통신에 실패했습니다.');
      console.error('Error during login:', error);
    }
  };

  return (
    <div className="login-container">
      <div className="login-form">
        <h2>로그인</h2>
        <form onSubmit={handleLogin}>
          <div className="form-group">
            <label>아이디</label>
            <input
              type="text"
              placeholder="아이디"
              value={account}
              onChange={(e) => setAccount(e.target.value)}
            />
          </div>
          <div className="form-group">
            <label>비밀번호</label>
            <input
              type="password"
              placeholder="비밀번호"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
          {error && <p className="error-message">{error}</p>}
          <button type="submit" className="login-button">로그인</button>
          <div className="form-footer">
            <button type="button" className="footer-button">아이디/PW 찾기</button>
            <button type="button" className="footer-button" onClick={handleSignupClick}>회원가입</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default LoginForm;
