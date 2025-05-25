import axios from "axios"
import { useRef, useState } from "react"
import Swal from "sweetalert2"
import Terms from "../logins/Terms"

const EmailValid = ({ onEmailValid }) => {
	const [isEmailSent, setIsEmailSent] = useState(false)
	const email = useRef("")
	// const validNumber = useRef("")

	const onEmailSent = (email) => {
		// 이메일 보내기
		Swal.fire({
			title: "Loading",
			allowOutsideClick: false,
			didOpen: () => {
				Swal.showLoading()
			},
		})
		axios
			.get(import.meta.env.VITE_BASE_URL + "/user/emailvalid", {
				params: { email },
				withCredentials: true,
			})
			.then((res) => {
				//console.log(res.data)
				if (res.data.isValid) {
					Swal.close()
					Swal.fire({
						icon: "success",
						title: "이메일 확인 완료",
						text: "가입 가능한 이메일입니다.",
						showConfirmButton: false,
						timer: 1500,
					})
					setIsEmailSent(true)
					onEmailValid(email, "1234") // 인증 번호 임의 처리
				}
			})
			.catch((err) => {
				//console.log(err)
				Swal.close()
				switch (err.status) {
					case 409:
						Swal.fire({
							icon: "error",
							title: "중복된 이메일",
							text: "이미 가입된 이메일입니다. 다른 이메일을 작성해주세요.",
						})
						break
					default:
						Swal.fire({
							icon: "error",
							title: "오류",
							text: "알 수 없는 오류가 발생했습니다. 다시 시도해주세요.",
						})
						break
				}
			})
	}

	const handleSubmit = (e) => {
		e.preventDefault()
		if (!isEmailSent) {
			if (!allowPersonalTerms || !allowServiceTerms) {
				alert("약관에 모두 동의해주세요.")
				return
			}
			if (!email.current.value) {
				alert("이메일을 입력해주세요.")
				return
			}
			onEmailSent(email.current.value)
		}
	}

	const [allowPersonalTerms, setAllowPersonalTerms] = useState(false)
	const [allowServiceTerms, setAllowServiceTerms] = useState(false)

	return (
		<div>
			<form onSubmit={handleSubmit}>
				<Terms
					setAllowPersonalTerms={setAllowPersonalTerms}
					setAllowServiceTerms={setAllowServiceTerms}
				/>
				<div className="flex">
					<label htmlFor="userEmail" className="font-bold text-lg px-3 my-auto">
						Email
					</label>
					<input
						className="flex-1 outline-1 rounded-md p-1"
						type="email"
						name="email"
						id="userEmail"
						ref={email}
						autoComplete="off"
					/>
				</div>
				<button
					className="w-[100%] py-2 border-1 rounded-md my-4 bg-red-300 font-bold text-2xl"
					style={{
						padding: "0.5rem", // 패딩
						borderRadius: "0.375rem", // 둥근 테두리
						backgroundColor: "#fef2f2", // 배경색
						fontWeight: "700", // 글씨 두껍게
						fontSize: "1.25rem", // 글씨 크기
						color: "#e11d48", // 글씨 색
						transition: "all 0.3s ease", // 부드러운 전환 효과
					}}
				>
					이메일 중복 확인
				</button>
			</form>
		</div>
	)
}

export default EmailValid
