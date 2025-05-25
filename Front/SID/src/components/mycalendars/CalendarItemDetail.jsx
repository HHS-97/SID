import { useMemo } from "react"
import Swal from "sweetalert2"
import withReactContent from "sweetalert2-react-content"
import UpdateSchedule from "./UpdateSchedule"

const CalendarItemDetail = ({ schedule, updateSchedule, deleteSchedule }) => {
	const mySwal = withReactContent(Swal)
	const scheduleTime = useMemo(() => {
		const start = new Date(schedule.start)
		const end = new Date(schedule.end)
		const startDay = `${start.getMonth() + 1}월 ${start.getDate()}일`
		const endDay = `${end.getMonth() + 1}월 ${end.getDate()}일`
		const startHour = String(start.getHours()).padStart(2, "0")
		const startMin = String(start.getMinutes()).padStart(2, "0")
		const endHour = String(end.getHours()).padStart(2, "0")
		const endMin = String(end.getMinutes()).padStart(2, "0")
		return `${startDay} ${startHour}:${startMin} - ${startDay !== endDay ? endDay : ""} ${endHour}:${endMin}`
	}, [schedule.start, schedule.end])

	const showUpdateSchedule = () => {
		mySwal.fire({
			title: "일정 수정",
			html: <UpdateSchedule schedule={schedule} updateSchedule={updateSchedule} />,
			showConfirmButton: false,
		})
	}

	const onDelete = () => {
		mySwal
			.fire({
				title: "일정 삭제",
				text: "정말 삭제하시겠습니까?",
				icon: "warning",
				showCancelButton: true,
				confirmButtonColor: "#d33",
				cancelButtonColor: "#3085d6",
				confirmButtonText: "삭제",
				cancelButtonText: "취소",
			})
			.then((result) => {
				if (result.isConfirmed) {
					deleteSchedule(schedule.scheduleId)
				}
			})
	}

	return (
		<div className="grid grid-cols-4 gap-5">
			<p className="col-span-3 my-auto mr-auto">🕒 {scheduleTime}</p>
			<p className="col-span-1 text-sm my-auto">
				등록 : <span className="font-bold">{schedule.nickname}</span>
			</p>
			<p className="col-span-4">{schedule.memo}</p>
			<p className="col-span-4">
				{Number(schedule.alarmTime) > 0 && Number(schedule.alarmTime) < 24
					? `${schedule.alarmTime}시간 전 알람`
					: Number(schedule.alarmTime) > 0 && `${schedule.alarmTime / 24}일 전 알람`}
			</p>
			<button
				className="col-span-2 bg-pink-100 rounded border py-1"
				onClick={showUpdateSchedule}
			>
				수정
			</button>
			<button className="col-span-2 bg-red-700 text-white rounded py-1" onClick={onDelete}>
				삭제
			</button>
		</div>
	)
}

export default CalendarItemDetail
