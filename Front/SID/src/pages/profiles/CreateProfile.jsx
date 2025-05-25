import axios, { formToJSON } from "axios"
import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { useRecoilState } from "recoil"
import Swal from "sweetalert2"
import { userAtom } from "../../atoms/userAtom"
import CreateProfileForm from "../../components/profiles/createProfiles/CreateProfileForm"
import SelectInterestCategory from "../../components/profiles/createProfiles/SelectInterestCategory"

const CreateProfile = () => {
	const navigate = useNavigate()
	const [user, setUser] = useRecoilState(userAtom)
	const [isInputData, setIsInputData] = useState(false)
	const [formData, setFormData] = useState(null)

	const onCreateProfile = (form, isWantInterest) => {
		if (isWantInterest) {
			setIsInputData(true)
			setFormData(form)
		} else {
			form.append("interestCategoryId", [])
			form.forEach((value, key) => {
				//console.log(key, value)
			})
			//console.log(formToJSON(form))
			axios
				.post(import.meta.env.VITE_BASE_URL + "/profiles", form, {
					withCredentials: true,
				})
				.then((res) => {
					//console.log(res)
					setUser({
						...user,
						lastProfile: res.data.lastProfile,
						profileList: res.data.profiles,
					})
					Swal.fire({
						icon: "success",
						title: "성공",
						text: "프로필이 생성되었습니다.",
						timer: 1500,
					})
					navigate("/")
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
								title: "프로필 생성 실패",
								icon: "error",
								showConfirmButton: true,
							})
							break
					}
				})
		}
	}

	const onCompleteSelect = (categories) => {
		const form = formData
		form.append("interestCategoryId", categories)
		form.forEach((value, key) => {
			//console.log(key, value)
		})
		axios
			.post(import.meta.env.VITE_BASE_URL + "/profiles", form, {
				withCredentials: true,
			})
			.then((res) => {
				//console.log(res)
				setUser({
					...user,
					lastProfile: res.data.lastProfile,
					profileList: res.data.profiles,
				})
				Swal.fire({
					icon: "success",
					title: "성공",
					text: "프로필이 생성되었습니다.",
					timer: 1500,
				})
				navigate("/")
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
							title: "프로필 생성 실패",
							icon: "error",
							showConfirmButton: true,
						})
						break
				}
			})
	}

	return (
		<div>
			<p className="my-10 font-bold text-4xl">프로필 생성</p>
			{isInputData ? (
				<SelectInterestCategory onCompleteSelect={onCompleteSelect} />
			) : (
				<CreateProfileForm onCreateProfile={onCreateProfile} />
			)}
		</div>
	)
}

export default CreateProfile
