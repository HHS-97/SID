import { formToJSON } from "axios"
import { useRef, useState } from "react"
import "./logins.css"

const EmailLogin = ({ onLogin }) => {
	const formRef = useRef(null)
	const [emailFocus, setEmailFocus] = useState(false)
	const [passwordFocus, setPasswordFocus] = useState(false)
	const [rememberMe, setRememberMe] = useState(false)

	const handleSubmit = (e) => {
		e.preventDefault()
		const form = formToJSON(new FormData(formRef.current))
		form.rememberMe = rememberMe
		if (form.email && form.password) {
			onLogin(form)
		} else {
			alert("이메일을 입력해주세요.")
		}
	}

	return (
		<form
			onSubmit={handleSubmit}
			ref={formRef}
			id="loginForm"
			className="flex flex-col items-center space-y-5 my-5 w-full max-w-md mx-auto"
			style={{
				backgroundClip: "text",
				fontWeight: "bold",
			}}
		>
			{/* 이메일 입력 필드 */}
			<div className="flex">
				<label htmlFor="userEmail" className="text-lg text-gray-700 w-20 my-auto">
					이메일
				</label>
				<input
					type="email"
					name="email"
					id="userEmail"
					autoComplete="off"
					className={`border-2 rounded-md px-3 py-2 text-sm transition-all duration-300 ease-in-out ${
						emailFocus ? "border-blue-500 shadow-md" : "border-gray-300"
					}`}
					onFocus={() => setEmailFocus(true)}
					onBlur={() => setEmailFocus(false)}
				/>
			</div>

			{/* 비밀번호 입력 필드 */}
			<div className="flex">
				<label htmlFor="userPassword" className="text-lg text-gray-700 w-20 my-auto">
					비밀번호
				</label>
				<input
					type="password"
					name="password"
					id="userPassword"
					className={`border-2 rounded-md px-3 py-2 text-sm transition-all duration-300 ease-in-out ${
						passwordFocus ? "border-blue-500 shadow-md" : "border-gray-300"
					}`}
					onFocus={() => setPasswordFocus(true)}
					onBlur={() => setPasswordFocus(false)}
				/>
			</div>

			{/* 로그인 유지 체크박스 */}
			<div className="flex items-center space-x-2">
				<input
					type="checkbox"
					name="rememberMe"
					id="userRememberToken"
					className="rounded-md"
					style={{
						width: "20px",
						height: "20px",
						borderRadius: "5px",
						border: "2px solid #4A90E2",
						transition: "all 0.3s ease",
						outline: "none",
					}}
					onChange={(e) => setRememberMe(e.target.checked)}
				/>
				<label
					htmlFor="userRememberToken"
					className="text-sm text-gray-600"
					style={{
						fontSize: "14px",
						color: "#333",
						cursor: "pointer",
						transition: "color 0.3s ease",
					}}
				>
					로그인 유지
				</label>
			</div>

			{/* 아이디/비밀번호 찾기 링크 */}
			<p
				onClick={(e) => {
					e.preventDefault()
					alert("준비중...")
				}}
				className="text-blue-500 text-sm hover:underline cursor-pointer"
			>
				아이디/비밀번호 찾기
			</p>

			{/* 로그인 버튼 */}
			<button
				type="submit"
				className="w-[80%] py-2 rounded-md bg-pink-400 hover:bg-pink-600 text-white font-bold text-xl transition-all duration-300 ease-in-out transform hover:scale-105 active:scale-95"
			>
				로그인
			</button>
		</form>
	)
}

export default EmailLogin
