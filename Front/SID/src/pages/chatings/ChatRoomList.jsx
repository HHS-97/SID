import axios from "axios"
import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { useRecoilValue } from "recoil"
import { userAtom } from "../../atoms/userAtom"
import Swal from "sweetalert2"

const ChatRoomList = () => {
	const [chatRooms, setChatRooms] = useState([])
	const [roomName, setRoomName] = useState("")
	const navigate = useNavigate()
	const nickname = useRecoilValue(userAtom).lastProfile.nickname

	// 채팅방 목록 불러오기
	const fetchChatRooms = () => {
		axios
			.get(import.meta.env.VITE_BASE_URL + "/chatroom", {
				headers: { "Content-Type": "application/json" },
				withCredentials: true,
			})
			.then((response) => {
				//console.log(response.data)
				setChatRooms(response.data.chatRoomList)
			})
			.catch((error) => {
				console.error("채팅방 목록 불러오기 실패:", error)
			})
	}

	useEffect(() => {
		//console.log(chatRooms)
		fetchChatRooms()
	}, [])

	// 채팅방 생성 요청
	const createRoom = () => {
		if (!roomName.trim()) return
		axios
			.post(
				import.meta.env.VITE_BASE_URL + "/chatroom",
				{ receiver: roomName }, // 데이터
				{
					headers: { "Content-Type": "application/json" },
					withCredentials: true,
				}, // 옵션
			)
			.then(() => {
				setRoomName("") // 입력 필드 초기화
				fetchChatRooms() // 채팅방 목록 새로고침
			})
			.catch((error) => {
				console.error("채팅방 생성 실패:", error)
				Swal.fire({
					title: "오류",
					text: "대상자가 없습니다!",
					icon: "error",
					confirmButtonColor: "#ff7675",
				})
			})
	}

	return (
		<div className="flex flex-col items-center p-4 bg-cream-50 min-h-screen">
			<h2 className="text-2xl font-bold mb-4 text-rose-500">채팅방 목록</h2>

			{/* 채팅방 생성 */}
			<div className="p-4 flex flex-nowrap gap-2 w-full max-w-md bg-white shadow-md rounded-lg">
			<input
				type="text"
				value={roomName}
				onChange={(e) => setRoomName(e.target.value)}
				className="border p-2 flex-1 min-w-0 rounded-md focus:ring-2 focus:ring-rose-300 outline-none"
				placeholder="채팅방 이름 입력"
			/>
			<button
				onClick={createRoom}
				className="flex-shrink-0 bg-gradient-to-r from-rose-300 to-rose-400 text-white px-4 py-2 rounded-md hover:shadow-lg transition"
			>
				생성
			</button>
			</div>

			{/* 채팅방 목록 */}
			<ul className="w-full max-w-md mt-4">
				{chatRooms.map((room) => (
					<li
						key={room.roomId}
						className="p-4 bg-white shadow-md rounded-lg mb-3 cursor-pointer hover:bg-rose-100 transition"
						onClick={() => navigate(`/chat/${room.roomId}?roomName=${encodeURIComponent(room.roomName !== nickname ? room.roomName : room.receiverNickname)}`)}
					>
						<span className="text-lg font-medium text-gray-800">
							{room.roomName !== nickname ? room.roomName : room.receiverNickname}
						</span>
					</li>
				))}
			</ul>
		</div>
	)
}

export default ChatRoomList