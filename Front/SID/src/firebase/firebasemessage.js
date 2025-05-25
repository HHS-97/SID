import { onMessage, getToken } from 'firebase/messaging';
import { messaging } from './firebase.js';
import { postFcmToken } from './notification.js'

const vapidKey = 'BLAkCjTnnjDfgWUERYmrOhSZ1F0lzZ7-MMbRNsLs5c4L9Z-umCNTkIgeyM43q4QROa4IjdhT84UWaAJ20fQ6reI';

//알림 전송
export const initializeForegroundNotifications = () => {
    onMessage(messaging, (payload) => {
      //console.log('Message received in foreground:', payload);
  
      if (!payload.data) {
        //console.log('No notification object in payload, skipping...');
        return;
      }
  
      const notificationTitle = payload.data.title;
      const notificationOptions = {
        body: payload.data.body,
        icon: payload.data.icon,
        data: payload.data,
      };
  
      if (
        Notification.permission === 'granted' &&
        document.visibilityState === 'visible'
      ) {
        new Notification(notificationTitle, notificationOptions);
      }
    });
  };

// 웹페이지 알림 권한 요청
export const requestNotificationPermission = async () => {
    try {
      const permission = await Notification.requestPermission();
      if (permission === 'granted') {
        //console.log('Notification permission granted.');
      } else {
        console.error('Notification permission not granted.');
      }
    } catch (error) {
      console.error(
        'An error occurred while requesting notification permission:',
        error
      );
    }
  };

  // FCM 토큰 가져오는 방법

  export const getFCMToken = async () => {
    const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
  
    try {
      await delay(1000);
      const token = await getToken(messaging, {
        vapidKey: vapidKey,
      });
  
      if (token) {
        //console.log('나는 토큰을 백에 보냈어')
        await postFcmToken(token);
        //postFcmToken을 만들어서 백에 이걸 저장하도록 해야 한다.
        //console.log('FCM Token saved:', token);
        return token;
      } else {
        console.warn(
          'No FCM token available. Request notification permissions to generate one.'
        );
      }
    } catch (error) {
      console.error('An error occurred while retrieving FCM token:', error);
    }
  };

  //권한 요청받고 token을 얻어옴
  export const handleEnableNotifications = async () => {
    await requestNotificationPermission();
    const token = await getFCMToken();
    //console.log('여기는 handle인데 token이 들어 왔어')
    if (token) //console.log('FCM Token:', token);
    initializeForegroundNotifications();
  };