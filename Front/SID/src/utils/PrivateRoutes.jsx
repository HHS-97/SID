import { Navigate, Outlet } from "react-router-dom"
import { useRecoilValue } from "recoil"
import { isLoginSelector } from "../atoms/userAtom"

const PrivateRoutes = () => {
	const isLogin = useRecoilValue(isLoginSelector)
	return isLogin ? <Outlet /> : <Navigate to="/user/login" />
}

export default PrivateRoutes
