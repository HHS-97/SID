import { useEffect, useRef, useState } from "react"
import { useNavigate, useParams, useLocation } from "react-router-dom"
import { useRecoilValue } from "recoil"
import SockJS from "sockjs-client"
import { Client } from "@stomp/stompjs"
import { userAtom } from "../../atoms/userAtom"
import axios from "axios"
import { IoArrowBack } from "react-icons/io5"

const TestChatRoom = () => {
	const { chatroomId } = useParams()
	const [messages, setMessages] = useState([])
	const [message, setMessage] = useState("")
	const clientRef = useRef(null)
	const isMounted = useRef(true)
	const nickname = useRecoilValue(userAtom).lastProfile.nickname
	const firstEntryRef = useRef(true)
	const nav = useNavigate()
	const messagesEndRef = useRef(null)
	const location = useLocation()
	const queryParams = new URLSearchParams(location.search)
	const roomName = queryParams.get("roomName")

	const backRoom = () => {
		nav(-1)
	}

	const scrollToBottom = () => {
		setTimeout(() => {
			messagesEndRef.current?.scrollIntoView({ behavior: "smooth", block: "end" })
		}, 100)
	}

	useEffect(() => {
		scrollToBottom()
	}, [messages])

	useEffect(() => {
		axios
			.get(import.meta.env.VITE_BASE_URL + `/chatroom/${chatroomId}/messages`, {
				headers: { "Content-Type": "application/json" },
				withCredentials: true,
			})
			.then((response) => {
				if (Array.isArray(response.data) && response.data.length > 0) {
					setMessages(response.data)
					firstEntryRef.current = false
				}
			})
			.catch((error) => console.error("메시지 로드 실패:", error))
	}, [chatroomId])

	const sendMessage = () => {
		if (clientRef.current && clientRef.current.connected && message.trim()) {
			const chatMessage = {
				type: "CHAT",
				sender: nickname,
				message: message,
				roomId: chatroomId,
			}
			clientRef.current.publish({
				destination: `/app/${chatroomId}/chat.sendMessage`,
				body: JSON.stringify(chatMessage),
			})
			setMessage("")
			//console.log("Sent:", JSON.stringify(chatMessage))
		}
	}

	const exitRoom = () => {
		axios
			.post(
				import.meta.env.VITE_BASE_URL + `/chatroom/${chatroomId}/leave`,
				{},
				{ headers: { "Content-Type": "application/json" }, withCredentials: true },
			)
			.then((response) => {
				//console.log("채팅방 퇴장 성공:", response.data)
				nav("/chat/chatroomlist")
			})
			.catch((error) => console.error("채팅방 퇴장 실패:", error))
	}

	const connectWebSocket = () => {
		if (clientRef.current && clientRef.current.connected) {
			//console.log("이미 연결되어 있음")
			return
		}
		const socketURL = "/api/chat-ws"
		//console.log("Connecting to:", window.location.origin + socketURL)
		clientRef.current = new Client({
			webSocketFactory: () => new SockJS(socketURL, null, { withCredentials: true }),
			debug: (str) => console.log(str),
			reconnectDelay: 5000,
			heartbeatIncoming: 4000,
			heartbeatOutgoing: 4000,
			onConnect: () => {
				if (!isMounted.current) {
					clientRef.current.deactivate()
					return
				}
				//console.log("Connected to WebSocket", nickname)
				clientRef.current.subscribe(`/topic/chat.${chatroomId}`, (msg) => {
					try {
						//console.log("Received:", msg.body)
						const parsedMsg = JSON.parse(msg.body)
						setMessages((prev) => [...prev, parsedMsg])
					} catch (error) {
						console.error("메시지 파싱 실패:", error)
					}
				})
				// if (firstEntryRef.current) {
				// 	const joinMessage = {
				// 		type: "JOIN",
				// 		sender: nickname,
				// 		message: message, // 혹은 "입장했습니다." 같은 메시지
				// 		roomId: chatroomId,
				// 	}
				// 	clientRef.current.publish({
				// 		destination: `/app/${chatroomId}/chat.sendMessage`,
				// 		body: JSON.stringify(joinMessage),
				// 	})
				// 	setMessage("")
				// }
			},
			onStompError: (frame) => {
				console.error("Broker error: " + frame.headers["message"])
				console.error("Details: " + frame.body)
			},
			onDisconnect: () => {
				//console.log("Disconnected from WebSocket")
			},
		})
		clientRef.current.activate()
	}

	useEffect(() => {
		isMounted.current = true
		connectWebSocket()
		return () => {
			isMounted.current = false
			if (clientRef.current) clientRef.current.deactivate()
		}
	}, [chatroomId, nickname])

	return (
		<div className="flex flex-col h-full opacity-90">
		{/* 상단 헤더 (고정 높이) */}
		<div className="flex-none">
			<div className="flex items-center justify-between p-2 bg-white shadow-md rounded-lg opacity-60 h-16">
			<div className="flex items-center">
				<button onClick={backRoom} className="p-2">
				<IoArrowBack className="text-2xl text-gray-800" />
				</button>
				<span className="text-lg font-semibold ml-2">{roomName}</span>
			</div>
			<button
				onClick={exitRoom}
				className="text-sm bg-red-500 text-white p-3 rounded-lg hover:bg-red-600 transition whitespace-nowrap"
			>
				채팅방 나가기
			</button>
			</div>
		</div>

		{/* 가운데 채팅 메시지 영역 (남은 공간 전부 차지) */}
		<div className="flex-1 p-3 bg-[#EDEDED] rounded-lg shadow-lg overflow-y-auto hide-scrollbar">
			{messages.map((msg, index) => (
			<div
				key={index}
				className={`flex ${msg.sender === nickname ? "justify-end" : "justify-start"} mb-4`}
			>
				{msg.sender !== nickname && (
				<div className="w-8 h-8 rounded-full bg-gray-300 mr-2 flex items-center justify-center">
					<span className="text-white font-bold text-xs">U</span>
				</div>
				)}
				<div className={`flex flex-col ${msg.sender === nickname ? "items-end" : "items-start"}`}>
				<div className="font-semibold text-sm mb-1">{msg.sender}</div>
				<div
					className={`w-fit p-3 rounded-lg ${
					msg.sender === nickname
						? "bg-yellow-400 text-black"
						: "bg-white text-black shadow-md"
					}`}
				>
					{msg.message}
				</div>
				</div>
			</div>
			))}
			<div ref={messagesEndRef}></div>
		</div>

		{/* 하단 입력창 (고정 높이) */}
		<div className="flex-none mt-2">
			<div className="flex flex-nowrap items-center p-2 bg-white rounded-lg shadow-md h-16">
			<input
				type="text"
				className="flex-1 min-w-0 border-none p-3 rounded-l-lg focus:outline-none"
				value={message}
				onChange={(e) => setMessage(e.target.value)}
				placeholder="메시지를 입력하세요..."
				onKeyDown={(e) => e.key === "Enter" && sendMessage()}
			/>
			<button
				onClick={sendMessage}
				className="flex-shrink-0 bg-sky-200 text-black px-4 py-2 rounded-r-lg hover:bg-yellow-500 font-bold whitespace-nowrap"
			>
				전송
			</button>
			</div>
		</div>
		</div>

	)
}

export default TestChatRoom
