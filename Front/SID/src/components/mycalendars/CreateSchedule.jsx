import axios, { formToJSON } from "axios"
import { useMemo } from "react"
import Swal from "sweetalert2"

const CreateSchedule = ({ createNewSchedule }) => {
	const formatDatetimeLocal = (date) => {
		const pad = (num) => num.toString().padStart(2, "0")
		const year = date.getFullYear()
		const month = pad(date.getMonth() + 1) // getMonth()는 0부터 시작
		const day = pad(date.getDate())
		const hours = pad(date.getHours())
		const minutes = pad(date.getMinutes())
		return `${year}-${month}-${day}T${hours}:${minutes}`
	}

	const defaultStartTime = useMemo(() => {
		const now = new Date()
		// UTC 시간을 구합니다.
		const utc = now.getTime() + now.getTimezoneOffset() * 60000
		// UTC에 9시간(540분)을 더하면 한국 시간이 됩니다.
		const koreaNow = new Date(utc + 9 * 60 * 60000)
		// 1시간 후 계산
		const oneHourLater = new Date(koreaNow.getTime() + 1 * 60 * 60 * 1000)
		return formatDatetimeLocal(oneHourLater)
	}, [])

	const defaultEndTime = useMemo(() => {
		const now = new Date()
		const utc = now.getTime() + now.getTimezoneOffset() * 60000
		const koreaNow = new Date(utc + 9 * 60 * 60000)
		// 2시간 후 계산
		const twoHoursLater = new Date(koreaNow.getTime() + 2 * 60 * 60 * 1000)
		return formatDatetimeLocal(twoHoursLater)
	}, [])

	const onSubmit = (e) => {
		e.preventDefault()
		const form = new FormData(e.target)
		// form 데이터를 JSON 객체로 변환
		let data = formToJSON(form)
		// datetime-local의 값은 "YYYY-MM-DDTHH:mm" 형식이므로, 백엔드가 원하는 "YYYY-MM-DD HH:mm" 형식으로 변경
		if (data.startTime) {
			data.startTime = data.startTime.replace("T", " ")
		}
		if (data.endTime) {
			data.endTime = data.endTime.replace("T", " ")
		}
		// 확인 창
		Swal.fire({
			title: "일정 추가",
			text: "일정을 추가하시겠습니까?",
			showCancelButton: true,
			confirmButtonText: "추가",
			cancelButtonText: "취소",
			showLoaderOnConfirm: true,
		}).then((result) => {
			if (result.isConfirmed) {
				axios
					.post(import.meta.env.VITE_BASE_URL + "/calendar", data, {
						headers: { "Content-Type": "application/json" },
						withCredentials: true,
					})
					.then((res) => {
						//console.log(res.data)
						createNewSchedule(res.data)
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
									title: "일정 추가 실패",
									icon: "error",
									showConfirmButton: true,
								})
								break
						}
					})
			} else {
				Swal.close()
			}
		})
	}

	return (
		<form
			className="flex flex-col space-y-5 rounded-3xl bg-pink-50 p-6 shadow-sm"
			id="createSchedule"
			onSubmit={onSubmit}
		>
			<input
				name="title"
				id="title"
				type="text"
				className="w-full rounded-2xl border border-pink-200 bg-pink-100 p-3 font-semibold text-lg text-pink-800 placeholder-pink-400 focus:outline-none focus:ring-2 focus:ring-pink-300"
				placeholder="일정 제목"
			/>

			<div className="flex flex-col space-y-2">
				<label htmlFor="start" className="font-medium text-pink-700 text-start">
					시작 시간
				</label>
				<input
					name="startTime"
					id="start"
					type="datetime-local"
					className="w-full rounded-2xl border border-pink-200 bg-pink-100 p-3 text-pink-800 focus:outline-none focus:ring-2 focus:ring-pink-300"
					defaultValue={defaultStartTime}
				/>
			</div>

			<div className="flex flex-col space-y-2">
				<label htmlFor="end" className="font-medium text-pink-700 text-start">
					종료 시간
				</label>
				<input
					name="endTime"
					id="end"
					type="datetime-local"
					className="w-full rounded-2xl border border-pink-200 bg-pink-100 p-3 text-pink-800 focus:outline-none focus:ring-2 focus:ring-pink-300"
					defaultValue={defaultEndTime}
				/>
			</div>

			<div className="flex flex-col space-y-2">
				<label htmlFor="memo" className="font-medium text-pink-700 text-start">
					메모
				</label>
				<textarea
					name="memo"
					id="memo"
					rows={5}
					className="w-full rounded-2xl border border-pink-200 bg-pink-100 p-3 resize-none text-pink-800 placeholder-pink-400 focus:outline-none focus:ring-2 focus:ring-pink-300"
					placeholder="메모를 입력하세요"
				></textarea>
			</div>

			<div className="flex flex-col space-y-2">
				<label htmlFor="alarmTime" className="font-medium text-pink-700 text-start">
					알람 설정
				</label>
				<select
					name="alarmTime"
					id="alarmTime"
					className="w-full rounded-2xl border border-pink-200 bg-pink-100 p-3 text-pink-800 focus:outline-none focus:ring-2 focus:ring-pink-300"
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

			<div className="flex justify-between space-x-3">
				<button className="w-full rounded-2xl bg-pink-400 p-3 text-white shadow-sm hover:bg-pink-500 transition">
					추가
				</button>
				<button
					className="w-full rounded-2xl bg-pink-200 p-3 text-pink-700 shadow-sm hover:bg-pink-300 transition"
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

export default CreateSchedule
