import Papa from 'papaparse';

export const loadCSVData = (url) => {
  return new Promise((resolve, reject) => {
    Papa.parse(url, {
      download: true,
      header: true, // CSV의 첫 줄을 header로 인식
      dynamicTyping: true, // 숫자 등 자동 변환
      complete: (results) => {
        resolve(results.data);
      },
      error: (error) => {
        reject(error);
      },
    });
  });
};
