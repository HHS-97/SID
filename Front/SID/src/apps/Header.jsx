import axios from "axios"
import { AnimatePresence, motion } from "framer-motion"
import { useEffect, useRef, useState } from "react"
import { Link, useNavigate } from "react-router-dom"
import { useRecoilState, useRecoilValue, useResetRecoilState } from "recoil"
import Swal from "sweetalert2"
import { notificationCountAtom } from "../atoms/notificationAtom"
import { isLoginSelector, userAtom } from "../atoms/userAtom"
import { onMessage } from "firebase/messaging"
import { messaging } from "../firebase/firebase"

const Header = () => {
	const resetUser = useResetRecoilState(userAtom)
	const isLogin = useRecoilValue(isLoginSelector)
	const navigate = useNavigate()
	const [viewKebab, setViewKebab] = useState(false)
	const kebabRef = useRef()
	const [notificationCount, setNotificationCount] = useRecoilState(notificationCountAtom)

	const toggleKebab = (e) => {
		setViewKebab((prev) => !prev)
	}

	const handleLogout = (e) => {
		e.preventDefault()
		if (isLogin) {
			axios
				.delete(import.meta.env.VITE_BASE_URL + "/cookie", { withCredentials: true })
				.then((res) => {
					//console.log(res)
					resetUser()
					setNotificationCount(0)
					navigate("/user/login")
					localStorage.removeItem("hasVisited", "false") // 방문 기록 초기화
				})
				.catch((err) => {
					//console.log(err)
					Swal.fire({
						icon: "error",
						title: "서버 오류",
						text: "로그아웃에 실패했습니다.",
						showConfirmButton: false,
						timer: 1500,
					})
				})
		} else {
			Swal.fire({
				icon: "error",
				title: "잘못된 접근입니다.",
				showConfirmButton: false,
				timer: 1500,
			})
		}
		setViewKebab(false)
	}

	useEffect(() => {
		function handleClickOutside(event) {
			if (kebabRef.current && !kebabRef.current.contains(event.target)) {
				setViewKebab(false)
			}
		}

		document.addEventListener("mousedown", handleClickOutside)
		return () => {
			document.removeEventListener("mousedown", handleClickOutside)
		}
	}, [kebabRef])

	const fetchNotificationCount = async () => {
		try {
			const res = await axios.get(import.meta.env.VITE_BASE_URL + "/notification/countNoti", {
				withCredentials: true,
			})
			setNotificationCount(res.data.count)
		} catch (err) {
			console.error(err)
		}
	}

	const kebabVariants = {
		hidden: { opacity: 0, y: -10 },
		visible: {
			opacity: 1,
			y: 0,
			transition: {
				staggerChildren: 0.1, // 각 항목이 0.1초 간격으로 순차 등장
			},
		},
		exit: { opacity: 0, scale: 0.9, y: -10, transition: { duration: 0.2 } },
	}

	const itemVariants = {
		hidden: { opacity: 0, y: -10 },
		visible: { opacity: 1, y: 0, transition: { duration: 0.2 } },
	}

	useEffect(() => {
		if (isLogin) {
			fetchNotificationCount()
		} else {
			setNotificationCount(0) // 로그아웃 상태일 때 알림 개수를 0으로 초기화
		}
	}, [isLogin])

	useEffect(() => {
		const unsubscribe = onMessage(messaging, (payload) => {
			//console.log("🔥 [Foreground] 새로운 알림 수신!", payload)
			fetchNotificationCount() // ✅ 이제 정상적으로 호출 가능
		})

		return () => unsubscribe()
	}, [])

	return (
		<div className="px-3 py-2 flex justify-center items-center border-b-2 border-gray-200 text-xl relative bg-soft-beige animate-bg">
			<div className="flex-grow flex ml-4 text-3xl">
				<Link
					to={"/"}
					onClick={() => {
						window.location.href = "/"
					}}
				>
					<motion.p
						initial={{ opacity: 0 }}
						animate={{ opacity: 1 }}
						transition={{ delay: 1, duration: 1 }}
						className="text-3xl font-extrabold mt-3 font-custom"
						style={{
							backgroundImage:
								"linear-gradient(90deg, rgb(159, 42, 46), rgb(255, 119, 129), rgb(255, 158, 167), rgb(255, 193, 204), rgb(255, 132, 145))",
							backgroundSize: "400% 400%",
							WebkitBackgroundClip: "text",
							WebkitTextFillColor: "transparent",
							animation: "gradientAnimation 8s ease infinite",
						}}
					>
						S Identity
					</motion.p>
				</Link>

				<style>{`
					.font-custom {
						font-family: "Great Vibes", cursive;
					}
					@keyframes gradientAnimation {
						0% {
							background-position: 0% 50%;
						}
						50% {
							background-position: 100% 50%;
						}
						100% {
							background-position: 0% 50%;
						}
					}
				`}</style>
			</div>

			<span>
				<Link to="/notification">
					<motion.div
						className="relative w-6 h-6 mr-5"
						whileTap={{
							scale: [1, 1.3, 1], // 클릭 시 1 -> 1.3 -> 1로 변경
							filter: ["brightness(1)", "brightness(1.1)", "brightness(1)"],
						}}
						transition={{
							duration: 0.6,
							ease: "easeOut",
						}}
					>
						<img
							src="/buttonIcon/notificationIcon.png"
							alt="Notification Icon"
							className="w-full h-full object-contain"
						/>
						{notificationCount > 0 && (
							<span
								className="bg-red-500 text-white rounded-full text-xs px-1 absolute"
								style={{ top: "-5px", right: "-10px" }}
							>
								{notificationCount}
							</span>
						)}
					</motion.div>
				</Link>
			</span>
			<div></div>
			<span>
				<Link to="/chat/chatroomlist">
					<motion.div
						className="relative w-6 h-6 mr-5"
						whileTap={{
							scale: [1, 1.3, 1], // 클릭 시 1 -> 1.3 -> 1로 변경
							filter: ["brightness(1)", "brightness(1.1)", "brightness(1)"],
						}}
						transition={{
							duration: 0.6,
							ease: "easeOut",
						}}
					>
						<img
							src="/buttonIcon/dmIcon.png"
							alt="Dm Icon"
							className="w-full h-full object-contain"
						/>
					</motion.div>
				</Link>
			</span>
			<span onClick={toggleKebab} ref={kebabRef}>
				<motion.div
					className="relative w-6 h-6 mr-2"
					whileTap={{
						scale: [1, 1.3, 1], // 클릭 시 1 -> 1.3 -> 1로 변경
						filter: ["brightness(1)", "brightness(1.1)", "brightness(1)"],
					}}
					transition={{
						duration: 0.6,
						ease: "easeOut",
					}}
				>
					<img
						src="/buttonIcon/kebobIcon.png"
						alt="Kebob Icon"
						className="w-full h-full object-contain"
					/>
				</motion.div>
			</span>
			<AnimatePresence>
				{viewKebab && (
					<motion.div
						className="absolute top-14 right-4 shadow-lg p-2 rounded-lg flex flex-col z-[998]"
						ref={kebabRef}
						variants={kebabVariants}
						initial="hidden"
						animate="visible"
						exit="exit"
						style={{
							border: "1px solid rgb(231, 144, 187)", // 핑크 보더
							backgroundImage:
								"linear-gradient(135deg, rgb(255, 182, 193), rgb(255, 255, 255))", // 연한 핑크 배경 (알파값 제거)
							backgroundSize: "200% 200%",
							backgroundPosition: "center",
							borderRadius: "12px",
							color: "rgb(94, 32, 62)", // 글자색 (불투명하게)
						}}
					>
						{isLogin ? (
							<>
								<motion.span variants={itemVariants} onClick={handleLogout}>
									로그아웃
								</motion.span>
								<hr className="opacity-30" />
								<motion.span variants={itemVariants}>
									<Link to="/user/userInfo">계정 설정</Link>
								</motion.span>
							</>
						) : (
							<>
								<motion.span variants={itemVariants}>
									<Link to="/user/login">로그인</Link>
								</motion.span>
								<motion.span variants={itemVariants}>
									<Link to="/user/signup">가입하기</Link>
								</motion.span>
							</>
						)}
					</motion.div>
				)}
			</AnimatePresence>
		</div>
	)
}

export default Header
