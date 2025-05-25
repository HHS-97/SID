import { useState, useEffect } from "react"
import { CookiesProvider } from "react-cookie"
import { Route, Routes } from "react-router-dom"
import { motion } from "framer-motion"
import { onMessage } from "firebase/messaging"
import { messaging } from "../src/firebase/firebase"
import { useRecoilState, useRecoilValue } from "recoil"
import { notificationCountAtom } from "../src/atoms/notificationAtom"
import axios from "axios"

import "./App.css"
import Header from "./apps/Header"
import ToolBar from "./apps/ToolBar"
import FallingCherryBlossom from "./components/designs/FallingCherryBlossom"
import KakaoRedirect from "./components/users/signups/KakaoRedirect"
import NaverRedirect from "./components/users/signups/NaverRedirect"
import Home from "./pages/Home"
import MyCalendar from "./pages/MyCalendar"
import Notification from "./pages/Notification"
import CreateProfile from "./pages/profiles/CreateProfile"
import ProfileFeed from "./pages/profiles/ProfileFeed"
import UserProfile from "./pages/profiles/UserProfile"
import TrendFeed from "./pages/TrendFeed"
import Login from "./pages/users/Login"
import SignUp from "./pages/users/SignUp"
import UserInfo from "./pages/users/UserInfo"
import PrivateRoutes from "./utils/PrivateRoutes"
import PublicRoutes from "./utils/PublicRoutes"
import Reset from "./utils/Reset"
import ChatRoom from "./pages/chatings/ChatRoom"
import ChatRoomList from "./pages/chatings/ChatRoomList"
import NotFound from "./pages/NotFound"
import SocialTerms from "./pages/users/SocialTerms"
import { isLoginSelector } from "../src/atoms/userAtom"

function App() {
	const [isLoading, setIsLoading] = useState(false)
	const [fadeOut, setFadeOut] = useState(false)
	const text = "SID"
	const letters = text.split("")
	const [notificationCount, setNotificationCount] = useRecoilState(notificationCountAtom)
	const isLogin = useRecoilValue(isLoginSelector)

	const fetchNotificationCount = async () => {
		try {
			const res = await axios.get(import.meta.env.VITE_BASE_URL + "/notification/countNoti", {
				withCredentials: true,
			})
			setNotificationCount(res.data.count)
			//console.log("여긴 데이터 개수", res.data.count)
		} catch (err) {
			console.error(err)
		}
	}

	useEffect(() => {
		if (isLogin) {
			fetchNotificationCount()
			//console.log('islogin', notificationCount)
		} else {
			setNotificationCount(0) // 로그아웃 상태일 때 알림 개수를 0으로 초기화
		}
	}, [isLogin])

	useEffect(() => {
		const unsubscribe = onMessage(messaging, (payload) => {
			//console.log("🔥 [Foreground] 새로운 알림 수신!", payload)
			
			fetchNotificationCount() // ✅ 이제 정상적으로 호출 가능
			//console.log('fcm', notificationCount)
		})

		return () => unsubscribe()
	}, [])

	useEffect(() => {
		setTimeout(() => setFadeOut(true), 3000)
	}, [])

	useEffect(() => {
		const hasVisited = localStorage.getItem("hasVisited")

		if (!hasVisited) {
			setIsLoading(true)
			localStorage.setItem("hasVisited", "true")

			const timer = setTimeout(() => {
				setFadeOut(true)
				setTimeout(() => setIsLoading(false), 800)
			}, 2000)

			return () => clearTimeout(timer)
		}
	}, [])

	return (
		<CookiesProvider>
			<div className="relative w-full h-dvh flex flex-col items-center justify-center overflow-hidden">
				{isLoading ? (
					<div
						className={`absolute w-full h-full flex flex-col items-center justify-center transition-opacity duration-500 backdrop-blur-md ${
							fadeOut ? "opacity-0 bg-white/80" : "opacity-100 bg-pink-100"
						}`}
					>
						{/* 첫 번째 애니메이션: 한 글자씩 등장 */}
						<div className="flex">
							{letters.map((char, index) => (
								<motion.span
									key={index}
									initial={{ opacity: 0, y: 10 }}
									animate={{ opacity: 1, y: 0 }}
									transition={{ delay: index * 0.2, duration: 0.5 }}
									className="text-6xl font-bold text-pink-500 mx-1 font-custom"
								>
									{char}
								</motion.span>
							))}
						</div>

						{/* 두 번째 애니메이션: "5 Identity" 점점 진하게 */}
						<motion.p
							initial={{ opacity: 0 }}
							animate={{ opacity: 1 }}
							transition={{ delay: 1, duration: 1 }}
							className="text-4xl font-extrabold text-gray-800 mt-3"
						>
							5 Identity
						</motion.p>
						<style jsx>{`
							.font-custom {
								font-family: "Great Vibes", cursive;
							}
						`}</style>
					</div>
				) : (
					<div className="relative z-10 max-w-[500px] w-full h-full flex flex-col bg-white/80 backdrop-blur-md shadow-xl rounded-xl overflow-hidden">
						<div className="absolute inset-0 z-0 pointer-events-none">
							<FallingCherryBlossom />
						</div>
						<Header />
						<div className="flex-1 p-4 overflow-y-auto hide-scrollbar bg-soft-beige animate-bg">
							<Routes>
								<Route path="/" element={<Home />} />
								<Route path="/reset" element={<Reset />} />
								<Route path="/*" element={<NotFound />} />
								<Route element={<PublicRoutes />}>
									<Route path="/user/login" element={<Login />} />
									<Route path="/user/socialterms" element={<SocialTerms />} />
									<Route path="/user/signup" element={<SignUp />} />
									<Route
										path="/login/oauth2/code/kakao"
										element={<KakaoRedirect />}
									/>
									<Route
										path="/login/oauth2/code/naver"
										element={<NaverRedirect />}
									/>
								</Route>
								<Route element={<PrivateRoutes />}>
									<Route path="/user/userinfo" element={<UserInfo />} />
									<Route
										path="/profile/:nickname"
										element={<UserProfile key={location.pathname} />}
									/>
									<Route
										path="/profile/createprofile"
										element={<CreateProfile />}
									/>
									<Route path="/chat/chatroomlist" element={<ChatRoomList />} />
									<Route path="/chat/:chatroomId" element={<ChatRoom />} />
									<Route path="/notification" element={<Notification />} />
									<Route path="/calendar" element={<MyCalendar />} />
									<Route path="/feed/profile" element={<ProfileFeed />} />
									<Route path="/feed/trend" element={<TrendFeed />} />
								</Route>
							</Routes>
						</div>
						<ToolBar />
					</div>
				)}
			</div>
		</CookiesProvider>
	)
}

export default App
