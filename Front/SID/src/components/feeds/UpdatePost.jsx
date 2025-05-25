import { useState } from "react"

const UpdatePost = ({ post, onUpdatePost }) => {
	const [image, setImage] = useState(
		post.image
			? {
					imageFile: null,
					previewUrl: post.image.startsWith("new")
						? post.image.replace("new", "")
						: import.meta.env.VITE_IMAGE_URL +
							"/" +
							post.image.replace("src/main/resources/uploads/", ""),
				}
			: { imageFile: null, previewUrl: null },
	)
	const onUpdate = (e) => {
		e.preventDefault()
		const form = new FormData(e.target)
		form.append("postId", post.postId)
		onUpdatePost(form, image)
	}

	const onImageUpload = (e) => {
		const uploadedFile = e.target.files[0]
		if (uploadedFile) {
			if (uploadedFile.size > 1024 * 1024 * 10) {
				alert("10MB 이하의 파일만 업로드 가능합니다.")
				return
			} else {
				//console.log(uploadedFile)

				const reader = new FileReader()
				reader.onload = (e) => {
					setImage({
						imageFile: uploadedFile,
						previewUrl: e.target.result,
					})
				}
				reader.readAsDataURL(uploadedFile)
			}
		}
	}

	return (
		<div>
			<form id="updatePost" onSubmit={onUpdate} className="flex flex-col space-y-5">
				<div className="bg-pink-100 p-3 rounded-lg shadow-md hover:bg-pink-200 transition duration-300">
					<label
						htmlFor="postImage"
						className="cursor-pointer text-pink-600 font-semibold hover:text-pink-800"
					>
						📷 사진 등록 하기
					</label>
					<input
						type="file"
						id="postImage"
						name="image"
						accept="image/*"
						className="hidden"
						onChange={onImageUpload}
					/>
				</div>
				<textarea
					className="resize-none border border-gray-300 rounded-md p-2"
					style={{ textAlign: "start" }}
					rows="5"
					id="content"
					name="content"
					defaultValue={post.content}
				/>
				{image.previewUrl && (
					<div className="flex flex-col items-center space-y-3">
						<p className="font-medium text-pink-500">[첨부 이미지 미리보기]</p>
						<img
							src={image.previewUrl}
							alt="preview"
							className="w-1/2 h-1/2 object-cover rounded-lg border-2 border-pink-200 shadow-sm"
						/>
					</div>
				)}
				<button
					className="py-2 border-1 rounded-md bg-red-300 font-bold text-xl"
					type="submit"
				>
					수정
				</button>
			</form>
		</div>
	)
}

export default UpdatePost
