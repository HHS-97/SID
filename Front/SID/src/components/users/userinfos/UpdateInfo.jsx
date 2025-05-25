import { useRef } from "react"
import Swal from "sweetalert2"

const UpdateInfo = ({ info, onUpdate, onCancle }) => {
	const form = useRef(null)

	const handleSubmit = (e) => {
		e.preventDefault()
		Swal.fire({
			title: "정보 수정",
			text: "수정하시겠습니까?",
			icon: "question",
			showCancelButton: true,
			confirmButtonText: "수정",
			confirmButtonColor: "#4caf50",
			cancelButtonText: "취소",
		}).then((result) => {
			if (result.isConfirmed) {
				const formData = new FormData(e.target)
				onUpdate(formData)
			} else {
				onCancle()
			}
		})
	}

	return (
		<div className="space-y-10">
			<form id="updateUserInfo" ref={form} className="space-y-5" onSubmit={handleSubmit}>
				<div className="grid grid-cols-6">
					<label className="col-start-2 col-span-1 m-auto" htmlFor="email">
						이메일
					</label>
					<input
						className="col-span-3 border-1 rounded-md p-2"
						type="email"
						name="email"
						id="email"
						defaultValue={info.email}
						autoComplete="off"
						readOnly
					/>
				</div>
				<div className="grid grid-cols-6">
					<label className="col-start-2 col-span-1 m-auto" htmlFor="userName">
						이름
					</label>
					<input
						className="col-span-3 border-1 rounded-md p-2"
						defaultValue={info.name}
						type="text"
						name="name"
						id="userName"
						autoComplete="off"
					/>
				</div>
				<div className="grid grid-cols-6">
					<label htmlFor="userBirthDate" className="col-start-2 col-span-1">
						생년월일
					</label>
					<input
						type="date"
						name="birthDate"
						id="userBirthDate"
						style={{ textAlign: "center" }}
						defaultValue={info.birthDate}
						className="col-span-3 border-1 rounded-md px-2"
					/>
				</div>
				<div className="grid grid-cols-6">
					<label className="col-start-2 col-span-1 m-auto" htmlFor="userPhone">
						휴대폰
					</label>
					<div className="col-span-3 flex">
						<input
							type="number"
							name="phone1"
							id="userPhone1"
							value="010"
							readOnly
							style={{ textAlign: "center" }}
							className="appearance-none w-1/3 border-1 rounded-md px-2"
						/>
						<span className="mx-1">-</span>
						<input
							type="number"
							name="phone2"
							id="userPhone2"
							placeholder="1234"
							autoComplete="off"
							defaultValue={info.phone ? info.phone.split("-")[1] : ""}
							style={{ textAlign: "center" }}
							className="appearance-none w-1/3 border-1 rounded-md px-2"
						/>
						<span className="mx-1">-</span>
						<input
							type="number"
							name="phone3"
							id="userPhone3"
							placeholder="5678"
							autoComplete="off"
							defaultValue={info.phone ? info.phone.split("-")[2] : ""}
							style={{ textAlign: "center" }}
							className="appearance-none w-1/3 border-1 rounded-md px-2"
						/>
					</div>
				</div>
				<div className="grid grid-cols-6">
					<label className="col-start-2 col-span-1 m-auto" htmlFor="userGender">
						성별
					</label>
					<select
						defaultValue={info.gender}
						name="gender"
						id="userGender"
						className="col-span-3 border-1 rounded-md p-2"
					>
						<option value="Other">알수없음</option>
						<option value="Male">남성</option>
						<option value="Female">여성</option>
					</select>
				</div>
				<div className="grid grid-cols-6 gap-2">
					<button className="col-start-2 col-span-2 border-1 rounded-md p-2 bg-[#4caf50] text-white">
						수정
					</button>
					<button
						className="col-span-2 border-1 rounded-md p-2 bg-gray-100 text-black"
						onClick={(e) => {
							e.preventDefault()
							onCancle()
						}}
					>
						취소
					</button>
				</div>
			</form>
		</div>
	)
}

export default UpdateInfo
