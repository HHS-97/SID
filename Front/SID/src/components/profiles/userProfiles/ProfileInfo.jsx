import axios from "axios"
import { useRecoilState } from "recoil"
import Swal from "sweetalert2"
import withReactContent from "sweetalert2-react-content"
import ProfileImage from "../../../assets/ProfileImage"
import { userAtom } from "../../../atoms/userAtom"
import FollowList from "./FollowList"
import UpdateProfileForm from "./UpdateProfileForm"

const ProfileInfo = ({ profile, onUpdate, onFollow, onDelete, navigate }) => {
	const mySwal = withReactContent(Swal)
	const [user, setUser] = useRecoilState(userAtom)

	const onEdit = () => {
		if (user.lastProfile.nickname !== profile.nickname) {
			Swal.fire({
				title: "프로필 수정",
				html: "잘못된 접근입니다.",
				icon: "error",
				iconColor: "#990000",
				showConfirmButton: false,
				timer: 1500,
			}).then(() => {
				onUpdate(profile.nickname)
				Swal.close()
				return
			})
		} else {
			mySwal.fire({
				title: "프로필 수정",
				html: (
					<UpdateProfileForm
						profile={profile}
						onUpdate={onUpdate}
						onCancle={mySwal.close}
						onDelete={onDelete}
						setUser={setUser}
					/>
				),
				showCancelButton: false,
				showConfirmButton: false,
			})
		}
	}

	const activeButton = () => {
		switch (profile.isFollowed) {
			case "ME":
				return (
					<button
						className="w-full py-2 rounded-md font-bold bg-gradient-to-r from-rose-300 to-rose-400 text-white px-4 py-2 rounded-md hover:shadow-lg transition text-xl hover:scale-105 active:scale-95"
						onClick={onEdit}
					>
						프로필 편집
					</button>
				)
			case "NO":
				return (
					<button
						className="w-full py-2 rounded-md bg-rose-400 hover:bg-rose-500 text-white font-bold text-xl transition-all duration-300 ease-in-out transform hover:scale-105 active:scale-95"
						onClick={onFollow}
					>
						팔로우
					</button>
				)
			case "YES":
				return (
					<button
						className="w-full p-2 border border-pink-400 hover:bg-red-200 rounded-md bg-red-100 font-bold text-xl text-rose-500 transition-all duration-300 ease-in-out  transform hover:scale-105 active:scale-95"
						onClick={onFollow}
					>
						언팔로우
					</button>
				)
		}
	}

	const viewFollowList = (param) => {
		axios
			.get(import.meta.env.VITE_BASE_URL + "/follows/" + param, {
				params: { nickname: profile.nickname },
				withCredentials: true,
			})
			.then((res) => {
				//console.log(res)
				mySwal.fire({
					title: `${param.replace(param, (txt) => {
						return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase()
					})} List`,
					html: (
						<FollowList
							user={user}
							followList={res.data}
							followCount={profile[`${param}Count`]}
							navigate={navigate}
						/>
					),
					showCancelButton: false,
					showConfirmButton: false,
					showCloseButton: true,
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
							title: "목록 조회 실패",
							icon: "error",
							showConfirmButton: true,
						})
						break
				}
			})
	}

	return (
		<div className="flex flex-col">
			<div className="grid grid-cols-4 grid-rows-6">
				{/* 프로필 이미지 */}
				<div className="row-span-6 m-auto">
					<ProfileImage src={profile.profileImage} />
				</div>

				{/* 닉네임 */}
				<h3 className="col-span-3 row-span-3 flex items-center justify-center text-3xl font-bold">
					{profile.nickname}
				</h3>

				{/* 피드, 팔로잉, 팔로워 */}
				<div className="col-span-3 row-span-2 flex justify-around text-center">
					{/* 피드 */}
					<div className="flex flex-col items-center">
						<p className="font-bold">피드</p>
						<p>{profile.postsCount}</p>
					</div>

					{/* 팔로잉 */}
					<div
						className="flex flex-col items-center"
						onClick={() => viewFollowList("following")}
					>
						<p className="font-bold cursor-pointer">팔로잉</p>
						<p className="cursor-pointer">{profile.followingCount}</p>
					</div>

					{/* 팔로워 */}
					<div
						className="flex flex-col items-center"
						onClick={() => viewFollowList("follower")}
					>
						<p className="font-bold cursor-pointer">팔로워</p>
						<p className="cursor-pointer">{profile.followerCount}</p>
					</div>
				</div>
			</div>
			<div className="flex flex-col mb-3">
				<div className="flex flex-col items-start">
					<p className="font-bold">프로필 소개글</p>
					<p>{profile.description ? profile.description : "소개글이 없습니다."}</p>
				</div>
			</div>
			{activeButton()}
		</div>
	)
}

export default ProfileInfo
