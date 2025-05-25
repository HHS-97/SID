self.importScripts(
  "https://www.gstatic.com/firebasejs/9.20.0/firebase-app-compat.js"
);
self.importScripts(
  "https://www.gstatic.com/firebasejs/9.20.0/firebase-messaging-compat.js"
);

const firebaseConfig = {
  apiKey: "AIzaSyBBhSXXhwza87qjZ2aAD6hqXC5th7PUEhE",
  authDomain: "sidproject-846a0.firebaseapp.com",
  projectId: "sidproject-846a0",
  storageBucket: "sidproject-846a0.appspot.com",
  messagingSenderId: "947340635193",
  appId: "1:947340635193:web:a41dc389a8a3df451371e8",
  measurementId: "G-NWZHDBK8KY"
};

firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();

if (!self.firebaseMessagingInitialized) {
  self.firebaseMessagingInitialized = true;

  messaging.onBackgroundMessage((payload) => {
    //console.log("📩 [Service Worker] 백그라운드 메시지 수신:", payload);

    let authorName = null;
    let title = "";

    if (payload.data.type === "posts") {
      title = payload.data.title;
      authorName = title.split("님이 글을 작성하셨습니다.")[0];
    } else if (payload.data.type === "chat") {
      title = payload.data.title;
      //console.log(title)
      //console.log(payload.data.title)
      authorName = payload.data.title.split("님이 채팅을 보내셨습니다.")[0];
      //console.log("채팅부분", authorName);
    }

    let notificationTitle = title;
    let notificationOptions = {
      body: payload.data.body,
      image: payload.data.image,
      read: payload.data.read,
      referenceId: payload.data.referenceId,
      type: payload.data.type,
      userId: payload.data.userId,
      authorName: authorName,
      room: payload.data.room,
      data: {
        authorName: authorName,
        type: payload.data.type,
        room: payload.data.room
      }
    };

    //console.log("알림 제목:", notificationTitle);
    //console.log("알림 옵션:", notificationOptions);

    self.registration.showNotification(notificationTitle, notificationOptions)
      .then(() => {
        //console.log("알림이 성공적으로 표시되었습니다.");
      })
      .catch((error) => {
        console.error("알림 표시 중 오류 발생:", error);
      });

    clients.matchAll().then((clients) => {
      //console.log("현재 활성화된 클라이언트 수:", clients.length);
      clients.forEach((client) => {
        client.postMessage({ type: "NEW_NOTIFICATION" });
      });
    });
  });
}

// 알림 클릭 이벤트 처리
self.addEventListener("notificationclick", (event) => {
  //console.log("알림 클릭 이벤트 발생!");
  event.notification.close();

  let landingUrl = "";
  let authorName = event.notification.data.authorName;

  if (event.notification.data.type === "chat") {
    // landingUrl = "https://i12c110.p.ssafy.io/chat/chatroomlist";
    landingUrl = `https://i12c110.p.ssafy.io/chat/${event.notification.data.room}?roomName=${authorName}`;
  } else if (event.notification.data.type === "posts") {
    landingUrl = `https://i12c110.p.ssafy.io/profile/${authorName}`;
  }

  event.waitUntil(clients.openWindow(landingUrl));
});
