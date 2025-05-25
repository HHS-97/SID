import axios from "axios"
import { motion } from "framer-motion"
import qs from "qs"
import { useEffect, useRef, useState } from "react"
import { useInView } from "react-intersection-observer"
import { BarLoader } from "react-spinners"
import { useRecoilValue } from "recoil"
import Swal from "sweetalert2"
import withReactContent from "sweetalert2-react-content"
import { isLoginSelector, userAtom } from "../../atoms/userAtom"
import CreatePost from "./CreatePost"
import PostItem from "./PostItem"
import ProfileItem from "./ProfileItem"

const PostList = ({ url, params, search, create }) => {
	const mySwal = withReactContent(Swal)
	const canSearch = search === undefined || search === true ? true : false
	const canCreate = create === undefined || create === true ? true : false

	const [posts, setPosts] = useState([])
	const [postIds, setPostIds] = useState([])
	const user = useRecoilValue(userAtom)
	const isLogin = useRecoilValue(isLoginSelector)

	const [page, setPage] = useState(0)
	const [isLoading, setIsLoading] = useState(false)
	const [canRequest, setCanRequest] = useState(true)
	const [observer, inView] = useInView({
		threshold: 1,
		delay: 1000,
	})

	const getPosts = (page = 0) => {
		if (canRequest === false) {
			setIsLoading(false)
		}
		axios
			.get(import.meta.env.VITE_BASE_URL + url, {
				headers: { "Content-Type": "application/json" },
				params: { ...params, page, postIds },
				// 배열을 repeat 형식으로 직렬화: postIds=1&postIds=2&postIds=3
				paramsSerializer: (params) => qs.stringify(params, { arrayFormat: "repeat" }),
				withCredentials: true,
			})
			.then((res) => {
				//console.log("서버에서 받은 데이터:", res)
				if (res.data.length === 0) {
					setCanRequest(false)
					setIsLoading(false)
				} else {
					setPosts([...posts, ...res.data])
					setPostIds([...postIds, ...res.data.map((post) => post.postId)])
					setIsLoading(false)
				}
			})
			.catch((err) => {
				//console.log(err)
				switch (err.status) {
					case 401:
						alert("로그인 만료")
						localStorage.removeItem("user")
						window.location.href = "/user/login"
						break
					case 403:
						if (url !== "/profiles/posts") {
							Swal.fire({
								title: "피드 불러오기 실패",
								icon: "error",
								showConfirmButton: true,
							})
						}
						break
					default:
						Swal.fire({
							title: "피드 불러오기 실패",
							icon: "error",
							showConfirmButton: true,
						})
						break
				}
			})
	}

	const onCreatePost = (form, img) => {
		Swal.fire({
			title: "Loading",
			allowOutsideClick: false,
			didOpen: () => {
				Swal.showLoading()
			},
		})
		axios
			.post(import.meta.env.VITE_BASE_URL + "/posts", form, {
				headers: { "Content-Type": "multipart/form-data" },
				withCredentials: true,
			})
			.then((res) => {
				Swal.close()
				setPosts([
					{
						postId: res.data.postId,
						writer: user.lastProfile,
						content: form.get("content"),
						image: img && img.imageFile ? "new" + img.previewUrl : "",
						createdAt: new Date(),
						time: "방금",
						likeCount: 0,
						dislikeCount: 0,
						commentCount: 0,
						reaction: null,
					},
					...posts,
				])
				//console.log(res)
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
							title: "피드 작성 실패",
							icon: "error",
							showConfirmButton: true,
						})
						break
				}
			})
	}

	const onUpdatePost = (form, img) => {
		axios
			.patch(import.meta.env.VITE_BASE_URL + "/posts", form, {
				withCredentials: true,
			})
			.then(async (res) => {
				//console.log(res)
				setPosts(
					posts.map((post) => {
						if (String(post.postId) === String(form.get("postId"))) {
							return {
								...post,
								content: form.get("content"),
								image: img ? "new" + img.previewUrl : null,
							}
						}
						return post
					}),
				)
				Swal.close()
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
							title: "피드 수정 실패",
							icon: "error",
							showConfirmButton: true,
						})
						break
				}
			})
	}

	const onDeletePost = (postId) => {
		Swal.fire({
			title: "피드 삭제",
			text: "정말로 삭제하시겠습니까?",
			icon: "warning",
			showCancelButton: true,
			confirmButtonText: "삭제",
			cancelButtonText: "취소",
		}).then((result) => {
			if (result.isConfirmed) {
				axios
					.delete(import.meta.env.VITE_BASE_URL + "/posts", {
						headers: { "Content-Type": "application/json" },
						data: { postId },
						withCredentials: true,
					})
					.then((res) => {
						//console.log(res)
						setPosts(posts.filter((post) => String(post.postId) !== String(postId)))
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
									title: "피드 삭제 실패",
									icon: "error",
									showConfirmButton: true,
								})
								break
						}
					})
			}
		})
	}

	const viewCreatePost = () => {
		mySwal.fire({
			html: <CreatePost user={user} onCreatePost={onCreatePost} />,
			showCloseButton: true,
			showConfirmButton: false,
		})
	}

	const getRenewPost = (postId) => {
		return axios
			.get(import.meta.env.VITE_BASE_URL + "/posts/one", {
				headers: { "Content-Type": "application/json" },
				params: { postId },
				withCredentials: true,
			})
			.then((res) => {
				//console.log("새로운 글:", res.data)
				return res.data.post
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
							title: "피드 갱신 실패",
							icon: "error",
							showConfirmButton: true,
						})
						break
				}
			})
	}

	const onLikePost = (postId, btn, state) => {
		//console.log(postId, btn, state)
		switch (btn) {
			case "like":
				switch (state) {
					case null:
					case "false":
						// 좋아요 요청
						axios
							.post(
								`${import.meta.env.VITE_BASE_URL}/${btn}/posts`,
								{ postId, isLike: false },
								{
									headers: { "Content-Type": "application/json" },
									withCredentials: true,
								},
							)
							.then((res) => {
								//console.log(res)
								getRenewPost(postId).then((renewPost) => {
									setPosts(
										posts.map((post) => {
											if (String(post.postId) === String(postId)) {
												return renewPost
											}
											return post
										}),
									)
								})
							})
							.catch((err) => {
								//console.log(err)
								switch (err.status) {
									case 401:
										alert("로그인 만료")
										localStorage.removeItem("user")
										window.location.href = "/user/login"
										break
									case 400:
										Swal.fire({
											icon: "error",
											title: "좋아요 실패",
											text: "잘못된 요청입니다.",
											timer: 1000,
											showConfirmButton: false,
										})
										break

									default:
										Swal.fire({
											title: "좋아요 실패",
											text: "서버 오류입니다.",
											icon: "error",
											showConfirmButton: true,
										})
										break
								}
							})
						break
					case "true":
						// 좋아요 취소 요청
						axios
							.delete(`${import.meta.env.VITE_BASE_URL}/${btn}/posts`, {
								headers: { "Content-Type": "application/json" },
								withCredentials: true,
								data: { postId, isLike: true },
							})
							.then((res) => {
								//console.log(res)
								getRenewPost(postId).then((renewPost) => {
									setPosts(
										posts.map((post) => {
											if (String(post.postId) === String(postId)) {
												return renewPost
											}
											return post
										}),
									)
								})
							})
							.catch((err) => {
								//console.log(err)
								switch (err.status) {
									case 401:
										alert("로그인 만료")
										localStorage.removeItem("user")
										window.location.href = "/user/login"
										break
									case 400:
										Swal.fire({
											icon: "error",
											title: "좋아요 취소 실패",
											text: "잘못된 요청입니다.",
											timer: 1000,
											showConfirmButton: false,
										})
										break

									default:
										Swal.fire({
											title: "좋아요 취소 실패",
											text: "서버 오류입니다.",
											icon: "error",
											showConfirmButton: true,
										})
										break
								}
							})
						break
				}
				break
			case "dislike":
				switch (state) {
					case null:
					case "true":
						// 싫어요 요청
						axios
							.post(
								`${import.meta.env.VITE_BASE_URL}/${btn}/posts`,
								{ postId, isDislike: false },
								{
									headers: { "Content-Type": "application/json" },
									withCredentials: true,
								},
							)
							.then((res) => {
								//console.log(res)
								getRenewPost(postId).then((renewPost) => {
									setPosts(
										posts.map((post) => {
											if (String(post.postId) === String(postId)) {
												return renewPost
											}
											return post
										}),
									)
								})
							})
							.catch((err) => {
								//console.log(err)
								switch (err.status) {
									case 401:
										alert("로그인 만료")
										localStorage.removeItem("user")
										window.location.href = "/user/login"
										break
									case 400:
										Swal.fire({
											icon: "error",
											title: "싫어요 실패",
											text: "잘못된 요청입니다.",
											timer: 1000,
											showConfirmButton: false,
										})
										break

									default:
										Swal.fire({
											title: "싫어요 실패",
											text: "서버 오류입니다.",
											icon: "error",
											showConfirmButton: true,
										})
										break
								}
							})
						break
					case "false":
						// 싫어요 취소 요청
						axios
							.delete(`${import.meta.env.VITE_BASE_URL}/${btn}/posts`, {
								headers: { "Content-Type": "application/json" },
								withCredentials: true,
								data: { postId, isDislike: true },
							})
							.then((res) => {
								//console.log(res)
								getRenewPost(postId).then((renewPost) => {
									setPosts(
										posts.map((post) => {
											if (String(post.postId) === String(postId)) {
												return renewPost
											}
											return post
										}),
									)
								})
							})
							.catch((err) => {
								//console.log(err)
								switch (err.status) {
									case 401:
										alert("로그인 만료")
										localStorage.removeItem("user")
										window.location.href = "/user/login"
										break
									case 400:
										Swal.fire({
											icon: "error",
											title: "싫어요 취소 실패",
											text: "잘못된 요청입니다.",
											timer: 1000,
											showConfirmButton: false,
										})
										break

									default:
										Swal.fire({
											title: "싫어요 취소 실패",
											text: "서버 오류입니다.",
											icon: "error",
											showConfirmButton: true,
										})
										break
								}
							})
						break
				}
				break
			default:
				break
		}
	}

	useEffect(() => {
		if (inView) {
			setIsLoading(true)
			getPosts(page)
			setPage(page + 1)
		}
		//console.log("use effect 실행")
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [params, inView])

	// 검색 관련 로직
	const [isSearching, setIsSearching] = useState(false)
	const query = useRef("")
	const [searchType, setSearchType] = useState("ALL")
	const [searchQuery, setSearchQuery] = useState("")
	const [searchPosts, setSearchPosts] = useState([])
	const [searchProfiles, setSearchProfiles] = useState([])
	const [searchPage, setSearchPage] = useState(0)
	const [isSearchLoading, setIsSearchLoading] = useState(false)
	const [canSearchRequest, setCanSearchRequest] = useState(true)
	const [searchViewMode, setSearchViewMode] = useState("FEED")
	const [searchObserver, searchInView] = useInView({
		threshold: 0,
		delay: 1000,
	})

	const onSearch = () => {
		setSearchPosts([])
		setSearchProfiles([])
		setSearchPage(0)
		setIsSearchLoading(true)
		setCanSearchRequest(true)
		switch (query.current.value[0]) {
			case "@":
				setSearchType("PROFILE")
				setSearchViewMode("PROFILE")
				setSearchQuery(query.current.value.slice(1))
				break
			case "#":
				setSearchType("FEED")
				setSearchViewMode("FEED")
				setSearchQuery(query.current.value.slice(1))
				break
			default:
				setSearchType("ALL")
				setSearchViewMode("FEED")
				setSearchQuery(query.current.value)
				break
		}
	}

	const getSearchResults = (p) => {
		if (!isLogin) {
			Swal.fire({
				title: "로그인이 필요합니다",
				icon: "error",
				showConfirmButton: true,
			}).then(() => {
				window.location.href = "/user/login"
			})
		}
		if (searchQuery && searchType) {
			axios
				.get(import.meta.env.VITE_BASE_URL + "/posts/search", {
					headers: {
						"Content-Type": "application/json",
					},
					params: {
						keyword: searchQuery,
						type: searchType,
						page: p,
					},
					withCredentials: true,
				})
				.then((res) => {
					//console.log(searchQuery, searchType)
					//console.log(res)
					if (
						searchQuery !== "PROFILE" &&
						res.data.posts.length === 0 &&
						res.data.profilePosts.length === 0
					) {
						setCanSearchRequest(false)
					} else if (searchQuery === "PROFILE" && res.data.profiles.length === 0) {
						setCanSearchRequest(false)
					} else {
						res.data.posts.forEach((post) => {
							if (searchPosts.includes(post)) return
							setSearchPosts((prev) => [...prev, post])
						})
						res.data.profilePosts.forEach((post) => {
							if (searchPosts.includes(post)) return
							setSearchPosts((prev) => [...prev, post])
						})
						res.data.profiles.forEach((profile) => {
							if (searchProfiles.includes(profile)) return
							setSearchProfiles((prev) => [...prev, profile])
						})
					}
				})
				.catch((error) => {
					console.error(error)
					switch (error.status) {
						case 401:
							alert("로그인 만료")
							localStorage.removeItem("user")
							window.location.href = "/user/login"
							break
						default:
							Swal.fire({
								title: "검색 실패",
								text: "서버 오류입니다.",
								icon: "error",
								showConfirmButton: true,
							})
							break
					}
				})
		}
		setIsSearchLoading(false)
	}

	const focusSearch = () => {
		setIsSearching(true)
	}

	const cancelSearch = () => {
		query.current.value = ""
		setSearchQuery("")
		setSearchType("ALL")
		setSearchPosts([])
		setSearchProfiles([])
		setPage(0)
		setCanRequest(true)
		setIsSearching(false)
	}

	const handleLoadMoreClick = () => {
		if (searchQuery && canSearchRequest) {
			setIsSearchLoading(true)
			getSearchResults(searchPage)
			setSearchPage(searchPage + 1)
		}
	}

	useEffect(() => {
		if (canSearchRequest && searchInView) {
			//console.log("검색 인피니티 스크롤")
			setIsSearchLoading(true)
			getSearchResults(searchPage)
			setSearchPage(searchPage + 1)
		}
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [searchQuery, searchInView, canSearchRequest])

	return (
		<div className="space-y-3">
			{/* 검색창 */}
			{!isLoading && canSearch && (
				<div className="flex items-center bg-[#fddde1] p-3 rounded-full shadow-lg max-w-full">
					<input
						id="searchQuery"
						ref={query}
						type="text"
						placeholder="검색어를 입력하세요..."
						onFocus={focusSearch}
						autoComplete="off"
						className="w-80 px-4 py-2 rounded-full text-gray-800 focus:outline-none bg-[#fddde1] placeholder:text-[#a18d94]"
						onKeyDown={(e) => {
							if (e.key === "Enter") {
								onSearch() // 엔터 키가 눌렸을 때 onSearch 실행
							}
						}}
					/>
					<button
						onClick={onSearch}
						className="bg-[#f6b5b1] hover:bg-[#f1908c] p-2 ml-auto rounded-full cursor-pointer transition duration-300"
					>
						🔍
					</button>

					{isSearching && (
						<button
							onClick={cancelSearch}
							className="bg-[#f6b5b1] hover:bg-[#f1908c] p-2 ml-2 rounded-full cursor-pointer transition duration-300"
						>
							❌
						</button>
					)}
				</div>
			)}
			{/* 검색 결과 */}
			{canSearch && isSearching && (
				<>
					{!searchQuery && (
						<div className="flex flex-col items-center justify-center h-[50vh]">
							<p className="text-gray-400 text-lg">검색어를 입력하세요...</p>
							<p className="text-gray-400 text-lg">@ 유저 검색 / # 피드 검색</p>
						</div>
					)}
					{/* 검색 결과 */}
					{searchQuery && (
						<div className="flex justify-evenly mb-5">
							<button
								onClick={() => {
									setSearchViewMode("FEED")
								}}
								className={searchViewMode === "FEED" ? "border-b-3 w-30" : "w-30"}
							>
								피드 검색 결과
							</button>
							<button
								onClick={() => {
									setSearchViewMode("PROFILE")
								}}
								className={
									searchViewMode === "PROFILE" ? "border-b-3 w-30" : "w-30"
								}
							>
								유저 검색 결과
							</button>
						</div>
					)}
					{/* 프로필 검색 */}
					{searchQuery && searchViewMode === "PROFILE" && searchProfiles.length > 0 && (
						<>
							{searchProfiles.map((profile, idx) => (
								<ProfileItem key={idx} profile={profile} />
							))}
							{!canSearchRequest && (
								<p className="text-center">마지막 프로필입니다.</p>
							)}
						</>
					)}
					{searchQuery &&
						!isSearchLoading &&
						searchViewMode === "PROFILE" &&
						searchProfiles.length === 0 && (
							<p className="text-center">검색 결과가 없습니다.</p>
						)}
					{/* 피드 검색 */}
					{searchQuery && searchViewMode === "FEED" && searchPosts.length > 0 && (
						<>
							{searchPosts.map((post) => (
								<PostItem
									key={`searchpost-${post.postId}`}
									post={post}
									user={user}
									isLogin={isLogin}
									onUpdatePost={onUpdatePost}
									onDeletePost={onDeletePost}
									onLikePost={onLikePost}
								/>
							))}
							{!canSearchRequest && (
								<p className="text-center">마지막 게시글입니다.</p>
							)}
						</>
					)}
					{searchQuery &&
						!isSearchLoading &&
						searchViewMode === "FEED" &&
						searchPosts.length === 0 && (
							<p className="text-center">검색 결과가 없습니다.</p>
						)}
					<div
						id="searchObserver"
						className="h-5 flex justify-center"
						onClick={handleLoadMoreClick}
						disabled={isSearchLoading || !canSearchRequest}
						ref={searchObserver}
					>
						{searchQuery && isSearchLoading && <BarLoader color="#f6339a" />}
						{searchQuery && canSearchRequest && <p>v 더보기</p>}
					</div>
				</>
			)}

			{/* 글 작성 버튼 */}
			{isLogin && canCreate && (
				<button
					onClick={viewCreatePost}
					className="absolute lg:right-6 bg-gradient-to-r from-pink-300 to-pink-400 text-white font-bold text-lg shadow-md hover:from-pink-400 hover:to-pink-500 transition duration-300 w-14 h-14 flex items-center justify-center rounded-full shadow-lg z-[500]"
					style={{ bottom: "5rem", right: "1rem" }}
				>
					<motion.div
						className="w-8 h-8 flex items-center justify-center"
						whileTap={{
							scale: [1, 1.3, 1], // 클릭 시 1 -> 1.3 -> 1로 변경
							filter: ["brightness(1)", "brightness(1.1)", "brightness(1)"],
						}}
						transition={{
							duration: 0.6,
							ease: "easeOut",
						}}
					>
						<img
							src="/buttonIcon/createPostIcon.png"
							alt="Create Post Icon"
							className="w-full h-full object-contain"
						/>
					</motion.div>
				</button>
			)}
			{/* 게시글 리스트 */}
			{!isSearching &&
				posts.map((post, idx) => {
					return (
						<PostItem
							key={`${url}-post-${post.postId}-${idx}`}
							post={post}
							user={user}
							isLogin={isLogin}
							onUpdatePost={onUpdatePost}
							onDeletePost={onDeletePost}
							onLikePost={onLikePost}
						/>
					)
				})}
			{/* 무한스크롤 로딩바 */}
			{!isSearching && (
				<div id="observer" className="h-3 flex justify-center" ref={observer}>
					{isLoading && <BarLoader color="#f6339a" />}
					{!canRequest && posts.length > 0 && url !== "/posts/follow" && (
						<p className="text-center">마지막 게시글입니다.</p>
					)}
					{!canRequest && posts.length > 0 && url === "/posts/follow" && (
						<p className="text-center">팔로우하는 유저의 게시글이 없습니다.</p>
					)}
				</div>
			)}
			{/* 더보기 수동 */}
			{!isSearching && !isLoading && canRequest && (
				<button
					onClick={() => {
						setIsLoading(true)
						getPosts(page)
						setPage(page + 1)
					}}
				>
					더보기
				</button>
			)}
			{/* 게시글이 없을 때 */}
			{!isSearching && posts.length === 0 && (
				<div className="flex flex-col items-center justify-center h-[50vh]">
					<p className="text-gray-400 text-lg">게시글이 없습니다.</p>
				</div>
			)}
		</div>
	)
}

export default PostList
