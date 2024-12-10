import React, { useState, useEffect } from 'react';
import { ListGroup, Spinner } from 'react-bootstrap';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import './CallHistory.css';
import { getApiBaseUrl } from '../Config';


const API_BASE_URL = getApiBaseUrl();

const CallHistory = () => {
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [callList, setCallList] = useState([]);
  const [selectedCall, setSelectedCall] = useState(null); // 선택된 통화 기록
  const [selectedCallId, setSelectedCallId] = useState(null); // 선택된 callId를 관리하는 상태 추가
  const [loading, setLoading] = useState(false);

  // 서버에서 통화 기록을 가져오는 함수
  const fetchCallList = async (startDate) => {
    setLoading(true);
    try {
      const formattedDate = startDate.toISOString().split('T')[0]; // 날짜를 "YYYY-MM-DD" 형식으로 변환
      const response = await fetch(`${API_BASE_URL}/api/v1/call/date?startDate=${formattedDate}&endDate=${formattedDate}`, {
        headers: {
          'Content-Type': 'application/json',
          'ngrok-skip-browser-warning': 'true', // ngrok 필터 추가
        },
      });
      const data = await response.json();
      setCallList(data);
    } catch (error) {
      console.error('Error fetching call list:', error);
      setCallList([]); // 오류 발생 시 빈 리스트로 설정
    } finally {
      setLoading(false);
    }
  };

  // 서버에서 선택된 CallId로 통화 기록을 가져오는 함수
  const fetchCallDetails = async (callId) => {
    setLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/${callId}/call-record`, {
        headers: {
          'Content-Type': 'application/json',
          'ngrok-skip-browser-warning': 'true', // ngrok 필터 추가
        },
      });
      const data = await response.json();
      setSelectedCall(data); // 서버로부터 받은 통화 상세 정보를 설정
    } catch (error) {
      console.error('Error fetching call details:', error);
      setSelectedCall(null); // 오류 발생 시 null로 설정
    } finally {
      setLoading(false);
    }
  };

  // 날짜 변경 시 호출되는 함수
  const handleDateChange = (date) => {
    setSelectedDate(date);
    fetchCallList(date); // 선택된 날짜로 서버 데이터를 가져옴
  };

  // 통화 클릭 시 대화 내역을 가져오는 함수
  const handleCallClick = (callId) => {
    setSelectedCallId(callId); // 선택된 callId 설정
    fetchCallDetails(callId); // 선택된 CallId로 통화 내역을 가져옴
  };

  useEffect(() => {
    fetchCallList(selectedDate); // 초기 로딩 시 현재 날짜의 데이터를 설정
  }, [selectedDate]);

  return (
    <div className="history-detail-wrapper">
      <div className="call-history-container">
        <h3>통화 기록</h3>
        <div className="date-picker">
          <DatePicker
            selected={selectedDate}
            onChange={handleDateChange}
            dateFormat="yyyy.MM.dd"
            className="form-control"
          />
        </div>
        <ListGroup className="call-list">
          {loading ? (
            <Spinner animation="border" role="status">
              <span className="sr-only">Loading...</span>
            </Spinner>
          ) : (
            callList.map((call) => (
              <ListGroup.Item
                key={call.id}
                onClick={() => handleCallClick(call.id)}
                className={`call-list-item ${selectedCallId === call.id ? 'selected' : ''}`} // 선택된 항목 스타일 적용
              >
                {new Date(call.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </ListGroup.Item>
            ))
          )}
        </ListGroup>
      </div>

      <div className="call-details-container">
        {selectedCall ? (
          <div className="conversation-box">
            {selectedCall.map((entry) => (
              <div key={entry.id} className={`message-container ${entry.speakerPhoneNumber === '821071578670' ? 'agent-container' : 'patient-container'}`}>
                {entry.speakerPhoneNumber === '821071578670' ? (
                  <>
                    <span className="time time-left">{new Date(entry.time).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                    <div className="message agent-message">
                      <span>{entry.transcription}</span>
                    </div>
                  </>
                ) : (
                  <>
                    <div className="message patient-message">
                      <span>{entry.transcription}</span>
                    </div>
                    <span className="time time-right">{new Date(entry.time).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                  </>
                )}
              </div>
            ))}
          </div>
        ) : (
          <div className="call-placeholder">통화 내역을 선택하세요.</div>
        )}
      </div>
    </div>
  );
};

export default CallHistory;