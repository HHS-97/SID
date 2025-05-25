import { useMemo, useState } from "react"
import PostList from "../../components/feeds/PostList"

const ProfileFeed = () => {
	const [viewParam, setViewParam] = useState("Random")
	const view = useMemo(() => {
		switch (viewParam) {
			case "Random":
				return (
					<PostList
						url="/posts/briefly"
						key="/posts/briefly/profile"
						params={{ type: "2" }}
					/>
				)
			case "Follow":
				return <PostList url="/posts/follow" key="/posts/follow" create={false} />
			default:
				return <PostList url="/posts/briefly" key="/posts/briefly" params={{ type: "2" }} />
		}
	}, [viewParam])

	return (
		<div>
			<div className="flex justify-evenly mb-5">
				<button
					onClick={() => {
						setViewParam("Random")
					}}
					className={viewParam === "Random" ? "border-b-3 w-30" : "w-30"}
				>
					둘러보기
				</button>
				<button
					onClick={() => {
						setViewParam("Follow")
					}}
					className={viewParam === "Follow" ? "border-b-3 w-30" : "w-30"}
				>
					팔로우
				</button>
			</div>
			{view}
		</div>
	)
}

export default ProfileFeed
