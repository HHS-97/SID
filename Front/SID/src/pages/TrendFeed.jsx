import { useMemo, useState } from "react"
import PostList from "../components/feeds/PostList"

const TrendFeed = () => {
	const [viewParam, setViewParam] = useState("Trend")
	const view = useMemo(() => {
		switch (viewParam) {
			case "Trend":
				return <PostList url="/posts/trend" key="/posts/trend" />
			case "Recent":
				return (
					<PostList
						url="/posts/briefly"
						key="/posts/briefly/recent"
						params={{ type: "1" }}
					/>
				)
			default:
				return (
					<PostList
						url="/posts/briefly"
						key="/posts/briefly/random"
						params={{ type: "2" }}
					/>
				)
		}
	}, [viewParam])

	return (
		<div>
			<div className="flex justify-evenly mb-5">
				<button
					onClick={() => {
						setViewParam("Trend")
					}}
					className={viewParam === "Trend" ? "border-b-3 w-30" : "w-30"}
				>
					지금 뜨는 피드
				</button>
				<button
					onClick={() => {
						setViewParam("Recent")
					}}
					className={viewParam === "Recent" ? "border-b-3 w-30" : "w-30"}
				>
					최신 피드
				</button>
			</div>
			{view}
		</div>
	)
}

export default TrendFeed
