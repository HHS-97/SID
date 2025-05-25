import { useEffect, useState } from "react"
import { useNavigate, useParams } from "react-router-dom"
// import Post from "../../components/posts/Post"
import axios from "axios"
import { useRecoilState } from "recoil"
import Swal from "sweetalert2"
import { userAtom } from "../../atoms/userAtom"
import PostList from "../../components/feeds/PostList"
import ProfileInfo from "../../components/profiles/userProfiles/ProfileInfo"

const UserProfile = () => {
	// 들어올 때 링크에 따라 프로필 설정
	const [profile, setProfile] = useState({})
	const params = useParams()
	const [user, setUser] = useRecoilState(userAtom)
	const navigate = useNavigate()

	const getProfile = (nickname) => {
		axios
			.get(import.meta.env.VITE_BASE_URL + "/profiles", {
				withCredentials: true,
				params: { nickname },
			})
			.then((res) => {
				//console.log(res)
				setProfile(res.data)
			})
			.catch((err) => {
				//console.log(err)
				switch (err.status) {
					case 401:
						alert("로그인 만료")
						localStorage.removeItem("user")
						window.location.href = "/user/login"
						break
					case 404:
						Swal.fire({
							icon: "error",
							title: "ERROR",
							text: "해당 프로필을 찾을 수 없습니다.",
							timer: 2000,
						}).then(() => {
							navigate("/")
						})
						break
					default:
						Swal.fire({
							title: "프로필 로딩 실패",
							icon: "error",
							showConfirmButton: true,
						})
						break
				}
			})
	}

	const handleFollow = (status) => {
		switch (status) {
			case "NO": // 현재 팔로우 하지 않은 상태, 팔로우 요청
				axios
					.post(
						import.meta.env.VITE_BASE_URL + "/follows",
						{ followNickname: profile.nickname, isFollowed: false },
						{
							headers: { "Content-Type": "application/json" },
							withCredentials: true,
						},
					)
					.then((res) => {
						//console.log(res)
						setProfile({
							...profile,
							isFollowed: "YES",
							followerCount: profile.followerCount + 1,
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
							default:
								Swal.fire({
									title: "팔로우 실패",
									icon: "error",
									showConfirmButton: true,
								})
								break
						}
					})
				break
			case "YES": // 현재 팔로우 중인 상태, 팔로우 취소 요청
				axios
					.delete(import.meta.env.VITE_BASE_URL + "/follows", {
						headers: { "Content-Type": "application/json" },
						withCredentials: true,
						data: { followNickname: profile.nickname, isFollowed: true },
					})
					.then((res) => {
						//console.log(res)
						setProfile({
							...profile,
							isFollowed: "NO",
							followerCount: profile.followerCount - 1,
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
							default:
								Swal.fire({
									title: "팔로우 취소 실패",
									icon: "error",
									showConfirmButton: true,
								})
								break
						}
					})
				break
			default:
				//console.log("ERROR")
				break
		}
	}

	const updateUserProfile = (nickname) => {
		//console.log("넘어온 닉네임", nickname)
		if (nickname === profile.nickname) {
			getProfile(nickname)
		} else {
			navigate("/profile/" + nickname)
			getProfile(nickname)
		}
	}

	const deleteUserProfile = () => {
		Swal.fire({
			title: "프로필 삭제",
			html: "정말 삭제하시겠습니까? <br /> 삭제 후에도 게시글과 댓글은 삭제되지 않습니다.",
			icon: "warning",
			iconColor: "#990000",
			confirmButtonColor: "#990000",
			confirmButtonText: "삭제하기",
			showCancelButton: true,
		}).then((res) => {
			if (res.isConfirmed) {
				axios
					.delete(import.meta.env.VITE_BASE_URL + "/profiles", {
						headers: { "Content-Type": "application/json" },
						data: { nickname: profile.nickname },
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
							title: "프로필 삭제 성공",
							icon: "success",
							confirmButtonText: "확인",
							timer: 2000,
						}).then(() => {
							navigate("/", { replace: true })
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
							default:
								Swal.fire({
									title: "프로필 삭제 실패",
									icon: "error",
									showConfirmButton: true,
								})
								break
						}
					})
			}
		})
	}

	const followUserProfile = () => {
		switch (profile.isFollowed) {
			case "ME":
				Swal.fire({
					icon: "error",
					title: "ERROR",
					text: "자신을 팔로우할 수 없습니다.",
					confirmButtonText: "확인",
				})
				break
			case "YES":
			case "NO":
				handleFollow(profile.isFollowed)
				break
			default:
				//console.log("ERROR")
				break
		}
	}

	useEffect(() => {
		getProfile(params.nickname)
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [params.nickname])

	return (
		<div>
			<ProfileInfo
				profile={profile}
				onUpdate={updateUserProfile}
				onFollow={followUserProfile}
				onDelete={deleteUserProfile}
				navigate={navigate}
			/>
			<hr className="my-3 border-none" />
			<PostList
				url="/profiles/posts"
				key={`/profiles/posts/${profile.nickname}`}
				params={{ nickname: profile.nickname }}
				search={false}
				create={profile.nickname === user.lastProfile.nickname ? true : false}
			/>
		</div>
	)
}

export default UserProfile
