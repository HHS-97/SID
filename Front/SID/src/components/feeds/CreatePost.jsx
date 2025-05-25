import { useState } from "react"

const CreatePost = ({ onCreatePost }) => {
	const [image, setImage] = useState(null)

	const onCreate = (e) => {
		e.preventDefault()
		// 버튼 비활성화
		const createButton = document.querySelector("button[type=submit]")
		createButton.disabled = true
		createButton.classList.add("hidden")
		const form = new FormData(e.target)
		onCreatePost(form, image)
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
		<div className="my-5">
			<form
				id="createPost"
				onSubmit={onCreate}
				className="flex flex-col space-y-5 bg-pink-50 p-6 rounded-2xl shadow-lg animate-fadeIn"
			>
				<span className="bg-pink-50 text-pink-700 font-medium text-xl px-3 py-1 rounded-md">
					게시글 작성하기
				</span>
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
					className="resize-none border border-pink-200 rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-pink-300 shadow-md"
					style={{ textAlign: "start" }}
					rows="5"
					id="content"
					name="content"
					placeholder="글 작성하기 ..."
				/>

				{image && (
					<div className="flex flex-col items-center space-y-3">
						<p className="font-medium text-pink-500">[첨부 이미지 미리보기]</p>
						<img
							src={image.previewUrl}
							alt="preview"
							className="w-1/2 h-1/2 object-cover rounded-lg border-2 border-pink-200 shadow-sm"
						/>
					</div>
				)}

				<div className="flex items-center space-x-4 justify-end">
					<button
						className="py-2 px-8 rounded-lg bg-gradient-to-r from-pink-300 to-pink-400 text-white font-bold text-lg shadow-md hover:from-pink-400 hover:to-pink-500 transition duration-300"
						type="submit"
					>
						작성
					</button>
				</div>
			</form>
		</div>
	)
}

export default CreatePost
