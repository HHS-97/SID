import axios from "axios"

// export const postFcmToken = (token) => {
//     try {
//         const baseURL = import.meta.env.VITE_BASE_URL;
//         //console.log('여기는 notification.js 입니다.')
//         axios.post(
//             `${baseURL}/api/notification/fcm-token`,
//             { fcmToken: token },
//             {
//               withCredentials: true,
//             }
            
//           );
          
//     } catch(error) {
//         //console.log(error)
//     }
// }


export const postFcmToken = async (token) => {
    try {
        //console.log('여기는 Post입니다')
        //console.log(token)
      const baseURL = import.meta.env.VITE_BASE_URL;
      const { data } = await axios.post(`${baseURL}/notification/fcm-token`, null, {
        params: {
            fcmToken: token
        },
        withCredentials: true,
    });
    //console.log(data)
      return data.response === 'FCM 토큰이 성공적으로 등록되었습니다.';
    } catch (e) {
      //console.log(e);
      return false;
    }
  };
