import { useState } from "react"
import axios from "axios"

const TestCreateChatRoom = ({ onRoomCreated }) => {
	const [roomName, setRoomName] = useState("")

	const createRoom = () => {
		if (!roomName.trim()) return

		axios
			.post("http://localhost:8080/api/chatroom", { name: roomName })
			.then((response) => {
				alert("채팅방이 생성되었습니다!")
				onRoomCreated() // 채팅방 목록 새로고침
				setRoomName("")
			})
			.catch((error) => console.error("채팅방 생성 실패:", error))
	}

	return (
		<div className="p-4 flex gap-2">
			<input
				type="text"
				value={roomName}
				onChange={(e) => setRoomName(e.target.value)}
				className="border p-2 flex-1 rounded-md"
				placeholder="채팅방 이름 입력"
			/>
			<button
				onClick={createRoom}
				className="bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600"
			>
				생성
			</button>
		</div>
	)
}

export default TestCreateChatRoom
