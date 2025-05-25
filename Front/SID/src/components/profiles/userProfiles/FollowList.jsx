import Swal from "sweetalert2"
import ProfileImage from "../../../assets/ProfileImage"

const FollowList = ({ user, followList, followCount, navigate }) => {
	return (
		<div className="space-y-5 mx-10">
			{followCount ? (
				followList.map((follow, idx) => (
					<div
						key={`follower-${idx}`}
						className="flex space-x-5 px-2"
						onClick={() => {
							Swal.close()
							navigate(`/profile/${follow.nickname}`)
						}}
					>
						<ProfileImage size="50" src={follow.profileImage} />
						<span className="my-auto">{follow.nickname}</span>
						{!follow.isFollowed && user.lastProfile !== follow.nickname && (
							<span className="text-sm p-1 my-auto ml-auto">
								내가 팔로우하지 않은 유저
							</span>
						)}
					</div>
				))
			) : (
				<p>팔로우하는 유저가 없습니다.</p>
			)}
		</div>
	)
}

export default FollowList
