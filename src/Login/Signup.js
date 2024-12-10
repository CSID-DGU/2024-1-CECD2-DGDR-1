import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Signup.css';
import { getApiBaseUrl } from '../Config';

const API_BASE_URL = getApiBaseUrl(); // API 베이스 URL 가져오기

// ngrok 필터 함수
const filterNgrok = (url) => {
  if (url.includes("ngrok")) {
    return url.replace("ngrok", "production-url"); // 필터링 후 실제 서비스 주소로 교체
  }
  return url;
};

const SignUpForm = () => {
  const [phoneNumber, setPhoneNumber] = useState('');
  const [account, setAccount] = useState('');
  const [name, setName] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [accountValid, setAccountValid] = useState(null);
  const [phoneValid, setPhoneValid] = useState(null);
  const [passwordValid, setPasswordValid] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false); // 제출 상태 관리
  const [signupSuccess, setSignupSuccess] = useState(false); // 회원가입 성공 메시지 상태
  const [errorMessage, setErrorMessage] = useState(null); // 에러 메시지 상태
  const navigate = useNavigate();

  // 비밀번호 유효성 검사
  const validatePassword = (password) => {
    const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,12}$/;
    return regex.test(password);
  };

  // 아이디 중복 확인 함수
  const checkAccountAvailability = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/user/auth/id/check?id=${account}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'ngrok-skip-browser-warning': 'true', // ngrok 필터 추가
        },
      });
      const data = await response.json();

      if (data.isExist) {
        setAccountValid(false); // 이미 사용 중인 아이디
      } else {
        setAccountValid(true); // 사용 가능한 아이디
      }
    } catch (error) {
      console.error('Error checking account availability:', error);
      setAccountValid(false);
    }
  };

  // 전화번호 인증 함수
  const phoneNumberAvailability = () => {
    setPhoneValid(true); // 현재는 무조건 인증 성공 처리
  };

  // 회원가입 처리 함수
  const handleSignup = async (event) => {
    event.preventDefault();
    setIsSubmitting(true);

    if (!account || !name || !password || !confirmPassword || !phoneNumber) {
      alert('모든 필드를 채워주세요.');
      setIsSubmitting(false);
      return;
    }

    if (password !== confirmPassword) {
      alert('비밀번호가 일치하지 않습니다.');
      setIsSubmitting(false);
      return;
    }

    if (!passwordValid) {
      alert('유효한 비밀번호를 입력하세요.');
      setIsSubmitting(false);
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/user/auth/signup`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'ngrok-skip-browser-warning': 'true', // ngrok 필터 추가
        },
        body: JSON.stringify({
          id: account,
          name: name,
          password: password,
          phone: phoneNumber,
        }),
      });

      if (response.ok) {
        setSignupSuccess(true);
        setTimeout(() => {
          navigate('/login'); // 성공 후 로그인 페이지로 리다이렉트
        }, 2000);
      } else {
        const errorData = await response.json();
        setErrorMessage(errorData.message || '회원가입에 실패했습니다.');
        setSignupSuccess(false);
      }
    } catch (error) {
      setErrorMessage('서버와의 통신에 실패했습니다.');
      console.error('Error during signup:', error);
    }

    setIsSubmitting(false);
  };

  return (
    <div className="sign-up-container">
      <div className="sign-up-form">
        <h2>회원 가입</h2>
        <form onSubmit={handleSignup}>
          <div className="form-group">
            <label>아이디</label>
            <div className="input-container">
              <input
                type="text"
                placeholder="아이디"
                value={account}
                onChange={(e) => setAccount(e.target.value)}
              />
              <button type="button" onClick={checkAccountAvailability}>중복 확인</button>
            </div>
            {accountValid === null ? null : accountValid ? (
              <p className="success-message">✔ 사용할 수 있는 아이디입니다.</p>
            ) : (
              <p className="error-message">✖ 이미 사용 중인 아이디입니다.</p>
            )}
          </div>

          {/* 전화번호 입력 필드를 먼저, 이름 필드를 그 다음에 배치 */}
          <div className="form-group">
            <label>전화번호</label>
            <div className="input-container">
              <input
                type="text"
                placeholder="전화번호"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
              />
              <button type="button" onClick={phoneNumberAvailability}>인증하기</button>
            </div>
              {phoneValid === null ? null : phoneValid ? (
              <p className="success-message">✔ 사용 가능한 번호입니다.</p>
              ) : (
              <p className="error-message">✖ 이미 사용 중인 번호입니다.</p>
              )}
            </div>

          <div className="form-group">
            <label>이름</label>
            <input
              type="text"
              placeholder="이름"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </div>
          
          <div className="form-group">
            <label>비밀번호</label>
            <input
              type="password"
              placeholder="비밀번호"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                setPasswordValid(validatePassword(e.target.value));
              }}
            />
            {password && (
              <p className={passwordValid ? 'success-message' : 'error-message'}>
                {passwordValid
                  ? '✔ 비밀번호가 유효합니다.'
                  : '✖ 비밀번호는 8~12자여야 하며, 특수문자 및 영어 대소문자를 모두 포함해야 합니다.'}
              </p>
            )}
          </div>
          <div className="form-group">
            <label>비밀번호 확인하기</label>
            <input
              type="password"
              placeholder="비밀번호 확인하기"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
            />
            {confirmPassword && password !== confirmPassword && (
              <p className="error-message">✖ 비밀번호가 일치하지 않습니다.</p>
            )}
          </div>
          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? '제출 중...' : '제출하기'}
          </button>
        </form>

        {signupSuccess && <p className="success-message">회원가입이 성공했습니다! 로그인 페이지로 이동합니다...</p>}
        {errorMessage && <p className="error-message">{errorMessage}</p>}
      </div>
    </div>
  );
};

export default SignUpForm;
