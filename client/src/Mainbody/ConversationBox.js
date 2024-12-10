// ConversationBox.js
import React, { useState, useEffect, useRef } from 'react';
import './ConversationBox.css';
import { Spinner, Button, Alert } from 'react-bootstrap';
import { HotKeys } from 'react-hotkeys';
import { getApiBaseUrl } from '../Config';

const API_BASE_URL = getApiBaseUrl();

const ConversationBox = ({ onCallIDUpdate }) => {
  const [conversations, setConversations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const conversationEndRef = useRef(null);

  useEffect(() => {
    fetchConversations();
    const intervalId = setInterval(fetchConversations, 5000);
    return () => clearInterval(intervalId);
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [conversations]);

  const fetchConversations = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/call/latest`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'ngrok-skip-browser-warning': 'true',
        },
        credentials: 'same-origin',
      });

      if (!response.ok) {
        throw new Error(`API error: ${response.statusText}`);
      }

      const data = await response.json();
      const transformedConversations = transformConversations(data);

      setConversations(transformedConversations);

      if (data.length > 0 && data[0].call) {
        onCallIDUpdate(data[0].call.id);
      }

      setError(null);
    } catch (error) {
      console.error('Fetch error:', error);
      setError(error.message);
      setConversations([]);
    } finally {
      setLoading(false);
    }
  };

  const transformConversations = (data) => {
    return data.map((item, index) => ({
      id: index + 1,
      text: item.transcription,
      sender: item.speakerPhoneNumber === '821071578670' ? 'agent' : 'patient',
      time: new Date(item.time).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    }));
  };

  const scrollToBottom = () => {
    if (conversationEndRef.current) {
      conversationEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  };

  const handleClearConversations = async () => {
    setConversations([]);
  };

  // 단축키 설정
  const keyMap = {
    REFRESH_CONVERSATIONS: 'ctrl+alt+r',
  };

  const handlers = {
    REFRESH_CONVERSATIONS: handleClearConversations,
  };

  if (loading) return <Spinner animation="border" role="status"><span className="sr-only">Loading...</span></Spinner>;

  return (
    <HotKeys keyMap={keyMap} handlers={handlers}>
      <div className="conversation-box-container">
        <div className="conversation-header d-flex justify-content-between align-items-center">
          <h3>응급 신고 통화 내용</h3>
          <Button variant="danger" onClick={handleClearConversations}>새로고침</Button>
        </div>
        {error ? (
          <Alert variant="danger" className="error-message">서버가 응답하지 않습니다.</Alert>
        ) : (
          <div className="conversation-box">
            {conversations.length > 0 ? (
              conversations.map((conversation, index) => (
                <div
                  key={conversation.id}
                  className={`message-container ${conversation.sender === 'agent' ? 'agent-container' : 'patient-container'}`}
                >
                  {conversation.sender === 'agent' ? (
                    <>
                      <span className="time time-left">{conversation.time}</span>
                      <div className={`message ${index === conversations.length - 1 ? 'latest-message' : ''}`}>
                        <span>{conversation.text}</span>
                      </div>
                    </>
                  ) : (
                    <>
                      <div className={`message ${index === conversations.length - 1 ? 'latest-message' : ''}`}>
                        <span>{conversation.text}</span>
                      </div>
                      <span className="time time-right">{conversation.time}</span>
                    </>
                  )}
                </div>
              ))
            ) : (
              <Alert variant="info">대화 내용을 가져오는 중입니다....</Alert>
            )}
            <div ref={conversationEndRef} />
          </div>
        )}
      </div>
    </HotKeys>
  );
};

export default ConversationBox;
