import { useState } from "react"
import Swal from "sweetalert2"
import ProfileImage from "../../assets/ProfileImage"

const CommentItem = ({ comment, user, isLogin, onUpdateComment, onDeleteComment, onMove }) => {
	const [isEdit, setIsEdit] = useState(false)

	const onUpdate = () => {
		//console.log("수정")
		setIsEdit(true)
	}

	const updateComment = (e) => {
		e.preventDefault()
		onUpdateComment(e.target.content.value, comment.commentId)
		e.target.reset()
		setIsEdit(false)
	}

	const onDelete = () => {
		onDeleteComment(comment.commentId)
	}

	return (
		<>
			<div className="flex space-x-5 px-2">
				<div
					className="flex space-x-5 my-auto"
					onClick={(e) => {
						onMove(e, comment.writer.nickname)
						Swal.close()
					}}
				>
					<ProfileImage src={comment.writer.profileImage} size="50" />
					<span className="my-auto font-bold">{comment.writer.nickname}</span>
				</div>
				<span className="my-auto ms-auto">{comment.time}</span>
			</div>
			<div className="flex">
				{isEdit ? (
					<form
						id="updateCommentForm"
						className="flex-1 flex space-x-3 p-5"
						onSubmit={updateComment}
					>
						<textarea
							name="content"
							id="content"
							className="flex-1 resize-none border rounded-md p-2 hide-scrollbar"
							rows="1"
							style={{ textAlign: "start" }}
							defaultValue={comment.content}
						/>
						<button className="bg-gradient-to-r from-rose-300 to-rose-400 text-white px-4 py-2 rounded-md hover:shadow-lg transition">
							수정
						</button>
						<button
							className="bg-gradient-to-r from-gray-300 to-gray-400 text-white px-4 py-2 rounded-md hover:shadow-lg transition"
							onClick={(e) => {
								e.preventDefault()
								setIsEdit(false)
							}}
						>
							취소
						</button>
					</form>
				) : (
					<p style={{ textAlign: "start" }} className="px-2 my-5 flex-1">
						{comment.content}
					</p>
				)}
				{isLogin && !isEdit && user?.lastProfile?.nickname === comment.writer.nickname && (
					<>
						<span className="my-auto mx-2 h-7" onClick={onUpdate}>
							<img
								src="/buttonIcon/editIcon.png"
								alt="수정"
								className="w-3/4 h-3/4 object-contain"
							/>
						</span>
						<span className="my-auto mx-2 h-7" onClick={onDelete}>
							<img
								src="/buttonIcon/deleteIcon.png"
								alt="삭제"
								className="w-3/4 h-3/4 object-contain"
							/>
						</span>
					</>
				)}
			</div>
		</>
	)
}

export default CommentItem
