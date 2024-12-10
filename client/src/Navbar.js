// NavbarForm.js
import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Navbar, Nav, Button } from 'react-bootstrap';
import { HotKeys } from 'react-hotkeys';
import './Navbar.css';

const NavbarForm = ({ userName, onShowBedsModal }) => {
  const location = useLocation();
  const navigate = useNavigate();
  const [currentTime, setCurrentTime] = useState('');

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      const timeString = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      setCurrentTime(timeString);
    };
    updateTime();
    const intervalId = setInterval(updateTime, 60000);

    return () => clearInterval(intervalId);
  }, []);

  const isAuthPage = location.pathname === '/login' || location.pathname === '/signup';

  const handleLogout = () => {
    navigate('/login');
  };

  // `react-hotkeys` 키 매핑 설정
  const keyMap = {
    NAVIGATE_HISTORY: 'shift+1',
    NAVIGATE_MAIN: 'shift+2',
    SHOW_BEDS_MODAL: 'shift+3',
  };

  const handlers = {
    NAVIGATE_HISTORY: () => navigate('/history'),
    NAVIGATE_MAIN: () => navigate('/'),
    SHOW_BEDS_MODAL: onShowBedsModal,
  };

  return (
    <HotKeys keyMap={keyMap} handlers={handlers}>
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
                <Button variant="danger" onClick={onShowBedsModal} className="nav-button">응급 병상 확인하기</Button>
              </Nav>
              <Navbar.Text className="navbar-text ms-auto">
                {userName} 님 | 접속 시간: {currentTime}
              </Navbar.Text>
            </>
          )}
        </Navbar.Collapse>
      </Navbar>
    </HotKeys>
  );
};

export default NavbarForm;
