import axios, { formToJSON } from "axios"
import Swal from "sweetalert2"

const UpdateSchedule = ({ schedule, updateSchedule }) => {
	// 입력창에 표시하기 위해 백엔드 형식("YYYY-MM-DD HH:mm")을
	// datetime-local 형식("YYYY-MM-DDTHH:mm")으로 변환하는 함수
	const onSubmit = (e) => {
		e.preventDefault()
		// FormData를 JSON으로 변환
		let form = formToJSON(new FormData(e.target))

		// datetime-local의 값은 "YYYY-MM-DDTHH:mm" 형식이므로,
		// 백엔드가 요구하는 "YYYY-MM-DD HH:mm" 형식으로 변경
		if (form.startTime) {
			form.startTime = form.startTime.replace("T", " ")
		}
		if (form.endTime) {
			form.endTime = form.endTime.replace("T", " ")
		}

		Swal.fire({
			title: "일정 수정",
			text: "일정을 수정하시겠습니까?",
			showCancelButton: true,
			confirmButtonText: "수정",
			cancelButtonText: "취소",
			showLoaderOnConfirm: true,
		}).then((result) => {
			if (result.isConfirmed) {
				axios
					.patch(import.meta.env.VITE_BASE_URL + "/calendar", form, {
						headers: { "Content-Type": "application/json" },
						withCredentials: true,
					})
					.then((res) => {
						//console.log(res.data)
						updateSchedule(res.data)
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
									title: "일정 수정 실패",
									icon: "error",
									showConfirmButton: true,
								})
								break
						}
					})
			}
		})
	}
	return (
		<form className="flex flex-col space-y-5" id="createSchedule" onSubmit={onSubmit}>
			<input type="hidden" name="scheduleId" defaultValue={schedule.scheduleId} />
			<input
				name="title"
				id="title"
				type="text"
				className="w-full border rounded-md p-2 font-bold text-xl"
				placeholder="일정 제목"
				defaultValue={schedule.title}
			/>
			<div className="flex flex-col space-y-2">
				<label htmlFor="start" className="font-bold" style={{ textAlign: "start" }}>
					시작 시간
				</label>
				<input
					name="startTime"
					id="start"
					type="datetime-local"
					className="w-full border rounded-md p-2"
					defaultValue={schedule.start}
				/>
			</div>
			<div className="flex flex-col space-y-2">
				<label htmlFor="end" className="font-bold" style={{ textAlign: "start" }}>
					종료 시간
				</label>
				<input
					name="endTime"
					id="end"
					type="datetime-local"
					className="w-full border rounded-md p-2"
					defaultValue={schedule.end}
				/>
			</div>
			<div className="flex flex-col space-y-2">
				<label htmlFor="memo" className="font-bold" style={{ textAlign: "start" }}>
					메모
				</label>
				<textarea
					name="memo"
					id="memo"
					rows={5}
					className="w-full border rounded-md p-2 resize-none"
					placeholder="메모"
					defaultValue={schedule.memo}
				></textarea>
			</div>
			<div className="flex flex-col space-y-2">
				<label htmlFor="alarmTime" className="font-bold" style={{ textAlign: "start" }}>
					알람 설정
				</label>
				<select
					name="alarmTime"
					id="alarmTime"
					className="w-full border rounded-md p-2"
					defaultValue={schedule.alarmTime}
				>
					<option value="0">알람 없음</option>
					<option value="1">1시간 전</option>
					<option value="12">12시간 전</option>
					<option value="24">1일 전</option>
					<option value="48">2일 전</option>
					<option value="72">3일 전</option>
					<option value="168">1주 전</option>
				</select>
			</div>
			<div>
				<button className="p-2 border-1 rounded-md bg-blue-700 text-white">수정</button>
				<button
					className="p-2 border-1 rounded-md bg-gray-100"
					onClick={(e) => {
						e.preventDefault()
						Swal.close()
					}}
				>
					취소
				</button>
			</div>
		</form>
	)
}

export default UpdateSchedule
