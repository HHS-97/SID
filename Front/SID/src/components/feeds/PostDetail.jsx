import axios from "axios"
import { useEffect, useMemo, useState } from "react"
import Swal from "sweetalert2"
import withReactContent from "sweetalert2-react-content"
import ProfileImage from "../../assets/ProfileImage"
import CommentItem from "./CommentItem"
import UpdatePost from "./UpdatePost"

const PostDetail = ({
	post,
	user,
	isLogin,
	onUpdatePost,
	onDeletePost,
	onMove,
	onComment,
	onLikePost,
}) => {
	const [comments, setComments] = useState([])

	const [reaction, setReaction] = useState(post.reaction)
	const [likeCount, setLikeCount] = useState(post.likeCount)
	const [dislikeCount, setDislikeCount] = useState(post.dislikeCount)

	const mySwal = withReactContent(Swal)

	const getComments = () => {
		axios
			.get(import.meta.env.VITE_BASE_URL + "/posts/more", {
				headers: { "Content-Type": "application/json" },
				params: { postId: post.postId, page: "0" },
				withCredentials: true,
			})
			.then((res) => {
				setComments(res.data)
				//console.log("댓글 데이터 :", res.data)
			})
			.catch((err) => {
				//console.log("댓글 조회 에러", err)
				//console.log("포스트 아이디 : ", post.postId)
			})
	}

	const createComment = (e) => {
		e.preventDefault()
		const comment = e.target.newComment.value
		if (!comment.trim()) {
			alert("댓글을 입력해주세요 !")
			return
		}

		axios
			.post(
				import.meta.env.VITE_BASE_URL + "/comments",
				{ content: comment, postId: post.postId },
				{
					headers: { "Content-Type": "application/json" },
					withCredentials: true,
				},
			)
			.then((res) => {
				//console.log("댓글 응답 : ", res)
				setComments([
					...comments,
					{
						commentId: res.data.commentId,
						writer: user.lastProfile,
						content: comment,
						createdAt: new Date(),
						time: "방금",
						likeCount: 0,
						dislikeCount: 0,
						reaction: null,
					},
				])
				e.target.newComment.value = ""
				onComment("create")
			})
			.catch((err) => {
				//console.log("댓글 오류 메세지 : ", err)
				switch (err.status) {
					case 401:
						alert("로그인 만료")
						localStorage.removeItem("user")
						window.location.href = "/user/login"
						break
					default:
						Swal.fire({
							title: "댓글 작성 실패",
							icon: "error",
							showConfirmButton: true,
						})
						break
				}
			})
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
		Swal.close()
	}

	const onLike = (e) => {
		e.preventDefault()
		e.stopPropagation()
		const btn = e.currentTarget.dataset.value
		onLikePost(post.postId, btn, reaction)
		if (reaction === null) {
			if (btn === "like") {
				setReaction("true")
				setLikeCount(likeCount + 1)
			} else if (btn === "dislike") {
				setReaction("false")
				setDislikeCount(dislikeCount + 1)
			}
		} else if (reaction === "true") {
			if (btn === "like") {
				setReaction(null)
				setLikeCount(likeCount - 1)
			} else if (btn === "dislike") {
				setReaction("false")
				setLikeCount(likeCount - 1)
				setDislikeCount(dislikeCount + 1)
			}
		} else if (reaction === "false") {
			if (btn === "like") {
				setReaction("true")
				setLikeCount(likeCount + 1)
				setDislikeCount(dislikeCount - 1)
			} else if (btn === "dislike") {
				setReaction(null)
				setDislikeCount(dislikeCount - 1)
			}
		}
	}

	const onUpdateComment = (content, commentId) => {
		// //console.log("수정", content, commentId)
		axios
			.patch(
				import.meta.env.VITE_BASE_URL + "/comments",
				{ content, commentId, postId: post.postId },
				{ withCredentials: true },
			)
			.then((res) => {
				//console.log(res)
				setComments(
					comments.map((comment) => {
						if (String(comment.commentId) === String(commentId)) {
							return { ...comment, content }
						}
						return comment
					}),
				)
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
							title: "댓글 수정 실패",
							icon: "error",
							showConfirmButton: true,
						})
						break
				}
			})
	}

	const onDeleteComment = (commentId) => {
		axios
			.delete(import.meta.env.VITE_BASE_URL + "/comments", {
				headers: { "Content-Type": "application/json" },
				data: { commentId, postId: post.postId },
				withCredentials: true,
			})
			.then((res) => {
				//console.log(res)
				setComments(
					comments.filter((comment) => String(comment.commentId) !== String(commentId)),
				)
				onComment("delete")
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
							title: "댓글 삭제 실패",
							icon: "error",
							showConfirmButton: true,
						})
						break
				}
			})
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

	useEffect(() => {
		getComments()
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [])

	return (
		<div className="bg-white rounded-xl bg-[radial-gradient(circle,_rgba(255,230,235,0.7)_30%,_white_100%)]">
			<div className="my-5 max-h-[80vh] overflow-y-auto hide-scrollbar">
				{/* 게시글 영역 */}
				<div>
					<div className="flex space-x-5 px-2 py-1">
						<div className="flex space-x-5 my-auto" onClick={onMove}>
							<ProfileImage src={post.writer.profileImage} size="50" />
							<span className="my-auto font-bold">{post.writer.nickname}</span>
						</div>
						<span className="my-auto ms-auto">{post.time}</span>
					</div>
					{post.image && (
						<img
							src={ImageSrc}
							alt="post"
							className="w-1/2 h-1/2 object-cover mx-auto my-5"
						/>
					)}
					<p style={{ textAlign: "start" }} className="px-2 my-8">
						{post.content}
					</p>
					<div className="px-2 flex z-[800]">
						<span
							className="flex items-center justify-center h-10"
							onClick={onLike}
							data-value="like"
						>
							{reaction === "true" ? (
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
						<span className="my-auto me-2">{likeCount}</span>
						<span
							className="flex items-center justify-center h-11"
							onClick={onLike}
							data-value="dislike"
						>
							{reaction === "false" ? (
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
							<span className="my-auto">{dislikeCount}</span>
						)}
						<span className="flex items-center justify-center h-10 ms-2">
							<img
								src="/buttonIcon/commentIcon.png"
								alt="Home Icon"
								className="w-3/4 h-3/4 object-contain"
							/>
						</span>
						<span className="my-auto me-2">{post.commentCount}</span>
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
				{/* 댓글 생성 */}

				{isLogin && (
					<form
						id="createCommentForm"
						className="w-full flex space-x-5 my-5"
						onSubmit={createComment}
					>
						<textarea
							name="newComment"
							id="newComment"
							className="flex-1 resize-none border rounded-md p-2 mx-4 border-rose-200 border-2"
							rows="1"
							style={{ textAlign: "start" }}
						/>
						<button className="px-3 mx-4 py-2 rounded-lg bg-gradient-to-r from-rose-200 to-sky-200 text-white font-semibold shadow-md hover:from-rose-300 hover:to-sky-300 transition-all duration-300">
							댓글 작성
						</button>
					</form>
				)}

				{/* 댓글 조회 */}
				<div>
					{comments.length > 0 ? (
						comments.map((comment) => (
							<div key={`comment-${comment.commentId}`} className="my-5">
								<CommentItem
									comment={comment}
									user={user}
									isLogin={isLogin}
									onUpdateComment={onUpdateComment}
									onDeleteComment={onDeleteComment}
									onMove={onMove}
								/>
							</div>
						))
					) : (
						<p className="my-5">댓글이 없습니다.</p>
					)}
				</div>
			</div>
		</div>
	)
}

export default PostDetail
