import { initializeApp } from 'firebase/app';
import { getMessaging } from 'firebase/messaging';


const firebaseConfig = {
    apiKey: "AIzaSyBBhSXXhwza87qjZ2aAD6hqXC5th7PUEhE",
    authDomain: "sidproject-846a0.firebaseapp.com",
    projectId: "sidproject-846a0",
    storageBucket: "sidproject-846a0.firebasestorage.app",
    messagingSenderId: "947340635193",
    appId: "1:947340635193:web:a41dc389a8a3df451371e8",
    measurementId: "G-NWZHDBK8KY"
  };

const firebaseApp = initializeApp(firebaseConfig);

const messaging = getMessaging(firebaseApp);

// const analytics = getAnalytics(app);

export { firebaseApp, messaging};