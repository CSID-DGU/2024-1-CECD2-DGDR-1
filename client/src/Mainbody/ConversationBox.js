import React, { useState, useEffect, useRef } from 'react';
import './ConversationBox.css';

const API_BASE_URL = 'https://82eb-58-236-188-94.ngrok-free.app';

const ConversationBox = () => {
  const [conversations, setConversations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const conversationEndRef = useRef(null); // Ref for the end of the conversation box

  useEffect(() => {
    fetchConversations(); // 대화 내용을 가져오는 함수 호출

    const intervalId = setInterval(fetchConversations, 5000); // 5초마다 대화 내용을 가져오는 함수 호출

    return () => clearInterval(intervalId); // 컴포넌트 언마운트 시 인터벌 정리
  }, []);

  useEffect(() => {
    scrollToBottom(); // Scroll to bottom when conversations change
  }, [conversations]);

  const fetchConversations = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/transcript`, {
        headers: {
          'Content-Type': 'application/json',
          'ngrok-skip-browser-warning': '69420',
        },
      });
  
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
  
      const contentType = response.headers.get("content-type");
      if (contentType && contentType.includes("application/json")) {
        const data = await response.json();
  
        // 데이터를 대화 형식으로 변환
        const transformedConversations = data.map((item, index) => ({
          id: index + 1,
          text: item.transcription,
          sender: item.callerId === '821071578670' ? 'agent' : 'patient',
          time: new Date(item.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        }));
  
        setConversations(transformedConversations);
        setError(null); // 데이터 로드 성공 시 에러 상태 초기화
      } else {
        throw new Error('JSON 형식이 아닌 응답을 받았습니다.');
      }
    } catch (error) {
      console.error('Fetch error:', error); // 오류 전체 로그 출력
      setError(error.message);
      setConversations([]); // 에러 발생 시 대화 내용을 빈 배열로 설정
    } finally {
      setLoading(false);
    }
  };
  

  const handleClearConversations = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/transcript/clear`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'ngrok-skip-browser-warning': '69420',
        },
      });

      if (!response.ok) {
        throw new Error(`Failed to clear conversations: ${response.status}`);
      }

      setConversations([]); // 초기화가 성공하면 대화를 비웁니다.
    } catch (error) {
      setError(error.message);
    }
  };

  const scrollToBottom = () => {
    if (conversationEndRef.current) {
      conversationEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  };

  if (loading) return <p>Loading...</p>;

  return (
    <div className="conversation-box-container">
      <div className="conversation-header">
        <h3>응급 신고 통화 내용</h3>
        <div className="button-container">
          <button className="clear-button" onClick={handleClearConversations}>초기화</button>
        </div>
      </div>
      {error ? ( // 에러가 있을 경우 메시지 표시
        <p className="error-message">서버가 응답하지 않습니다.</p>
      ) : (
        <div className="conversation-box">
          {conversations.length > 0 ? (
            conversations.map(conversation => (
              <div
                key={conversation.id}
                className={`message ${conversation.sender === 'agent' ? 'agent' : 'patient'}`}
              >
                <span>{conversation.text}</span>
                <span className="time">{conversation.time}</span>
              </div>
            ))
          ) : (
            <p className="error-message">대화 내용을 가져올 수 없습니다.</p>
          )}
          <div ref={conversationEndRef} /> {/* This div acts as a reference point to scroll to */}
        </div>
      )}
    </div>
  );
};

export default ConversationBox;
