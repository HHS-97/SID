export const loadDataFromAPI = async () => {
  try {
    const response = await fetch(`${process.env.REACT_APP_CURATING_API_URL}/data/all`);

    if (!response.ok) {
      throw new Error(`데이터 요청 실패: ${response.status}`);
    }

    const data = await response.json();
    // console.log("서버에서 받은 데이터:", data);  // 응답 데이터 확인

    // JSON 문자열을 객체 배열로 변환
    return {
      userData: data.user_data ? JSON.parse(data.user_data) : [],
      interestData: data.interest_data ? JSON.parse(data.interest_data) : [],
      categoryData: data.category_data ? JSON.parse(data.category_data) : [],
      postData: data.post_data ? JSON.parse(data.post_data) : [],
      interactionData: data.interaction_data ? JSON.parse(data.interaction_data) : [],
      commentData: data.comments ? JSON.parse(data.comments) : []
    };
  } catch (error) {
    console.error("데이터 로드 중 오류 발생:", error);
    return { 
      userData: [], 
      interestData: [], 
      categoryData: [], 
      postData: [], 
      interactionData: [],
      commentData: [], 
    };
  }
};
