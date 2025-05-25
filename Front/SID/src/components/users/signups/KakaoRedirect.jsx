import { useEffect } from "react"

const KakaoRedirect = () => {
	useEffect(() => {
		const params = new URLSearchParams(window.location.search)
		const code = params.get("code")

		if (code && window.opener) {
			window.opener.postMessage({ code }, import.meta.env.VITE_FRONT_URL)
			window.close()
		}
	})
}

export default KakaoRedirect
