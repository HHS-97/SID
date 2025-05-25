import axios from "axios"
import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { useRecoilState } from "recoil"
import Swal from "sweetalert2"
import { userAtom } from "../../atoms/userAtom"
import EmailSignUp from "../../components/users/signups/EmailSignUp"
import EmailValid from "../../components/users/signups/EmailValid"
import { handleEnableNotifications } from "../../firebase/firebasemessage"

const SignUp = () => {
	const navigate = useNavigate()

	const [isEmailValid, setIsEmailValid] = useState(false) // 이메일 인증이 되었나?
	const [validEmail, setValidEmail] = useState("") // 인증된 이메일

	const [user, setUser] = useRecoilState(userAtom)

	const onEmailValid = (userEmail, validNumber) => {
		// 이메일 인증이 되었을 경우 다음 페이지로 넘어가는 로직
		if (!validNumber) {
			alert("숫자를 입력해주세요.")
			return
		}
		if (/^[0-9]{1,}$/.test(validNumber)) {
			// 디버깅용
			// //console.log("확인 " + validNumber)
			setValidEmail(userEmail)
			setIsEmailValid(true)
		} else {
			alert("숫자만 입력하세요.")
		}
	}

	const onSignUp = (userInfo) => {
		// 디버깅용
		//console.log("request : ")
		//console.log(userInfo)

		axios
			.post(import.meta.env.VITE_BASE_URL + "/user/signup", userInfo)
			.then((res) => {
				//console.log("response : ")
				//console.log(res)
				if (res.status === 201) {
					//console.log(user.email) // 로그인된 계정 출력
					Swal.fire({
						icon: "success",
						title: "회원가입 성공",
						text: "바로 로그인하시겠습니까?",
					}).then((result) => {
						if (result.isConfirmed) {
							axios
								.post(
									import.meta.env.VITE_BASE_URL + "/user/login",
									{
										email: userInfo.email,
										password: userInfo.password,
										rememberMe: false,
									},
									{
										headers: { "Content-Type": "application/json" },
										withCredentials: true,
									},
								)
								.then((res) => {
									//console.log(res)
									if (res.status === 200) {
										// //console.log("여기는 로그인이 완료되어서 토큰을 뱉습니다.")
										handleEnableNotifications()
										const notificationOn =
											localStorage.getItem("notificationPermission")

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
						} else {
							navigate("/user/login")
						}
					})
				}
			})
			.catch((err) => {
				//console.log(err)
				switch (err.status) {
					case 409:
						alert("이미 가입된 이메일입니다.")
						break
					default:
						alert("알 수 없는 문제가 발생했습니다.")
				}
			})
	}

	return (
		<>
			<p className="my-10 font-bold text-4xl">회원가입</p>
			{isEmailValid ? (
				<EmailSignUp email={validEmail} onSignUp={onSignUp} />
			) : (
				<EmailValid onEmailValid={onEmailValid} />
			)}
		</>
	)
}

export default SignUp
