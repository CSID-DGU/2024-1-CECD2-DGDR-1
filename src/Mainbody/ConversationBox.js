import React, { useState, useEffect, useRef } from 'react';
import './ConversationBox.css';
import { Spinner, Button, Alert } from 'react-bootstrap'; // Bootstrap 컴포넌트 추가
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
          'ngrok-skip-browser-warning': 'true', // ngrok 보안 우회 헤더
        },
        credentials: 'same-origin',
      });

      if (!response.ok) {
        throw new Error(`API error: ${response.statusText}`);
      }

      const data = await response.json();
      const transformedConversations = transformConversations(data);

      setConversations(transformedConversations);

      // 첫 번째 대화의 callID를 부모로 전달
      if (data.length > 0 && data[0].call) {
        onCallIDUpdate(data[0].call.id); // callID를 부모로 전달
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
    try {
      setConversations([]); // 초기화 시 대화를 비움
    } catch (error) {
      setError(error.message);
    }
  };

  if (loading) return <Spinner animation="border" role="status"><span className="sr-only">Loading...</span></Spinner>;

  return (
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
  );
};

export default ConversationBox;
