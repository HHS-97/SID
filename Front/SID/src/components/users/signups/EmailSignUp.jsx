import { formToJSON } from "axios"
import { useRef, useState } from "react"
import "./signups.css"

const EmailSignUp = ({ email, onSignUp }) => {
	const password = useRef(null)
	const formRef = useRef(null)

	const [hasLowercase, setHasLowercase] = useState(false)
	const [hasNumber, setHasNumber] = useState(false)
	const [isLongEnough, setIsLongEnough] = useState(false)
	const handleChangePassword = (e) => {
		const newPassword = e.target.value

		// 영어 소문자 포함 여부 확인
		setHasLowercase(/[a-z]/.test(newPassword))

		// 숫자 포함 여부 확인
		setHasNumber(/\d/.test(newPassword))

		// 길이 확인
		setIsLongEnough(newPassword.length >= 8)
	}

	const [isEqualPassword, setIsEqualPassword] = useState(false)
	const handleChangePasswordConfirm = (e) => {
		const confirmPassword = e.target.value

		// 비밀번호 일치 여부 확인
		setIsEqualPassword(password.current.value === confirmPassword)
	}

	const handleNumberInput = (e) => {
		const target = e.target
		if (target.value.length >= 5) {
			e.target.value = e.target.value.slice(0, 4)
		}
	}

	const handleSubmit = (e) => {
		e.preventDefault()
		if (hasLowercase && hasNumber && isLongEnough && isEqualPassword) {
			const form = formToJSON(new FormData(formRef.current))
			if (!form.name) {
				form.name = "이름없음"
			}
			if (form.phone2 && form.phone3) {
				form.phone = [form.phone1, form.phone2, form.phone3].join("-")
			} else {
				form.phone = ""
			}
			delete form.phone1
			delete form.phone2
			delete form.phone3
			//console.log(form)
			onSignUp(form)
		} else {
			alert("비밀번호를 올바르게 설정해주세요.")
		}
	}

	return (
		<form
			onSubmit={handleSubmit}
			ref={formRef}
			id="signUpForm"
			className="flex flex-col space-y-10"
		>
			<div className="grid grid-cols-5">
				<label htmlFor="userEmail" className="col-span-2 px-3" style={{ textAlign: "end" }}>
					이메일
				</label>
				<input
					type="text"
					name="email"
					id="userEmail"
					value={email}
					readOnly
					autoComplete="off"
					className="col-span-2 border-1 rounded-md px-2 bg-gray-200"
				/>
			</div>
			<div className="grid grid-cols-5 gap-y-2">
				<label
					htmlFor="userPassword"
					className="col-span-2 px-3"
					style={{ textAlign: "end" }}
				>
					비밀번호
				</label>
				<input
					ref={password}
					type="password"
					name="password"
					id="userPassword"
					className="col-span-2 border-1 rounded-md px-2"
					onChange={handleChangePassword}
				/>
				<div className="col-span-5">
					<span className="mx-2">
						<span>{hasLowercase ? "✅" : "❌"}</span> 영어 소문자 포함
					</span>
					<span className="mx-2">
						<span>{hasNumber ? "✅" : "❌"}</span> 숫자 포함
					</span>
					<span className="mx-2">
						<span>{isLongEnough ? "✅" : "❌"}</span> 8글자 이상
					</span>
				</div>
			</div>
			<div className="grid grid-cols-5 gap-y-2">
				<label
					htmlFor="userPasswordConfirm"
					className="col-span-2 px-3"
					style={{ textAlign: "end" }}
				>
					비밀번호 확인
				</label>
				<input
					type="password"
					name="passwordConfirm"
					id="userPasswordConfirm"
					className="col-span-2 border-1 rounded-md px-2"
					onChange={handleChangePasswordConfirm}
				/>
				<p className="col-span-5">
					<span>{isEqualPassword ? "✅" : "❌"}</span>비밀번호 일치
				</p>
			</div>
			<hr />
			<div className="grid grid-cols-6">
				<label htmlFor="userName" className="col-span-2 px-3" style={{ textAlign: "end" }}>
					이름
				</label>
				<input
					type="text"
					name="name"
					id="userName"
					autoComplete="off"
					style={{ textAlign: "center" }}
					className="col-span-3 border-1 rounded-md px-2"
				/>
			</div>
			<div className="grid grid-cols-6">
				<label
					htmlFor="userBirthDate"
					className="col-span-2 px-3"
					style={{ textAlign: "end" }}
				>
					생년월일
				</label>
				<input
					type="date"
					name="birthDate"
					id="userBirthDate"
					style={{ textAlign: "center" }}
					className="col-span-3 border-1 rounded-md px-2"
				/>
			</div>
			<div className="grid grid-cols-6">
				<label htmlFor="userPhone" className="col-span-2 px-3" style={{ textAlign: "end" }}>
					휴대폰
				</label>
				<div className="col-span-3 flex">
					<input
						type="number"
						name="phone1"
						id="userPhone1"
						value="010"
						readOnly
						style={{ textAlign: "center" }}
						className="appearance-none w-1/3 border-1 rounded-md px-2"
					/>
					<span className="mx-1">-</span>
					<input
						type="number"
						name="phone2"
						id="userPhone2"
						placeholder="1234"
						autoComplete="off"
						style={{ textAlign: "center" }}
						onInput={handleNumberInput}
						className="appearance-none w-1/3 border-1 rounded-md px-2"
					/>
					<span className="mx-1">-</span>
					<input
						type="number"
						name="phone3"
						id="userPhone3"
						placeholder="5678"
						autoComplete="off"
						style={{ textAlign: "center" }}
						onInput={handleNumberInput}
						className="appearance-none w-1/3 border-1 rounded-md px-2"
					/>
				</div>
			</div>
			<div className="grid grid-cols-6">
				<label
					htmlFor="userGender"
					className="col-span-2 px-3"
					style={{ textAlign: "end" }}
				>
					성별
				</label>
				<select
					name="gender"
					id="userGender"
					className="col-span-3 border-1 rounded-md px-2"
				>
					<option value="Other">알수없음</option>
					<option value="Male">남성</option>
					<option value="Female">여성</option>
				</select>
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
				시작하기
			</button>
		</form>
	)
}

export default EmailSignUp
