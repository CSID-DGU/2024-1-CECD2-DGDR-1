const API_BASE_URL_1 = 'https://17e0-58-236-188-94.ngrok-free.app';
const API_BASE_URL_2 = 'http://whitex.iptime.org:4000';

const getApiBaseUrl = (feature) => {
  switch (feature) {
    case 'modal':
      return API_BASE_URL_2; // 데이터 조회 API
    default:
      return API_BASE_URL_1; // 기본값으로 API_BASE_URL_1 사용
  }
};

export { getApiBaseUrl };