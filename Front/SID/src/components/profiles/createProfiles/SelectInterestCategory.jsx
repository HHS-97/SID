import axios from "axios"
import { useEffect, useState } from "react"
import Swal from "sweetalert2"
import CategoryItem from "./CategoryItem"

const SelectInterestCategory = ({ onCompleteSelect }) => {
	const [categories, setCategories] = useState([])
	const [checkedCategories, setCheckedCategories] = useState([])

	const checkCategoryHandler = (id, isChecked) => {
		if (isChecked) {
			// 체크된 상태이면 id를 배열에 추가
			setCheckedCategories((prev) => [...prev, id])
		} else {
			// 체크 해제된 상태이면 id를 배열에서 제거
			setCheckedCategories((prev) => prev.filter((item) => item !== id))
		}
	}

	const completeSelect = (e) => {
		e.preventDefault()
		onCompleteSelect(checkedCategories)
	}

	useEffect(() => {
		// 카테고리 목록을 받아옴
		axios
			.get(import.meta.env.VITE_BASE_URL + "/category", { withCredentials: true })
			.then((res) => {
				//console.log(res.data)
				setCategories(res.data)
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
							title: "카테고리 로딩 실패",
							icon: "error",
							showConfirmButton: true,
						})
						break
				}
			})
	}, [])

	return (
		<div>
			<form onSubmit={completeSelect}>
				{categories.map((category) => (
					<CategoryItem
						key={category.id}
						id={category.id} // id 전달
						name={category.tag}
						checkCategoryHandler={checkCategoryHandler}
						checked={checkedCategories.includes(category.id)} // id로 체크
					/>
				))}
				<button className="w-full mt-5 p-2 border border-pink-400 hover:bg-red-200 rounded-md bg-red-100 font-bold text-xl text-rose-500 transition-all duration-300 ease-in-out  transform hover:scale-105 active:scale-95">
					시작하기
				</button>
			</form>
		</div>
	)
}

export default SelectInterestCategory
