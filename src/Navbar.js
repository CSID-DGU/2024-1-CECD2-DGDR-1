import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Navbar, Nav, Button } from 'react-bootstrap';
import './Navbar.css';

const NavbarForm = ({ userName }) => {
  const location = useLocation();
  const navigate = useNavigate();
  const [currentTime, setCurrentTime] = useState('');

  useEffect(() => {
    // 현재 시간을 업데이트
    const updateTime = () => {
      const now = new Date();
      const timeString = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      setCurrentTime(timeString);
    };
    updateTime();
    const intervalId = setInterval(updateTime, 60000); // 1분마다 시간 업데이트

    return () => clearInterval(intervalId); // cleanup
  }, []);

  // 로그인, 회원가입 페이지인 경우
  const isAuthPage = location.pathname === '/login' || location.pathname === '/signup';

  // 로그아웃 함수
  const handleLogout = () => {
    // 로그아웃 로직을 여기에 추가 가능 (예: 토큰 삭제)
    navigate('/login');
  };

  return (
    <Navbar bg="light" expand="lg" className="custom-navbar">
      <Navbar.Toggle aria-controls="basic-navbar-nav" />
      <Navbar.Collapse id="basic-navbar-nav">
        {isAuthPage ? (
          <Nav className="me-auto">
            <Button variant="outline-primary" onClick={() => navigate('/login')} className="nav-button">로그인</Button>
            <Button variant="outline-secondary" onClick={() => navigate('/signup')} className="nav-button">회원가입</Button>
          </Nav>
        ) : (
          <>
            <Nav className="me-auto">
              <Button variant="outline-primary" onClick={() => navigate('/history')} className="nav-button">History</Button>
              <Button variant="outline-secondary" onClick={() => navigate('/')} className="nav-button">Main</Button>
              <Button variant="danger" onClick={handleLogout} className="nav-button">로그아웃</Button>
            </Nav>
            <Navbar.Text className="navbar-text ms-auto">
              {userName} 님 | 접속 시간: {currentTime}
            </Navbar.Text>
          </>
        )}
      </Navbar.Collapse>
    </Navbar>
  );
};

export default NavbarForm;
