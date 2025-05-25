import { useMemo, useState } from "react"
import { useNavigate } from "react-router-dom"
import Swal from "sweetalert2"
import withReactContent from "sweetalert2-react-content"
import ProfileImage from "../../assets/ProfileImage"
import PostDetail from "./PostDetail"
import UpdatePost from "./UpdatePost"

const PostItem = ({ post, user, isLogin, onUpdatePost, onDeletePost, onLikePost }) => {
	const mySwal = withReactContent(Swal)
	const navigate = useNavigate()

	const viewMore = () => {
		//console.log(post)
		mySwal
			.fire({
				html: (
					<PostDetail
						post={post}
						user={user}
						isLogin={isLogin}
						onUpdatePost={onUpdatePost}
						onDeletePost={onDeletePost}
						onMove={onMove}
						onComment={onComment}
						onLikePost={onLikePost}
					/>
				),
				showCloseButton: true,
				showConfirmButton: false,
			})
			.then((result) => {
				//console.log(result)
			})
	}

	const onMove = (e, nickname = post.writer.nickname) => {
		e.preventDefault()
		e.stopPropagation()
		navigate(`/profile/${nickname}`)
	}

	const onUpdate = () => {
		mySwal.fire({
			html: <UpdatePost post={post} onUpdatePost={onUpdatePost} />,
			showCloseButton: true,
			showConfirmButton: false,
		})
	}

	const onDelete = () => {
		onDeletePost(post.postId)
	}

	const onLike = (e) => {
		e.preventDefault()
		e.stopPropagation()
		const btn = e.currentTarget.dataset.value
		const state = post.reaction
		//console.log(btn, state)
		onLikePost(post.postId, btn, state)
	}

	const [commentCount, setCommentCount] = useState(post.commentCount)
	const onComment = (action) => {
		switch (action) {
			case "create":
				setCommentCount(commentCount + 1)
				break
			case "delete":
				setCommentCount(commentCount - 1)
				break
			default:
				break
		}
	}

	const ImageSrc = useMemo(() => {
		if (post.image) {
			return post.image.startsWith("new")
				? post.image.replace("new", "")
				: import.meta.env.VITE_IMAGE_URL +
						"/" +
						post.image.replace("src/main/resources/uploads/", "")
		} else {
			return null
		}
	}, [post.image])

	return (
		<div
			onClick={viewMore}
			className="shadow-lg border border-gray-300 rounded-md my-2 px-1 pt-4 pb-2 transition-transform duration-200 ease-out  active:scale-95"
		>
			<div className="flex space-x-5 px-2">
				<div className="flex space-x-5 my-auto" onClick={onMove}>
					<ProfileImage
						src={post?.writer?.profileImage ? post.writer.profileImage : ""}
						size="50"
					/>
					<span className="my-auto font-bold">{post?.writer?.nickname}</span>
				</div>
				<span className="my-auto ms-auto">{post.time}</span>
			</div>
			{post.image && (
				<img src={ImageSrc} alt="post" className="w-1/2 h-1/2 object-cover mx-auto my-6" />
			)}
			<div className="relative">
				<p
					style={{
						textAlign: "start",
						display: "-webkit-box",
						WebkitLineClamp: 3, // 3줄까지만 보이게 설정
						WebkitBoxOrient: "vertical",
						overflow: "hidden",
						position: "relative",
						cursor: "pointer",
						whiteSpace: "pre-line",
					}}
					className="px-2 my-5"
				>
					{post.content}
				</p>
			</div>
			<div className="px-2 flex z-[800]">
				<span
					className="flex items-center justify-center h-10"
					onClick={onLike}
					data-value="like"
				>
					{post.reaction === "true" ? (
						<img
							src="/buttonIcon/activeLikeIcon.png"
							alt="Home Icon"
							className="w-3/4 h-3/4 object-contain"
						/>
					) : (
						<img
							src="/buttonIcon/noActiveLikeIcon.png"
							alt="Home Icon"
							className="w-3/4 h-3/4 object-contain"
						/>
					)}
				</span>
				<span className="my-auto me-2">{post.likeCount}</span>
				<span
					className="flex items-center justify-center h-11"
					onClick={onLike}
					data-value="dislike"
				>
					{post.reaction === "false" ? (
						<img
							src="/buttonIcon/activeDislikeIcon.png"
							alt="Home Icon"
							className="w-3/4 h-3/4  object-contain"
						/>
					) : (
						<img
							src="/buttonIcon/noActiveDislikeIcon.png"
							alt="Home Icon"
							className="w-3/4 h-3/4 object-contain"
						/>
					)}
				</span>
				{isLogin && post.writer.nickname === user.lastProfile.nickname && (
					<span className="my-auto">{post.dislikeCount}</span>
				)}
				<span className="flex items-center justify-center h-10 ms-2">
					<img
						src="/buttonIcon/commentIcon.png"
						alt="Home Icon"
						className="w-3/4 h-3/4 object-contain"
					/>
				</span>
				<span className="my-auto me-2">{commentCount}</span>
				{isLogin && post.writer.nickname === user.lastProfile.nickname && (
					<>
						<span
							className="ms-auto my-auto h-8"
							onClick={(e) => {
								e.preventDefault()
								e.stopPropagation()
								onUpdate()
							}}
						>
							<img
								src="/buttonIcon/editIcon.png"
								alt="수정"
								className="w-3/4 h-3/4 object-contain"
							/>
						</span>
						<span
							className="my-auto mx-3 h-8"
							onClick={(e) => {
								e.preventDefault()
								e.stopPropagation()
								onDelete()
							}}
						>
							<img
								src="/buttonIcon/deleteIcon.png"
								alt="삭제"
								className="w-3/4 h-3/4 object-contain"
							/>
						</span>
					</>
				)}
			</div>
		</div>
	)
}

export default PostItem
