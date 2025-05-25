import axios from "axios"
import { useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { useRecoilState, useRecoilValue } from "recoil"
import Swal from "sweetalert2"
import { isLoginSelector, userAtom } from "../atoms/userAtom"
import PostList from "../components/feeds/PostList"

const Home = () => {
	// Atom 사용법
	const [user, setUser] = useRecoilState(userAtom) // useState와 같은 방식으로 사용
	const isLogin = useRecoilValue(isLoginSelector) // vue.js의 computed같은 읽기 전용 상태
	const navigate = useNavigate()

	const getProfiles = () => {
		axios
			.get(import.meta.env.VITE_BASE_URL + "/profiles/list", {
				headers: { "Content-Type": "application/json" },
				withCredentials: true,
			})
			.then((res) => {
				//console.log(res)
				setUser({
					...user,
					lastProfile: res.data.lastProfile,
					profileList: res.data.profiles,
				})
			})
			.catch((err) => {
				//console.log(err)
			})
	}

	useEffect(() => {
		if (isLogin && user.lastProfile) {
			getProfiles()
		} else if (isLogin) {
			Swal.fire({
				title: "Loading",
				allowOutsideClick: false,
				didOpen: () => {
					Swal.showLoading()
				},
				timer: 3000,
			}).then(() => {
				Swal.fire({
					icon: "info",
					text: "프로필 생성 페이지로 이동합니다.",
					timer: 1000,
					showConfirmButton: false,
				}).then(() => {
					navigate("/profile/createprofile")
				})
			})
		}
		//console.log("유저 정보 : ", user)
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [isLogin])

	return (
		<div className="h-full flex flex-col">
			{isLogin && user.lastProfile ? (
				<PostList url="/posts/curating" key="/posts/curating" />
			) : (
				<PostList url="/posts/briefly" key="/posts/briefly/home" params={{ type: "2" }} />
			)}
		</div>
	)
}

export default Home
