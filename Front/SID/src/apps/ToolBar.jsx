import axios from "axios"
import { useEffect, useMemo, useState } from "react"
import { Link, useNavigate } from "react-router-dom"
import { Tooltip } from "react-tooltip"
import { useRecoilState, useRecoilValue } from "recoil"
import Swal from "sweetalert2"
// import { motion } from "framer-motion"
import { motion } from "framer-motion"
import { CircleMenu, CircleMenuItem } from "react-circular-menu"
import ProfileImage from "../assets/ProfileImage"
import { isLoginSelector, userAtom } from "../atoms/userAtom"

const ToolBar = () => {
	const [user, setUser] = useRecoilState(userAtom)
	const isLogin = useRecoilValue(isLoginSelector)
	const navigate = useNavigate()
	const [showProfileList, setShowProfileList] = useState(false)
	const [isCalendarClicked, setIsCalendarClicked] = useState(false)
	const [isTrendClicked, setIsTrendClicked] = useState(false)
	const [isHomeClicked, setIsHomeClicked] = useState(false)
	const [isFollowClicked, setIsFollowClicked] = useState(false)

	const handleHomeClick = () => {
		setIsHomeClicked(true)
		setTimeout(() => {
			setIsHomeClicked(false)
		}, 2000)
	}
	const handleFollowClick = () => {
		setIsFollowClicked(true)
		setTimeout(() => {
			setIsFollowClicked(false)
		}, 2000)
	}
	const handleCalendarClick = () => {
		setIsCalendarClicked(true)
		setTimeout(() => {
			setIsCalendarClicked(false)
		}, 2000)
	}
	const handleTrendClick = () => {
		setIsTrendClicked(true)
		setTimeout(() => {
			setIsTrendClicked(false)
		}, 2000)
	}

	const changeProfile = (nickname) => {
		if (user.lastProfile.nickname !== nickname) {
			axios
				.patch(
					import.meta.env.VITE_BASE_URL + "/lastprofiles",
					{ nickname },
					{ headers: { "Content-Type": "application/json" }, withCredentials: true },
				)
				.then((res) => {
					//console.log(res)
					setUser({
						...user,
						lastProfile: {
							nickname: res.data.nickname,
							profileImage: res.data.profileImage
								? res.data.profileImage
								: user.profileList.find(
										(profile) => profile.nickname === res.data.nickname,
									).profileImage,
						},
					})
					setShowProfileList(false)
					window.location.reload()
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
								title: "계정 조회 실패",
								icon: "error",
								showConfirmButton: true,
							})
							break
					}
				})
		} else {
			Swal.fire({
				icon: "info",
				text: "현재 프로필입니다",
				timer: 1000,
				showConfirmButton: false,
			})
			setShowProfileList(false)
		}
	}

	const items = useMemo(() => {
		const profiles = user.profileList.map((profile, idx) => (
			<CircleMenuItem
				key={profile.nickname}
				size={50}
				tooltip={profile.nickname}
				onClick={() => changeProfile(profile.nickname)}
				tooltipPlacement="top"
				style={{
					border: "none",
					backgroundColor: "transparent",
					width: "50px",
					height: "50px",
				}}
				className="myRadial"
			>
				<ProfileImage
					size="50"
					src={profile.profileImage}
					except={{ profileKey: idx + 1 }}
				/>
			</CircleMenuItem>
		))
		if (user.profileList.length < 5) {
			profiles.push(
				<CircleMenuItem
					key="addProfile"
					size={50}
					tooltip="프로필 추가"
					onClick={() => {
						navigate("/profile/createprofile")
						setShowProfileList(false)
					}}
					tooltipPlacement="top"
					style={{
						border: "1px solid #ccc",
						backgroundColor: "#f0f0f0",
						width: "40px",
						height: "40px",
					}}
					className="myRadial"
				>
					➕
				</CircleMenuItem>,
			)
		}
		return profiles
	}, [user.profileList])

	useEffect(() => {
		function handleClickOutside(e) {
			if (e.target.closest(".myRadial")) {
				return
			} else {
				setShowProfileList(false)
			}
		}

		// 바깥 클릭 이벤트 리스너 추가
		document.addEventListener("click", handleClickOutside)
		return () => {
			// 컴포넌트가 언마운트될 때 이벤트 리스너 제거
			document.removeEventListener("click", handleClickOutside)
		}
	}, [])

	return (
		<div className="fixed bottom-0 h-[60px] w-full relative bg-soft-beige animate-bg safe-bottom z-[900]">
			{showProfileList && <></>}

			<div className="px-1 py-1 flex justify-evenly border-t-2 border-gray-200 text-xl">
				<span className="my-auto">
					<Link to="/">
						<motion.div
							className="relative w-7 h-7"
							animate={{
								scale: isHomeClicked ? [1, 1.1, 1] : 1, // 클릭 시 크기 변화
								y: isHomeClicked ? [0, 0, 5, 0] : 0, // 클릭 시 상하 이동
								filter: isHomeClicked
									? ["brightness(1)", "brightness(1.2)", "brightness(1)"]
									: "brightness(1)", // 클릭 시 밝기 변화
							}}
							transition={{
								duration: 1,
								repeat: 1,
								ease: "easeInOut",
							}}
							onClick={handleHomeClick}
						>
							<img src="/buttonIcon/homeIcon.png" alt="Home Icon" />
						</motion.div>
					</Link>
				</span>
				<span className="my-auto">
					<Link to="/feed/profile">
						<motion.div
							className="relative w-8 h-8"
							animate={{
								scale: isFollowClicked ? [1, 1.1, 1] : 1, // 클릭 시 크기 변화
								y: isFollowClicked ? [0, 0, 5, 0] : 0, // 클릭 시 상하 이동
								filter: isFollowClicked
									? ["brightness(1)", "brightness(1.2)", "brightness(1)"]
									: "brightness(1)", // 클릭 시 밝기 변화
							}}
							transition={{
								duration: 1,
								repeat: 1,
								ease: "easeInOut",
							}}
							onClick={handleFollowClick}
						>
							<img src="/buttonIcon/recommendAndFollow.png" alt="Follow Icon" />
						</motion.div>
					</Link>
				</span>
				{isLogin && user.lastProfile && (
					<div className="relative">
						<button
							className={
								showProfileList
									? "absolute bottom-0 left-1/2 transform -translate-x-1/2 myRadial"
									: "hidden myRadial"
							}
							data-tooltip-id="my-profile"
							data-tooltip-content={`${user.lastProfile.nickname} 프로필 페이지`}
							data-tooltip-place="top"
							style={{
								zIndex: 1000,
							}}
							onClick={() => {
								navigate(`/profile/${user.lastProfile.nickname}`)
								setShowProfileList(false)
							}}
						>
							<ProfileImage
								size="75"
								src={user.lastProfile.profileImage}
								except={{
									profileKey:
										user.profileList.findIndex(
											(profile) =>
												profile.nickname === user.lastProfile.nickname,
										) + 1,
								}}
							/>
						</button>
						<Tooltip id="my-profile" />
						<CircleMenu
							startAngle={-150}
							rotationAngle={120}
							radius={10}
							open={showProfileList}
							onMenuToggle={() => setShowProfileList(!showProfileList)}
							menuToggleElement={
								<span
									data-tooltip-id="now-profile"
									data-tooltip-content={user.lastProfile.nickname}
									data-tooltip-place="top"
								>
									<ProfileImage
										size="50"
										src={user.lastProfile.profileImage}
										except={{
											profileKey:
												user.profileList.findIndex(
													(profile) =>
														profile.nickname ===
														user.lastProfile.nickname,
												) + 1,
										}}
									/>
								</span>
							}
							className="absolute bottom-0 left-1/2 transform -translate-x-1/2 myRadial"
						>
							{items}
						</CircleMenu>
						<Tooltip id="now-profile" />
					</div>
				)}
				<span className="my-auto">
					<Link to="/calendar">
						<motion.div
							className="relative w-7 h-7"
							animate={{
								scale: isCalendarClicked ? [1, 1.1, 1] : 1, // 클릭 시 크기 변화
								y: isCalendarClicked ? [0, 0, 5, 0] : 0, // 클릭 시 상하 이동
								filter: isCalendarClicked
									? ["brightness(1)", "brightness(1.2)", "brightness(1)"]
									: "brightness(1)", // 클릭 시 밝기 변화
							}}
							transition={{
								duration: 1,
								repeat: 1,
								ease: "easeInOut",
							}}
							onClick={handleCalendarClick}
						>
							<img src="/buttonIcon/calendarIcon.png" alt="Calendar Icon" />
						</motion.div>
					</Link>
				</span>
				<span className="my-auto">
					<Link to="/feed/trend">
						<motion.div
							className="relative w-7 h-7"
							animate={{
								scale: isTrendClicked ? [1, 1.1, 1] : 1, // 클릭 시 크기 변화
								y: isTrendClicked ? [0, 0, 5, 0] : 0, // 클릭 시 상하 이동
								filter: isTrendClicked
									? ["brightness(1)", "brightness(1.2)", "brightness(1)"]
									: "brightness(1)", // 클릭 시 밝기 변화
							}}
							transition={{
								duration: 1,
								repeat: 1,
								ease: "easeInOut",
							}}
							onClick={handleTrendClick}
						>
							<img src="/buttonIcon/noActiveTrendIcon.png" alt="Trend Icon" />
						</motion.div>
					</Link>
				</span>
			</div>
		</div>
	)
}

export default ToolBar
