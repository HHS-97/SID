import axios, { formToJSON } from "axios"
import { useState } from "react"
import Swal from "sweetalert2"
import ProfileImage from "../../../assets/ProfileImage"

const UpdateProfileForm = ({ profile, onUpdate, onCancle, onDelete, setUser }) => {
	const [nickname, setNickname] = useState(profile.nickname)
	const [isValidNickname, setIsValidNickname] = useState(null)
	const [profileImage, setProfileImage] = useState({
		imageFile: null,
		previewUrl: profile.profileImage,
	})

	const onImageUpload = (e) => {
		const uploadedFile = e.target.files[0]
		if (uploadedFile) {
			//console.log(uploadedFile)

			const reader = new FileReader()
			reader.onload = (e) => {
				setProfileImage({
					imageFile: uploadedFile,
					previewUrl: e.target.result,
				})
			}
			reader.readAsDataURL(uploadedFile)
		}
	}

	const onValidNickname = (e) => {
		e.preventDefault()
		if (profile.nickname === nickname || nickname === null || nickname === "") {
			setIsValidNickname(null)
			setNickname(profile.nickname)
		} else {
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
							setIsValidNickname(false)
							break
						default:
							Swal.fire({
								title: "중복 검사 실패",
								icon: "error",
								showConfirmButton: true,
							})
							break
					}
				})
		}
	}

	const onSubmit = async (e) => {
		e.preventDefault()

		// 닉네임이 변경되었지만 중복확인을 하지 않은 경우
		if (nickname !== profile.nickname && isValidNickname === null) {
			Swal.fire({
				icon: "warning",
				title: "닉네임 중복확인",
				text: "닉네임 중복확인이 필요합니다.",
				confirmButtonText: "확인",
			})
			return
		}

		// 닉네임이 유효하지 않은 경우
		if (isValidNickname === false) {
			Swal.fire({
				icon: "error",
				title: "닉네임 오류",
				text: "사용할 수 없는 닉네임입니다.",
				confirmButtonText: "확인",
			})
			return
		}

		const formData = new FormData(e.target)

		if (isValidNickname) {
			formData.set("nickname", nickname)
		} else {
			formData.delete("nickname")
		}

		if (profileImage.imageFile === null) {
			formData.delete("profileImage")
		}

		if (formData.get("description") === profile.description) {
			formData.delete("description")
		}

		Swal.fire({
			icon: "question",
			title: "프로필 수정",
			text: "수정된 내용을 저장하시겠습니까?",
			showCancelButton: true,
			confirmButtonText: "확인",
			cancelButtonText: "취소",
		}).then((res) => {
			if (!res.isConfirmed) {
				onCancle()
				return
			}
			axios
				.patch(import.meta.env.VITE_BASE_URL + "/profiles", formData, {
					headers: { "Content-Type": "multipart/form-data" },
					withCredentials: true,
				})
				.then(async (res) => {
					onUpdate(isValidNickname ? nickname : profile.nickname)
					//console.log("req : ", formToJSON(formData))
					//console.log("res : ", res)
					setUser((prev) => {
						return {
							...prev,
							lastProfile: res.data.lastProfile,
							profileList: res.data.profileList,
						}
					})
					Swal.fire({
						icon: "success",
						title: "프로필 수정",
						text: "프로필이 수정되었습니다.",
						confirmButtonText: "확인",
					})
					onCancle()
				})
				.catch((err) => {
					//console.log(err)
					switch (err.response.status) {
						case 401:
							alert("로그인 만료")
							localStorage.removeItem("user")
							window.location.href = "/user/login"
							break
						case 400:
							Swal.fire({
								icon: "error",
								title: "프로필 수정 오류",
								text: "프로필 수정에 실패했습니다.",
								confirmButtonText: "확인",
							})
							break
						default:
							Swal.fire({
								title: "프로필 수정 실패",
								icon: "error",
								showConfirmButton: true,
							})
							break
					}
				})
		})
	}

	return (
		<div className="flex flex-col items-center space-y-3">
			<form
				id="updateProfileForm"
				encType="multipart/form-data"
				className="space-y-5"
				onSubmit={onSubmit}
			>
				<div>
					<label
						htmlFor="profileImage"
						className="flex flex-col items-center cursor-pointer space-y-2"
					>
						<ProfileImage
							src={profileImage.previewUrl}
							except={{ src: profileImage.previewUrl }}
						/>
						<p className="text-lg">📷 사진 수정</p>
					</label>
					<input
						type="file"
						id="profileImage"
						name="profileImage"
						accept="image/*"
						className="hidden"
						onChange={onImageUpload}
					/>
				</div>
				<div className="grid grid-cols-10">
					<label htmlFor="nickname" className="col-start-2 col-span-2 font-bold text-xl my-auto">
						닉네임
					</label>
					<input
						type="text"
						name="nickname"
						id="nickname"
						placeholder={profile.nickname}
						className="col-span-4 border-1 rounded-md px-2 mx-2"
						onChange={(e) => {
							setNickname(e.target.value)
							setIsValidNickname(null)
						}}
					/>
					<button
						className="col-span-2 whitespace-nowrap sm:text-base font-bold bg-gradient-to-r from-rose-300 to-rose-400 text-white px-4 py-2 rounded-md hover:shadow-lg transition"
						onSubmit={(e) => e.preventDefault()}
						onClick={onValidNickname}
					>
						중복 확인
					</button>
					{(isValidNickname === null && nickname === profile.nickname) ||
					nickname === "" ? (
						<p className="col-start-2 col-span-8">💜 현재 닉네임</p>
					) : isValidNickname ? (
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
						className="w-[80%] border-1 rounded-md px-2 resize-none"
						defaultValue={profile.description}
					></textarea>
				</div>
				<div className="grid grid-cols-6 gap-2">
					<button className="col-start-2 col-span-2 py-2 rounded-md bg-rose-400 hover:bg-rose-500 text-white font-bold text-xl transition-all duration-300 ease-in-out transform hover:scale-105 active:scale-95">
						수정
					</button>
					<button
						className="col-span-2 p-2 border border-pink-400 hover:bg-red-200 rounded-md bg-red-100 font-bold text-xl text-rose-500 transition-all duration-300 ease-in-out  transform hover:scale-105 active:scale-95"
						onClick={(e) => {
							e.preventDefault()
							onCancle()
						}}
					>
						취소
					</button>
				</div>
			</form>
			<p className="text-sm underline cursor-pointer" onClick={onDelete}>
				프로필 삭제
			</p>
		</div>
	)
}

export default UpdateProfileForm
