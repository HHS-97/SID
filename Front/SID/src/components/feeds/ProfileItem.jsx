import { useNavigate } from "react-router-dom"
import ProfileImage from "../../assets/ProfileImage"

const ProfileItem = ({ profile }) => {
	const navigate = useNavigate()
	const handleClick = () => {
		navigate(`/profile/${profile.nickname}`)
	}
	return (
		<div className="flex items-center w-full p-3" onClick={handleClick}>
			<ProfileImage src={profile.profileImage} size="50" />
			<p className="ml-3 text-gray-800">{profile.nickname}</p>
		</div>
	)
}

export default ProfileItem
