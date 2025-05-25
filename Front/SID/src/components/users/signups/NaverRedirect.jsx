import { useEffect } from "react"

const NaverRedirect = () => {
	useEffect(() => {
		const params = new URLSearchParams(window.location.search)
		const code = params.get("code")
		const state = params.get("state")

		if (state && code && window.opener) {
			window.opener.postMessage({ code, state }, import.meta.env.VITE_FRONT_URL)
			window.close()
		}
	})
}

export default NaverRedirect
