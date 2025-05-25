import { useMemo } from "react"
import "./assets.css"

const ProfileImage = ({ size, src, except }) => {
	const imageSrc = useMemo(() => {
		if (src) {
			return (
				import.meta.env.VITE_IMAGE_URL +
				"/" +
				src.replace("src/main/resources/uploads/", "")
			)
		} else {
			return except?.profileKey
				? `/profiles/DP${except.profileKey}.jpg`
				: except?.src
					? except.src
					: "/UserProfileDefault.png"
		}
	}, [src, except])

	const handleError = (e) => {
		e.target.onerror = null
		e.target.src = except?.profileKey
			? `/profiles/DP${except.profileKey}.jpg`
			: except?.src
				? except.src
				: "/UserProfileDefault.png"
	}

	return (
		<div
			className="profile-img-div"
			style={{ width: size ? size + "px" : "100px", height: size ? size + "px" : "100px" }}
		>
			<img
				src={imageSrc}
				alt="프로필 사진"
				className="profile-img-img"
				onError={handleError}
			/>
		</div>
	)
}

export default ProfileImage
