import { Link } from "react-router-dom"

const NotFound = () => {
	return (
		<div className="flex flex-col items-center text-black font-bold font-sans rtl">
			<img
				src="https://cdn.rawgit.com/ahmedhosna95/upload/1731955f/sad404.svg"
				alt="404"
				className="mb-5 mt-20 h-[342px]"
			/>
			<span className="text-[3.3em] font-extrabold mb-10">404 NOT FOUND</span>
			<p className="text-lg mb-3">페이지를 찾을 수 없습니다.</p>
			<p className="text-sm mb-6"></p>
			<Link
				to="/"
				className="bg-[#fdf3e7] text-black text-2xl font-extrabold py-2 px-10 rounded-full shadow-[0px_20px_70px_4px_rgba(0,0,0,0.1),_inset_7px_33px_0px_#f8d7da] transition-transform duration-300 hover:translate-y-[-13px] hover:shadow-[0_35px_90px_4px_rgba(0,0,0,0.3),_inset_7px_33px_0px_#f8d7da]"
			>
				홈으로 돌아가기
			</Link>
		</div>
	)
}

export default NotFound
