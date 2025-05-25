import { Navigate, Outlet } from "react-router-dom"
import { useRecoilValue } from "recoil"
import { isLoginSelector } from "../atoms/userAtom"

const PublicRoutes = () => {
	const isLogin = useRecoilValue(isLoginSelector)
	return <div>{isLogin ? <Navigate to="/" /> : <Outlet />}</div>
}

export default PublicRoutes
