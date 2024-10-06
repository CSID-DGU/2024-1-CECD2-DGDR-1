import React, { useEffect, useState } from 'react';
import './DetailInfoModal.css';

const API_BASE_URL = 'http://whitex.iptime.org:4000';

const Title = ({ hospitalName }) => (
  <div>
    <h1>{hospitalName} 상세 정보</h1>
  </div>
);

const Section = ({ title, items }) => (
  <div className="section">
    <h2>{title}</h2>
    <ul>
      {items.map((item, index) => (
        <li key={index}>{item}</li>
      ))}
    </ul>
  </div>
);

const DetailInfoModal = ({ onClose }) => {  // 병원 ID를 직접 지정하지 않음
  const [hospitalDetails, setHospitalDetails] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchHospitalDetails = async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/hospital/emergency`, {
          headers: {
            'Content-Type': 'application/json',
          },
        });

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();

        // ID가 1인 데이터만 필터링
        const hospitalData = data.find((item) => item.id === 1);
        setHospitalDetails(hospitalData);
        setLoading(false);
      } catch (error) {
        setError(error.message);
        setLoading(false);
      }
    };

    fetchHospitalDetails();
  }, []); // hospitalPid를 제거

  if (loading) return <p>Loading...</p>;
  if (error) return <p>Error: {error}</p>;
  if (!hospitalDetails) return <p>병원 데이터를 찾을 수 없습니다.</p>; // 데이터를 못 찾는 경우

  const doctors = ["뇌혈관", "뇌검사와치료류", "심근경색의치료류", "복부손상수술", "사지절단의수술", "응급내시경", "응급투석", "조산산모", "신생아", "중증화상"];
  const conditions = ["심근경색증", "허혈성뇌졸중", "출혈성뇌졸중", "대동맥박리", "패혈증", "중증화상", "조산아", "저체중아", "간질적응 상태", "뇌수막염"];

  const equipment = [
    `응급실 성인 병상: 총 ${hospitalDetails.emergencyRoomDefault} 병상 중 ${hospitalDetails.emergencyRoomAvailable} 병상 이용 가능`,
    `응급실 소아 병상: 총 ${hospitalDetails.emergencyRoomChildDefault} 병상 중 ${hospitalDetails.emergencyRoomChildAvailable} 병상 이용 가능`,
    `일반 수술실: ${hospitalDetails.operatingRoom} 병상 이용 가능`,
    `신경외과 수술실: ${hospitalDetails.neurosurgeryRoom} 병상 이용 가능`,
    `신생아 중환자실: ${hospitalDetails.neonatalRoom} 병상 이용 가능`,
    `흉부 외과 중환자실: ${hospitalDetails.thoracicRoom} 병상 이용 가능`,
    `일반 병실: ${hospitalDetails.generalRoom} 병상 이용 가능`,
    `CT: ${hospitalDetails.ct ? '사용 가능' : '사용 불가능'}`,
    `MRI: ${hospitalDetails.mri ? '사용 가능' : '사용 불가능'}`,
    `혈관촬영기: ${hospitalDetails.angiography ? '사용 가능' : '사용 불가능'}`,
    `인공호흡기: ${hospitalDetails.ventilator ? '사용 가능' : '사용 불가능'}`,
    `CRRT: ${hospitalDetails.crrt ? '사용 가능' : '사용 불가능'}`,
    `ECMO: ${hospitalDetails.ecmo ? '사용 가능' : '사용 불가능'}`,
  ];

  return (
    <div className="modal-overlay full-screen">
      <div className="modal-content">
        <button className="back-button" onClick={onClose}>
          돌아가기
        </button>
        <Title hospitalName={hospitalDetails.hospitalName || "병원 이름"} />
        <Section title="시설명 기준 현재 가능 응급의" items={doctors} />
        <Section title="중증 질환별 수용 가능 여부" items={conditions} />
        <Section title="실시간 가용 병상 및 장비 현황" items={equipment} />
      </div>
    </div>
  );
};

export default DetailInfoModal;
