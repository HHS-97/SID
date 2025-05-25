import axios from "axios"
import { onMessage } from "firebase/messaging"
import { AnimatePresence, motion } from "framer-motion"
import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { useRecoilState } from "recoil"
import Swal from "sweetalert2"
import ProfileImage from "../assets/ProfileImage"
import { notificationCountAtom } from "../atoms/notificationAtom"
import { userAtom } from "../atoms/userAtom"
import { messaging } from "../firebase/firebase" // Firebase 설정 파일

const Notification = () => {
	const [user, setUser] = useRecoilState(userAtom)
	const [notifs, setNotifs] = useState([])
	const [notificationCount, setNotificationCount] = useRecoilState(notificationCountAtom)
	const navigate = useNavigate()

	// 알림 목록 가져오기
	const fetchNotifications = async () => {
		try {
			const response = await axios.get(
				import.meta.env.VITE_BASE_URL + "/notification/summaryNoti",
				{ withCredentials: true },
			)

			setNotifs((prev) => {
				if (JSON.stringify(prev) !== JSON.stringify(response.data)) {
					//console.log("🟢 알림 목록 업데이트됨!", response.data)
					setNotificationCount(response.data.length)
					return response.data
				}
				//console.log("🟡 알림 목록 동일해서 업데이트 안함")
				return prev
			})
		} catch (error) {
			//console.log(error)
		}
	}

	// 모든 알림 읽음 처리
	const clearNotifications = async () => {
		try {
			await axios.post(
				`${import.meta.env.VITE_BASE_URL}/notification/markAllAsRead`,
				{},
				{ withCredentials: true },
			)
			setNotifs([])
			setNotificationCount(0)
		} catch (error) {
			console.error(
				"Error marking all notifications as read:",
				error.response ? error.response.data : error.message,
			)
		}
	}

	// 특정 알림 삭제
	const removeNotification = (id) => {
		setNotifs((prev) => prev.filter((notif) => notif.notificationId !== id))
		setNotificationCount((prev) => prev - 1)
	}

	const markAsRead = async (id, notif) => {
		try {
			await axios.post(
				`${import.meta.env.VITE_BASE_URL}/notification/markAsRead?id=${id}`,
				{},
				{ withCredentials: true },
			)
			removeNotification(id)
	
			if (notif.type === "posts") {
				// 받은 프로필과 현재 프로필이 동일하면 바로 이동
				if (user.lastProfile.nickname === notif.receiver.receiverNickname) {
					navigate("/profile/" + notif.sender.senderNickname)
				} else {
					// 받은 프로필과 현재 프로필이 다르면 프로필 변경 여부 선택
					Swal.fire({
						title: "프로필 변경",
						html: "현재 프로필로 받은 알림이 아닙니다.<br/>프로필을 변경하시겠습니까?",
						icon: "warning",
						showCancelButton: true,
						confirmButtonText: "네",
						cancelButtonText: "아니요",
					}).then((result) => {
						if (!result.isConfirmed) {
							Swal.fire({
								title: "프로필 변경 취소",
								text: "프로필을 변경하지 않고 이동합니다.",
								icon: "warning",
							})
							navigate("/profile/" + notif.sender.senderNickname)
						} else {
							axios
								.patch(
									import.meta.env.VITE_BASE_URL + "/lastprofiles",
									{ nickname: notif.receiver.receiverNickname },
									{
										headers: { "Content-Type": "application/json" },
										withCredentials: true,
									},
								)
								.then((res) => {
									//console.log(res)
									setUser({
										...user,
										lastProfile: {
											nickname: res.data.nickname,
											profileImage: res.data.profileImage,
										},
									})
									navigate("/profile/" + notif.sender.senderNickname)
								})
								.catch((err) => {
									//console.log(err)
									switch (err.status) {
										case 401:
											alert("로그인 만료")
											localStorage.removeItem("user")
											window.location.href = "/user/login"
											break
										default:
											Swal.fire({
												title: "프로필 변경 실패",
												text: "서버 오류입니다.",
												icon: "error",
												showConfirmButton: true,
											})
											break
									}
								})
						}
					})
				}
			} else if (notif.type === "chat") {
				if (user.lastProfile.nickname === notif.receiver.receiverNickname) {
					navigate(`/chat/${notif.room}?roomName=${notif.sender.senderNickname}`)
				}
			} else {
				// 기타 알림 유형
				Swal.fire({
					title: "프로필 변경",
					html: "현재 프로필로 받은 알림이 아닙니다.<br/>프로필을 변경하시겠습니까?",
					icon: "warning",
					showCancelButton: true,
					confirmButtonText: "네",
					cancelButtonText: "아니요",
				}).then((result) => {
					if (!result.isConfirmed) {
						Swal.fire({
							title: "프로필 변경 취소",
							text: "프로필을 변경하지 않고 이동합니다.",
							icon: "warning",
						})
						navigate("/notification")
					} else {
						axios
							.patch(
								import.meta.env.VITE_BASE_URL + "/lastprofiles",
								{ nickname: notif.receiver.receiverNickname },
								{
									headers: { "Content-Type": "application/json" },
									withCredentials: true,
								},
							)
							.then((res) => {
								//console.log(res)
								setUser({
									...user,
									lastProfile: {
										nickname: res.data.nickname,
										profileImage: res.data.profileImage,
									},
								})
								navigate(`/chat/${notif.room}?roomName=${notif.sender.senderNickname}`)
							})
							.catch((err) => {
								//console.log(err)
								switch (err.status) {
									case 401:
										alert("로그인 만료")
										localStorage.removeItem("user")
										window.location.href = "/user/login"
										break
									default:
										Swal.fire({
											title: "프로필 변경 실패",
											text: "서버 오류입니다.",
											icon: "error",
											showConfirmButton: true,
										})
										break
								}
							})
					}
				})
			}
		} catch (error) {
			console.error(
				"Error marking notification as read:",
				error.response ? error.response.data : error.message,
			)
		}
	}

	useEffect(() => {
		// 초기 알림 가져오기
		fetchNotifications()

		// 📌 Foreground에서 Firebase 메시지 수신 처리
		const unsubscribe = onMessage(messaging, (payload) => {
			//console.log("🔥 [Foreground] 새로운 알림 수신!", payload)
			fetchNotifications()
		})

		// 📌 Service Worker 메시지 수신 처리 (Background 알림)
		navigator.serviceWorker.addEventListener("message", (event) => {
			if (event.data && event.data.type === "NEW_NOTIFICATION") {
				//console.log("📩 [React] 서비스 워커에서 새로운 알림 감지!", event.data)
				fetchNotifications() // 알림을 가져오는 함수 호출
			}
		})

		return () => unsubscribe()
	}, [])

	return (
		<div className="max-w-md mx-auto p-4 rounded-lg bg-white/50 relative">
			<div className="flex justify-between items-center mb-4">
				<h2 className="text-lg font-bold">알림</h2>
				{notifs.length > 0 && (
					<button
						className="px-4 py-2 bg-gray-600 text-white text-sm font-semibold rounded-lg shadow-md hover:bg-red-600 transition duration-200"
						onClick={clearNotifications}
					>
						모두 지우기
					</button>
				)}
			</div>
			<div className="space-y-3">
				<AnimatePresence>
					{notifs.length > 0 ? (
						notifs.map((notif) => (
							<motion.div
								key={notif.notificationId}
								initial={{ opacity: 1, x: 0 }}
								animate={{ opacity: 1, x: 0 }}
								exit={{ opacity: 0, x: 50 }}
								transition={{ duration: 0.3 }}
								drag="x"
								dragConstraints={{ left: 0, right: 0 }}
								onDragEnd={(event, info) => {
									if (Math.abs(info.offset.x) > 100) {
										removeNotification(notif.notificationId)
									}
								}}
								onClick={() => markAsRead(notif.notificationId, notif)}
								className="flex items-center justify-between p-3 outline outline-gray-300 rounded-lg shadow-lg cursor-pointer"
							>
								<div className="flex items-center space-x-3">
									<ProfileImage src={notif.sender.senderProfileImage} size="50" />
									<div>
										<h3
											className="font-semibold text-md"
											style={{ textAlign: "start" }}
										>
											{`${notif.sender.senderNickname} > ${notif.receiver.receiverNickname}`}
										</h3>
										<p
											className="text-sm text-gray-700"
											style={{ textAlign: "start" }}
										>
											{notif.title}
										</p>
										<p
											className="text-xs text-gray-500"
											style={{ textAlign: "start" }}
										>
											{notif.body.length > 15
												? notif.body.slice(0, 15) + "..."
												: notif.body}
										</p>
									</div>
								</div>
								<p className="text-xs text-gray-500">{notif.createdAt}</p>
							</motion.div>
						))
					) : (
						<p className="text-center text-gray-500">알림이 없습니다...!</p>
					)}
				</AnimatePresence>
			</div>
		</div>
	)
}

export default Notification
