import axios from "axios"
import { useRef, useState } from "react"
import Swal from "sweetalert2"
import ProfileImage from "../../../assets/ProfileImage"

const CreateProfileForm = ({ onCreateProfile }) => {
	const [nickname, setNickname] = useState(null)
	const [isValidNickname, setIsValidNickname] = useState(null)
	const [newProfileImage, setNewProfileImage] = useState({ imageFile: null, previewUrl: null })
	const formRef = useRef(null)

	const onImageUpload = (e) => {
		const uploadedFile = e.target.files[0]
		if (uploadedFile) {
			//console.log(uploadedFile)
			//console.log(e.target.result)

			const reader = new FileReader()
			reader.onload = (e) => {
				setNewProfileImage({
					imageFile: uploadedFile,
					previewUrl: e.target.result,
				})
			}
			reader.readAsDataURL(uploadedFile)
		}
	}

	const onValidNickname = (e) => {
		e.preventDefault()
		axios
			.get(import.meta.env.VITE_BASE_URL + "/profiles/nicknamevalid", {
				headers: { "Content-Type": "application/json" },
				withCredentials: true,
				params: { nickname },
			})
			.then((res) => {
				//console.log(res)
				if (res.data.message === "ok") {
					setIsValidNickname(true)
				} else {
					setIsValidNickname(false)
				}
			})
			.catch((err) => {
				//console.log(err)
				switch (err.status) {
					case 401:
						alert("로그인 만료")
						localStorage.removeItem("user")
						window.location.href = "/user/login"
						break
					case 409:
						Swal.fire({
							icon: "error",
							title: "중복된 닉네임입니다.",
							timer: 1500,
						})
						setIsValidNickname(false)
						break
					default:
						Swal.fire({
							icon: "error",
							title: "알 수 없는 오류가 발생했습니다.",
							timer: 1500,
						})
						break
				}
			})
	}

	return (
		<div className="flex flex-col items-center space-y-3">
			<form
				id="createProfileForm"
				encType="multipart/form-data"
				ref={formRef}
				className="space-y-5"
			>
				<div>
					<label
						htmlFor="newProfileImage"
						className="flex flex-col items-center cursor-pointer space-y-2"
					>
						<ProfileImage except={{ src: newProfileImage.previewUrl }} />
						<p className="text-lg">📷 사진 등록</p>
					</label>
					<input
						type="file"
						id="newProfileImage"
						name="profileImage"
						accept="image/*"
						className="hidden"
						onChange={onImageUpload}
					/>
				</div>
				<div className="flex flex-col items-center">
					<div className="flex flex-wrap items-center gap-3 sm:gap-4 md:gap-5">
						<label
							htmlFor="nickname"
							className="font-bold text-xl sm:text-lg md:text-xl my-2 sm:my-0 w-full sm:w-auto"
						>
							닉네임
						</label>

						<input
							type="text"
							name="nickname"
							id="nickname"
							className="border rounded-md px-3 py-2 flex-1 min-w-[150px] h-10 sm:h-12 md:h-14 text-sm sm:text-base"
							onChange={(e) => {
								setNickname(e.target.value)
								setIsValidNickname(null)
							}}
						/>

						<button
							className="bg-gradient-to-r from-rose-300 to-rose-400 text-white px-3 py-2 rounded-md hover:shadow-lg transition h-10 sm:h-12 md:h-14 text-sm sm:text-base"
							onSubmit={(e) => e.preventDefault()}
							onClick={onValidNickname}
						>
							중복 확인
						</button>
					</div>

					{isValidNickname ? (
						<p className="col-start-2 col-span-8">💚 사용 가능</p>
					) : isValidNickname !== null ? (
						<p className="col-start-2 col-span-8">💔 사용 불가</p>
					) : (
						""
					)}
				</div>
				<div className="flex flex-col items-center">
					<label htmlFor="description" className="font-bold text-xl">
						프로필 소개
					</label>
					<textarea
						name="description"
						id="description"
						rows="10"
						className="w-full border-1 rounded-md px-2 resize-none"
					></textarea>
				</div>
				<button
					className="w-full py-2 rounded-md bg-rose-400 hover:bg-rose-500 text-white font-bold text-xl transition-all duration-300 ease-in-out transform hover:scale-105 active:scale-95"
					onClick={(e) => {
						e.preventDefault()
						onCreateProfile(new FormData(formRef.current), true)
					}}
					onSubmit={(e) => {
						e.preventDefault()
					}}
				>
					관심사 설정하기
				</button>
				<button
					className="w-full p-2 border border-pink-400 hover:bg-red-200 rounded-md bg-red-100 font-bold text-xl text-rose-500 transition-all duration-300 ease-in-out  transform hover:scale-105 active:scale-95"
					onClick={(e) => {
						e.preventDefault()
						onCreateProfile(new FormData(formRef.current), false)
					}}
					onSubmit={(e) => {
						e.preventDefault()
					}}
				>
					관심사 설정하지 않고 시작
				</button>
			</form>
		</div>
	)
}

export default CreateProfileForm
