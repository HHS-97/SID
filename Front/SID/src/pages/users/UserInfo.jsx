import axios, { formToJSON } from "axios"
import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { useRecoilValue } from "recoil"
import Swal from "sweetalert2"
import withReactContent from "sweetalert2-react-content"
import { isLoginSelector } from "../../atoms/userAtom"
import ReadInfo from "../../components/users/userinfos/ReadInfo"
import UpdateInfo from "../../components/users/userinfos/UpdateInfo"

const UserInfo = () => {
	const isLogin = useRecoilValue(isLoginSelector)
	const navigate = useNavigate()
	const mySwal = withReactContent(Swal)
	const [userInformation, setUserInformation] = useState({})

	const onEdit = () => {
		mySwal
			.fire({
				title: "정보 수정",
				html: (
					<UpdateInfo
						info={userInformation}
						onUpdate={onUpdate}
						onCancle={() => {
							Swal.close()
						}}
					/>
				),
				showConfirmButton: false,
			})
			.then((result) => {
				if (result.isConfirmed) {
					onUpdate()
				}
			})
	}

	const getUserInfo = () => {
		axios
			.get(import.meta.env.VITE_BASE_URL + "/user/detail", {
				headers: { "Content-Type": "application/json" },
				withCredentials: true,
			})
			.then((res) => {
				setUserInformation(res.data)
				switch (res.data.gender) {
					case "O":
						setUserInformation({ ...res.data, gender: "Other" })
						break
					case "M":
						setUserInformation({ ...res.data, gender: "Male" })
						break
					case "F":
						setUserInformation({ ...res.data, gender: "Female" })
						break
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
					default:
						Swal.fire({
							title: "계정 조회 실패",
							icon: "error",
							showConfirmButton: true,
						})
						break
				}
			})
	}

	const onUpdate = (newInfo) => {
		const jsonInfo = formToJSON(newInfo)
		delete jsonInfo.email
		const phone =
			jsonInfo.phone2 && jsonInfo.phone3
				? jsonInfo.phone1 + "-" + jsonInfo.phone2 + "-" + jsonInfo.phone3
				: ""
		jsonInfo.phone = phone
		delete jsonInfo.phone1
		delete jsonInfo.phone2
		delete jsonInfo.phone3

		//console.log(jsonInfo)

		axios
			.patch(import.meta.env.VITE_BASE_URL + "/user/detail", jsonInfo, {
				headers: { "Content-Type": "application/json" },
				withCredentials: true,
			})
			.then((res) => {
				//console.log(res)
				getUserInfo()
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
							title: "정보 수정 실패",
							icon: "error",
							showConfirmButton: true,
						})
						break
				}
			})

		setUserInformation(formToJSON(newInfo))
	}

	const onDelete = () => {
		Swal.fire({
			title: "정말로 탈퇴하시겠습니까?",
			text: "계정을 삭제하면 복구할 수 없습니다.",
			icon: "warning",
			showCancelButton: true,
			confirmButtonColor: "#d33",
			cancelButtonColor: "#3085d6",
			confirmButtonText: "예, 탈퇴합니다.",
			cancelButtonText: "아니요",
		}).then((result) => {
			if (result.isConfirmed) {
				Swal.fire({
					icon: "info",
					text: "준비 중인 기능입니다.",
					showConfirmButton: false,
					timer: 1000,
				})
				// axios
				// 	.delete(import.meta.env.VITE_BASE_URL + "/user", {
				// 		headers: { "Content-Type": "application/json" },
				// 		withCredentials: true,
				// 	})
				// 	.then((res) => {
				// 		//console.log(res)
				// 		navigate("/")
				// 	})
				// 	.catch((err) => {
				// 		//console.log(err)
				// 	})
			}
		})
	}

	const onUpdatePassword = (form) => {
		//console.log(form)
		axios
			.patch(import.meta.env.VITE_BASE_URL + "/user/detail/password", form, {
				headers: { "Content-Type": "application/json" },
				withCredentials: true,
			})
			.then((res) => {
				//console.log(res)
				Swal.fire({
					title: "비밀번호 변경 성공",
					icon: "success",
					showConfirmButton: true,
				})
			})
			.catch((err) => {
				//console.log(err)
				switch (err.status) {
					case 401:
						alert("로그인 만료")
						localStorage.removeItem("user")
						window.location.href = "/user/login"
						break
					case 400:
						Swal.fire({
							title: "비밀번호 변경 실패",
							icon: "error",
							text: err.response.data.error,
							showConfirmButton: true,
						})
						break
					default:
						Swal.fire({
							title: "비밀번호 변경 실패",
							icon: "error",
							showConfirmButton: true,
						})
						break
				}
			})
	}

	useEffect(() => {
		getUserInfo()
	}, [isLogin, navigate])

	return (
		<>
			<ReadInfo
				onEdit={onEdit}
				onDelete={onDelete}
				info={userInformation}
				onUpdatePassword={onUpdatePassword}
			/>
		</>
	)
}

export default UserInfo
