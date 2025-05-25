import { useMemo } from "react"
import Swal from "sweetalert2"
import withReactContent from "sweetalert2-react-content"
import UpdatePassword from "./UpdatePassword"

const ReadInfo = ({ onEdit, onDelete, info, onUpdatePassword }) => {
	const gender = useMemo(() => {
		switch (info.gender) {
			case "Female":
				return "여성"
			case "Male":
				return "남성"
			default:
				return "알수없음"
		}
	}, [info.gender])

	const mySwal = withReactContent(Swal)

	const showUpadatePassword = () => {
		mySwal.fire({
			title: "비밀번호 변경",
			html: <UpdatePassword onUpdatePassword={onUpdatePassword} />,
			showConfirmButton: false,
		})
	}

	return (
		<div style={{ padding: "2rem", maxWidth: "800px", margin: "0 auto" }}>
			<p style={{ fontSize: "2rem", fontWeight: "700", color: "#4CAF50", marginBottom: "1.5rem", textAlign: "center" }}>
				계정 정보
			</p>

			{/* 이메일 */}
			<div className="grid grid-cols-5 gap-4 mb-6">
				<span style={{ fontWeight: "600", color: "#2d3748" }}>이메일</span>
				<span className="col-span-4" style={{ color: "#4a5568" }}>{info.email}</span>
			</div>

			{/* 이름 */}
			<div className="grid grid-cols-5 gap-4 mb-6">
				<span style={{ fontWeight: "600", color: "#2d3748" }}>이름</span>
				<span className="col-span-4" style={{ color: "#4a5568" }}>{info.name}</span>
			</div>

			{/* 성별 */}
			<div className="grid grid-cols-5 gap-4 mb-6">
				<span style={{ fontWeight: "600", color: "#2d3748" }}>성별</span>
				<span className="col-span-4" style={{ color: "#4a5568" }}>{gender}</span>
			</div>

			{/* 생일 */}
			<div className="grid grid-cols-5 gap-4 mb-6">
				<span style={{ fontWeight: "600", color: "#2d3748" }}>생일</span>
				<span className="col-span-4" style={{ color: "#4a5568" }}>
					{info.birthDate
						? `${info.birthDate.split("-")[0]}년 ${info.birthDate.split("-")[1]}월 ${info.birthDate.split("-")[2]}일`
						: "생일을 수정해주세요."}
				</span>
			</div>

			{/* 전화번호 */}
			<div className="grid grid-cols-5 gap-4 mb-4">
				<span style={{ fontWeight: "600", color: "#2d3748" }}>전화번호</span>
				<span className="col-span-4" style={{ color: "#4a5568" }}>
					{info.phone ? info.phone : "전화번호를 수정해주세요."}
				</span>
			</div>

			{/* 버튼 영역 */}
			<div className="grid grid-cols-2 gap-4 mb-4">
				<button
					style={{
						backgroundColor: "#81C784", // 봄 느낌의 연두색
						color: "#fff",
						padding: "0.8rem",
						borderRadius: "0.375rem",
						border: "1px solid #66BB6A",
						fontWeight: "600",
						transition: "all 0.3s ease",
					}}
					className="hover:bg-[#66BB6A] hover:scale-105"
					onClick={onEdit}
				>
					정보 편집
				</button>
				<button
					style={{
						backgroundColor: "#FF7043", // 살짝 오렌지 톤
						color: "#fff",
						padding: "0.8rem",
						borderRadius: "0.375rem",
						border: "1px solid #FF5722",
						fontWeight: "600",
						transition: "all 0.3s ease",
					}}
					className="hover:bg-[#FF5722] hover:scale-105"
					onClick={onDelete}
				>
					계정 탈퇴
				</button>
			</div>

			{/* 비밀번호 변경 버튼 */}
			<button
				onClick={showUpadatePassword}
				style={{
					marginTop: "1rem",
					backgroundColor: "#FFEB3B", // 밝은 노란색
					color: "#000",
					fontWeight: "600",
					padding: "0.8rem",
					borderRadius: "0.375rem",
					width: "100%",
					border: "1px solid #FFCA28",
					transition: "all 0.3s ease",
				}}
				className="hover:bg-[#FFC107] hover:scale-105"
			>
				비밀번호 변경
			</button>
		</div>
	)
}

export default ReadInfo
