import axios from "axios"
import { useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { useResetRecoilState } from "recoil"
import { userAtom } from "../atoms/userAtom"

const Reset = () => {
	const navigate = useNavigate()
	const resetUser = useResetRecoilState(userAtom)
	useEffect(() => {
		axios
			.delete(import.meta.env.VITE_BASE_URL + "/cookie", { withCredentials: true })
			.then((res) => {
				//console.log(res)
				resetUser()
				navigate("/user/login")
			})
			.catch((err) => {
				//console.log(err)
			})
	}, [resetUser, navigate])
}

export default Reset
