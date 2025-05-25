import axios from "axios"
import moment from "moment"
import { useEffect, useMemo, useState } from "react"
import Calendar from "react-calendar"
import "react-calendar/dist/Calendar.css"
import Swal from "sweetalert2"
import withReactContent from "sweetalert2-react-content"
import CalendarItem from "../components/mycalendars/CalendarItem"
import CreateSchedule from "../components/mycalendars/CreateSchedule"
import "../components/mycalendars/mycalendars.css"

const MyCalendar = () => {
	const [userCalendar, setUserCalendar] = useState([])
	const [selectDate, setSelectDate] = useState(new Date())
	const [selectView, setSelectView] = useState("all")

	const mySwal = withReactContent(Swal)

	const selectCalendar = useMemo(() => {
		if (selectView === "all") {
			return userCalendar
		} else {
			return userCalendar.filter((schedule) => {
				const start = new Date(schedule.start)
				const end = new Date(schedule.end)
				return (
					start.getDate() <= selectDate.getDate() && end.getDate() >= selectDate.getDate()
				)
			})
		}
	}, [selectDate, selectView, userCalendar])

	const createNewSchedule = (schedule) => {
		setUserCalendar([schedule, ...userCalendar])
		mySwal.close()
	}

	const showCreateSchedule = () => {
		mySwal.fire({
			title: "일정 추가",
			html: <CreateSchedule createNewSchedule={createNewSchedule} />,
			showConfirmButton: false,
		})
	}

	const updateSchedule = (schedule) => {
		setUserCalendar(
			userCalendar.map((item) =>
				String(item.scheduleId) === String(schedule.scheduleId) ? schedule : item,
			),
		)
	}

	const deleteSchedule = (scheduleId) => {
		axios
			.delete(import.meta.env.VITE_BASE_URL + `/calendar`, {
				headers: { "Content-Type": "application/json" },
				withCredentials: true,
				data: { scheduleId },
			})
			.then((res) => {
				//console.log(res)
				setUserCalendar(
					userCalendar.filter(
						(schedule) => String(schedule.scheduleId) !== String(scheduleId),
					),
				)
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
							title: "일정 삭제 실패",
							icon: "error",
							showConfirmButton: true,
						})
						break
				}
			})
	}

	useEffect(() => {
		axios
			.get(import.meta.env.VITE_BASE_URL + "/calendar/user", {
				headers: { "Content-Type": "application/json" },
				withCredentials: true,
			})
			.then((res) => {
				//console.log(res.data)
				setUserCalendar(res.data)
			})
			.catch((err) => {
				// //console.log(err)
				switch (err.status) {
					case 401:
						alert("로그인 만료")
						localStorage.removeItem("user")
						window.location.href = "/user/login"
						break
					case 404:
						setUserCalendar([])
						break
					default:
						Swal.fire({
							title: "일정 조회 실패",
							icon: "error",
							showConfirmButton: true,
						})
						break
				}
			})
	}, [])

	return (
		<div className="flex flex-col">
			<h1 className="my-5 font-bold text-4xl text-pink-500 drop-shadow-[2px_2px_4px_rgba(0,0,0,0.2)]">
				Calendar
			</h1>
			<div>
				<div className="flex items-center">
					<select
						name="view"
						id="view"
						className="ml-auto mr-4 rounded-lg border border-pink-300 bg-pink-100 p-2 text-sm text-pink-700 focus:outline-none focus:ring-2 focus:ring-pink-300"
						onChange={(e) => setSelectView(e.target.value)}
					>
						<option value="all">전체 보기</option>
						<option value="select">선택한 날짜 보기</option>
					</select>

					<button
						className="mr-4 rounded-lg bg-pink-400 px-3 py-2 text-sm text-white shadow-sm hover:bg-pink-500 transition"
						onClick={() => setSelectDate(new Date())}
					>
						오늘 보기
					</button>
				</div>
			</div>

			<Calendar
				onChange={setSelectDate}
				value={selectDate}
				className="mx-auto mt-4 rounded-2xl border border-pink-200 bg-pink-50 p-3 shadow-md"
				locale="ko"
				formatDay={(locale, date) => moment(date).format("D")}
				showNeighboringMonth={false}
				calendarType="gregory"
			/>

			<hr className="my-8 border-pink-300 border-2" />

			<button
				className="ml-auto rounded-lg bg-pink-400 px-4 py-2 text-sm text-white shadow-sm hover:bg-pink-500 transition"
				onClick={showCreateSchedule}
			>
				일정 추가
			</button>

			{selectCalendar.length > 0 ? (
				selectCalendar.map((schedule) => (
					<CalendarItem
						key={schedule.scheduleId}
						schedule={schedule}
						updateSchedule={updateSchedule}
						deleteSchedule={deleteSchedule}
					/>
				))
			) : (
				<div className="mt-4 opacity-70 rounded-lg bg-pink-100 p-4 text-center text-pink-600">
					일정이 생성 해보세요 !
				</div>
			)}
		</div>
	)
}

export default MyCalendar
