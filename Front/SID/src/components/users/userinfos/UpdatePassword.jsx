import { formToJSON } from "axios"
import { useRef, useState } from "react"
import Swal from "sweetalert2"

const UpdatePassword = ({ onUpdatePassword }) => {
	const password = useRef(null)
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

	const handleUpdatePassword = (e) => {
		e.preventDefault()
		Swal.fire({
			title: "비밀번호 변경",
			text: "비밀번호를 변경하시겠습니까?",
			icon: "question",
			showCancelButton: true,
			confirmButtonText: "변경",
			confirmButtonColor: "#4caf50",
			cancelButtonText: "취소",
		}).then((result) => {
			if (result.isConfirmed) {
				const formData = formToJSON(new FormData(e.target))
				if (
					!formData.currentPassword ||
					!formData.newPassword1 ||
					!formData.newPassword2 ||
					!isEqualPassword
				) {
					Swal.fire({
						title: "비밀번호 변경 실패",
						text: "비밀번호를 확인해주세요.",
						icon: "error",
						confirmButtonColor: "#4caf50",
					})
					return
				}
				onUpdatePassword(formData)
			} else {
				Swal.close()
			}
		})
	}

	return (
		<div>
			<form id="updatePassword" className="space-y-10" onSubmit={handleUpdatePassword}>
				<div className="grid grid-cols-7">
					<span className="col-start-2 col-span-2 font-bold">현재 비밀번호</span>
					<input
						className="col-span-3 border-1 rounded-md px-2"
						type="password"
						name="currentPassword"
						placeholder="현재 비밀번호"
					/>
				</div>
				<div className="grid grid-cols-7 space-y-2">
					<span className="col-start-2 col-span-2 font-bold">새 비밀번호</span>
					<input
						className="col-span-3 border-1 rounded-md px-2"
						type="password"
						name="newPassword1"
						placeholder="새 비밀번호"
						ref={password}
						onChange={handleChangePassword}
					/>
					<div className="col-start-2 col-span-5 text-sm">
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
				<div className="grid grid-cols-7 space-y-2">
					<span className="col-start-2 col-span-2 font-bold">비밀번호 확인</span>
					<input
						className="col-span-3 border-1 rounded-md px-2"
						type="password"
						name="newPassword2"
						placeholder="새 비밀번호 확인"
						onChange={handleChangePasswordConfirm}
					/>
					<p className="col-start-2 col-span-5 text-sm">
						<span>{isEqualPassword ? "✅" : "❌"}</span>비밀번호 일치
					</p>
				</div>
				<div className="grid grid-cols-6 gap-2">
					<button className="col-start-2 col-span-2 py-2 border-1 rounded-md bg-[#4caf50] text-white">
						비밀번호 변경
					</button>
					<button
						className="col-span-2 py-2 border-1 rounded-md bg-gray-100"
						onClick={(e) => {
							e.preventDefault()
							Swal.close()
						}}
					>
						취소
					</button>
				</div>
			</form>
		</div>
	)
}

export default UpdatePassword
