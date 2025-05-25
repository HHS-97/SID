import { useMemo } from "react"
import Swal from "sweetalert2"
import withReactContent from "sweetalert2-react-content"
import CalendarItemDetail from "./CalendarItemDetail"

const CalendarItem = ({ schedule, updateSchedule, deleteSchedule }) => {
	const scheduleTime = useMemo(() => {
		const start = new Date(schedule.start)
		const end = new Date(schedule.end)
		const startDay = `${start.getMonth() + 1}월 ${start.getDate()}일`
		const endDay = `${end.getMonth() + 1}월 ${end.getDate()}일`
		return `${startDay}${startDay !== endDay ? " - " + endDay : ""}`
	}, [schedule.start, schedule.end])

	const mySwal = withReactContent(Swal)
	const showDetail = () => {
		// //console.log(schedule)
		mySwal.fire({
			title: `제목 : ${schedule.title}`,
			html: (
				<CalendarItemDetail
					schedule={schedule}
					updateSchedule={updateSchedule}
					deleteSchedule={deleteSchedule}
				/>
			),
			showCloseButton: true,
			showConfirmButton: false,
		})
	}

	return (
		<div
			className="my-5 flex cursor-pointer items-center space-x-3 rounded-3xl border border-pink-200 bg-pink-50 p-4 shadow-sm transition hover:bg-pink-100 hover:shadow-md"
			onClick={showDetail}
		>
			<span className="text-2xl">📆</span>
			<span className="mr-auto font-bold text-lg text-pink-600">{schedule.title}</span>
			<span className="text-sm text-pink-500">{scheduleTime}</span>
		</div>
	)
}

export default CalendarItem
