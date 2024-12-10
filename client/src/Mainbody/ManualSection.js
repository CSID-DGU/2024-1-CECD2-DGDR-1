import React, { useState, useEffect } from 'react';
import { Button, Card, Alert } from 'react-bootstrap';
import { HotKeys } from 'react-hotkeys';
import './ManualSection.css';
import { getApiBaseUrl } from '../Config';

const API_BASE_URL = getApiBaseUrl();

const ManualSection = ({ onSaveManual, selectedManual, callID }) => {
  const [manual, setManual] = useState(null);
  const [cards, setCards] = useState([]);
  const [selectedCardId, setSelectedCardId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (selectedManual) {
      setManual(selectedManual);
      setSelectedCardId(selectedManual.id);
    }
  }, [selectedManual]);

  const fetchAndProcessData = async (callID) => {
    try {
      setLoading(true);
      const response = await fetch(`${API_BASE_URL}/manual/${callID}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'ngrok-skip-browser-warning': 'true',
        },
      });

      if (!response.ok) {
        throw new Error('Failed to fetch data from the API');
      }

      const data = await response.json();
      const processedData = processData(data);
      setCards(processedData);
      setError(null);
    } catch (error) {
      console.error('Fetch error:', error);
      setError(error.message);
    } finally {
      setLoading(false);
    }
  };

  const processData = (data) => {
    let result = [];
    Object.keys(data).forEach((key, index) => {
      const passageData = data[key];
      result.push({
        id: index + 1,
        passage: passageData.병명,
        script: {
          clinicalFeatures: passageData["임상적 특징"],
          patientAssessment: passageData["환자평가 필수항목"],
        },
      });
    });
    return result;
  };

  const handleManualButtonClick = () => {
    fetchAndProcessData(callID);
  };

  const handleCardClick = (card) => {
    const selected = { title: card.passage, content: card.script };
    setManual(selected);
    setSelectedCardId(card.id);
    onSaveManual(selected);
  };

  // 줄바꿈을 반영하여 텍스트를 포맷하는 함수
  const formatContentWithLineBreaks = (text) => {
    return text.split('\n').map((line, index) => (
      <React.Fragment key={index}>
        {line}
        <br />
      </React.Fragment>
    ));
  };

  // 단축키 설정
  const keyMap = {
    MANUAL_FETCH: 'ctrl+alt+m',
  };

  const handlers = {
    MANUAL_FETCH: handleManualButtonClick,
  };

  return (
    <HotKeys keyMap={keyMap} handlers={handlers}>
      <div className="manual-section">
        <div className="manual-section-header">
          <h2 className="manual-title">
            {manual ? manual.title : '아직 불러온 매뉴얼이 없습니다.'}
          </h2>
        </div>
        {manual ? (
          <div className="manual-content">
            <h4>임상적 특징</h4>
            <p>{formatContentWithLineBreaks(manual.content.clinicalFeatures)}</p>
            <div className="manual-content-spacing"></div>
            <h4>환자평가 필수항목</h4>
            <p>{formatContentWithLineBreaks(manual.content.patientAssessment)}</p>
          </div>
        ) : (
          <Alert variant="info" className="manual-placeholder">
            아직 불러온 매뉴얼이 없습니다.
          </Alert>
        )}
        <div className="buttons">
          <Button id="manual-check-button" variant="primary" onClick={handleManualButtonClick}>
            매뉴얼 확인하기
          </Button>
        </div>
        <div className="cards">
          {cards.map(card => (
            <Card
              key={card.id}
              className={`card ${card.id === selectedCardId ? 'selected' : ''}`}
              onClick={() => handleCardClick(card)}
            >
              <Card.Body>
                <Card.Title className="card-title">{card.passage}</Card.Title>
              </Card.Body>
            </Card>
          ))}
        </div>
      </div>
    </HotKeys>
  );
};

export default ManualSection;
