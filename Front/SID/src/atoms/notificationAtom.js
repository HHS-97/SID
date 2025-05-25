import { atom } from 'recoil';

export const notificationCountAtom = atom({
  key: 'notificationCount',
  default: 0, // 기본값 설정
});