import axios from "axios";

export const SimilarityFromAPI = async (post_id) => {
  try {
    const response = await axios.get(
      `${process.env.REACT_APP_CURATING_API_URL}/similarity?post_id=${post_id}`
    );
    console.log("서버에서 받은 유사글 데이터:", response.data);
    return response.data; // response.data를 반환
  } catch (error) {
    console.error("비슷한 게시글 불러오기 실패", error);
    return [];
  }
};
