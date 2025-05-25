import axios from "axios"
import { useEffect } from "react"
import { useNavigate, useNavigationType } from "react-router-dom"
import { useRecoilState } from "recoil"
import Swal from "sweetalert2"
import { userAtom } from "../../atoms/userAtom"
import EmailLogin from "../../components/users/logins/EmailLogin"
import { handleEnableNotifications } from "../../firebase/firebasemessage"

const Login = () => {
	// eslint-disable-next-line unused-imports/no-unused-vars
	const [user, setUser] = useRecoilState(userAtom)
	const navigate = useNavigate()
	const navigationType = useNavigationType()

	const KAKAO_AUTH_URL = `https://kauth.kakao.com/oauth/authorize?client_id=${import.meta.env.VITE_KAKAO_CLIENT_ID}&redirect_uri=${import.meta.env.VITE_FRONT_URL + import.meta.env.VITE_KAKAO_REDIRECT_URI}&response_type=code`
	const NAVER_AUTH_URL = `https://nid.naver.com/oauth2.0/authorize?client_id=${import.meta.env.VITE_NAVER_CLIENT_ID}&response_type=code&redirect_uri=${import.meta.env.VITE_FRONT_URL + import.meta.env.VITE_NAVER_REDIRECT_URI}&state=1234`

	const onLogin = (userInfo) => {
		axios
			.post(import.meta.env.VITE_BASE_URL + "/user/login", userInfo, {
				headers: { "Content-Type": "application/json" },
				withCredentials: true,
			})
			.then((res) => {
				//console.log(res)
				if (res.status === 200) {
					// //console.log("여기는 로그인이 완료되어서 토큰을 뱉습니다.")
					handleEnableNotifications()
					const notificationOn = localStorage.getItem("notificationPermission")

					if (notificationOn !== "true") {
						// //console.log("권한을 로컬스토리지에 등록합니다.")
						localStorage.setItem("notificationPermission", "true")
					} else {
						//console.log("이미 등록된 권한입니다.")
					}

					if (res.data.lastProfile === null) {
						setUser({
							email: res.data.email,
							accessToken: "Token",
							lastProfile: res.data.lastProfile,
							profileList: [],
							rememberMe: userInfo.rememberMe,
						})

						Swal.fire({
							title: "프로필이 없습니다.",
							text: "프로필을 생성해주세요.",
							icon: "info",
							showCancelButton: true,
							confirmButtonText: "프로필 생성",
							cancelButtonText: "취소",
						}).then((result) => {
							if (result.isConfirmed) {
								navigate("/profiles/create", { replace: true })
							}
						})
					} else {
						setUser({
							email: res.data.email,
							accessToken: "Token",
							lastProfile: res.data.lastProfile,
							profileList: res.data.profileList,
							rememberMe: userInfo.rememberMe,
						})
						navigate("/", { replace: true })
					}
				}
			})
			.catch((err) => {
				//console.log(err)
				switch (err.status) {
					case 409:
						alert("이메일 또는 비밀번호를 확인하세요.")
						break
					default:
						alert("알 수 없는 문제가 발생했습니다.")
				}
			})
	}

	const onKakao = (e) => {
		e.preventDefault()

		const width = 500
		const height = 600
		const left = window.screenX + (window.innerWidth - width) / 2
		const top = window.screenY + (window.innerHeight - height) / 2

		window.open(
			KAKAO_AUTH_URL,
			"KakaoLogin",
			`width=${width},height=${height},top=${top},left=${left}`,
		)
	}

	const onNaver = (e) => {
		e.preventDefault()

		const width = 500
		const height = 600
		const left = window.screenX + (window.innerWidth - width) / 2
		const top = window.screenY + (window.innerHeight - height) / 2

		window.open(
			NAVER_AUTH_URL,
			"NaverLogin",
			`width=${width},height=${height},top=${top},left=${left}`,
		)
	}

	useEffect(() => {
		// //console.log(window.location.pathname)
		window.addEventListener("message", (event) => {
			// 보안 체크: 메시지를 보낸 도메인을 확인합니다.
			if (event.origin !== import.meta.env.VITE_FRONT_URL) return
			const { code, state } = event.data
			// //console.log(code)
			// //console.log(state)

			if (code && !state) {
				// 이제 받은 code를 사용해 백엔드와 통신하거나 추가 처리를 합니다.
				axios
					.get(import.meta.env.VITE_BASE_URL + `/login/oauth2/code/kakao?code=${code}`, {
						withCredentials: true,
					})
					.then((res) => {
						if (res.status === 200) {
							if (res.data.message === "terms_required") {
								navigate("/user/socialterms", { state: res.data })
							}

							handleEnableNotifications()
							const notificationOn = localStorage.getItem("notificationPermission")

							if (notificationOn !== "true") {
								// //console.log("권한을 로컬스토리지에 등록합니다.")
								localStorage.setItem("notificationPermission", "true")
							} else {
								//console.log("이미 등록된 권한입니다.")
							}

							if (res.data.lastProfile === null) {
								setUser({
									email: res.data.email,
									accessToken: "Token",
									lastProfile: res.data.lastProfile,
									profileList: [],
								})
							} else {
								axios
									.get(import.meta.env.VITE_BASE_URL + "/profiles/list", {
										headers: { "Content-Type": "application/json" },
										withCredentials: true,
									})
									.then((response) => {
										//console.log(response)
										setUser({
											email: res.data.email,
											accessToken: "Token",
											lastProfile: res.data.lastProfile,
											profileList: response.data.profiles,
										})
										Swal.fire({
											title: "Loading",
											allowOutsideClick: false,
											didOpen: () => {
												Swal.showLoading()
											},
											timer: 3000,
										}).then(() => {
											navigate("/", { replace: true })
										})
									})
									.catch((err) => {
										//console.log(err)
									})
							}
						}
					})
					.catch((err) => {
						switch (err.status) {
							case 409:
								Swal.fire({
									icon: "error",
									iconColor: "#ee0000",
									title: "이메일 또는 비밀번호를 확인하세요",
									timer: 3000,
									showConfirmButton: false,
								})
								break
							case 403:
								break
							default:
								//console.log(err)
								Swal.fire({
									icon: "error",
									iconColor: "#ee0000",
									title: "알수없는 오류",
									timer: 3000,
									showConfirmButton: false,
								})
								break
						}
					})
			} else if (code && state) {
				axios
					.get(
						import.meta.env.VITE_BASE_URL +
							`/login/oauth2/code/naver?code=${code}&state=${state}`,
						{
							withCredentials: true,
						},
					)
					.then((res) => {
						if (res.status === 200) {
							if (res.data.message === "terms_required") {
								navigate("/user/socialterms", { state: res.data })
							}

							handleEnableNotifications()
							const notificationOn = localStorage.getItem("notificationPermission")

							if (notificationOn !== "true") {
								// //console.log("권한을 로컬스토리지에 등록합니다.")
								localStorage.setItem("notificationPermission", "true")
							} else {
								//console.log("이미 등록된 권한입니다.")
							}

							if (res.data.lastProfile === null) {
								setUser({
									email: res.data.email,
									accessToken: "Token",
									lastProfile: res.data.lastProfile,
									profileList: [],
								})
							} else {
								axios
									.get(import.meta.env.VITE_BASE_URL + "/profiles/list", {
										headers: { "Content-Type": "application/json" },
										withCredentials: true,
									})
									.then((response) => {
										//console.log(response)
										setUser({
											email: res.data.email,
											accessToken: "Token",
											lastProfile: res.data.lastProfile,
											profileList: response.data.profiles,
										})
										Swal.fire({
											title: "Loading",
											allowOutsideClick: false,
											didOpen: () => {
												Swal.showLoading()
											},
											timer: 3000,
										}).then(() => {
											navigate("/", { replace: true })
										})
									})
									.catch((err) => {
										//console.log(err)
									})
							}
						}
					})
					.catch((err) => {
						switch (err.status) {
							case 409:
								Swal.fire({
									icon: "error",
									iconColor: "#ee0000",
									title: "이메일 또는 비밀번호를 확인하세요",
									timer: 3000,
									showConfirmButton: false,
								})
								break
							case 403:
								break
							default:
								//console.log(err)
								Swal.fire({
									icon: "error",
									iconColor: "#ee0000",
									title: "알수없는 오류",
									timer: 3000,
									showConfirmButton: false,
								})
								break
						}
					})
			}
		})
	}, [navigate, setUser, navigationType])

	return (
		<div className="flex flex-col items-center">
			<p
				className="my-3 text-center"
				style={{
					fontFamily: "'Grate vibes', serif",
					backgroundImage: "linear-gradient(45deg, #6a1b9a, #ff4081)",
					WebkitBackgroundClip: "text",
					backgroundClip: "text",
					color: "transparent",
					fontWeight: "bold",
					fontSize: "3rem", // 텍스트 크기
					animation:
						"fadeInText 2s ease-out, glowEffect 1.5s ease-in-out infinite alternate",
				}}
			>
				5 Identity
			</p>
			<EmailLogin onLogin={onLogin} />
			<div className="w-[80%] grid grid-cols-2 gap-2 space-y-4 mb-5">
				<button
					onClick={onKakao}
					className="col-span-2 px-4 py-2 flex gap-2 bg-[#FEE500] rounded-lg text-[rgba(0,0,0,.85)] dark:text-[rgba(0,0,0,.85)] hover:border-slate-400 dark:hover:border-slate-500 hover:text-slate-900 dark:hover:text-slate-300 hover:shadow transition duration-150"
				>
					<img
						className="w-8 h-8"
						src="https://www.svgrepo.com/show/368252/kakao.svg"
						loading="lazy"
						alt="kakao logo"
					/>
					<span className="m-auto">카카오 로그인</span>
				</button>
				<button
					onClick={onNaver}
					className="col-span-2 px-4 py-2 flex gap-2 bg-[#03C75A] rounded-lg text-[rgba(0,0,0,.85)] dark:text-[rgba(0,0,0,.85)] hover:border-slate-400 dark:hover:border-slate-500 hover:text-slate-900 dark:hover:text-slate-300 hover:shadow transition duration-150"
				>
					<img
						className="w-8 h-8"
						src="/btnG_아이콘사각.png"
						loading="lazy"
						alt="naver logo"
					/>
					<span className="m-auto">네이버 로그인</span>
				</button>
			</div>
			<button
				style={{
					width: "80%",
					padding: "0.5rem", // 패딩
					// border: "1px solid #f87171", // 테두리
					borderRadius: "0.375rem", // 둥근 테두리
					backgroundColor: "#fef2f2", // 배경색
					fontWeight: "700", // 글씨 두껍게
					fontSize: "1.25rem", // 글씨 크기
					color: "#e11d48", // 글씨 색
					transition: "all 0.3s ease", // 부드러운 전환 효과
				}}
				onClick={() => {
					navigate("/user/signup")
				}}
				onMouseEnter={(e) => {
					e.target.style.backgroundColor = "#f87171" // 호버 시 배경색 변경
					e.target.style.color = "#fff" // 글씨색 변경
					e.target.style.transform = "scale(1.05)" // 버튼 크기 확대
				}}
				onMouseLeave={(e) => {
					e.target.style.backgroundColor = "#fef2f2" // 원래 배경색
					e.target.style.color = "#e11d48" // 원래 글씨색
					e.target.style.transform = "scale(1)" // 원래 크기
				}}
			>
				이메일로 가입하기
			</button>
		</div>
	)
}

export default Login
