import React, { useState, useEffect } from "react";
import {
  TextField,
  Card,
  CardContent,
  CardMedia,
  Typography,
  Tabs,
  Tab,
  Box,
  InputAdornment,
  Avatar,
  Skeleton,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import GenderChart from "./SearchCharts/GenderChart";
import AgeHistogram from "./SearchCharts/AgeHistogram";
import UserActiveTrendChart from "./SearchCharts/UserActiveTrendChart";
import axios from "axios";
import { useQuery } from "@tanstack/react-query";
import { loadDataFromAPI } from "../utils/dataLoader_api";
import { SimilarityFromAPI } from "../utils/similarity";

const Search = () => {
  const [postId, setPostId] = useState("");
  const [selectedPost, setSelectedPost] = useState(null);
  const [activeTab, setActiveTab] = useState(0);

  // 상태: 유사 게시글 중 이미지가 포함된 게시글
  const [similarPostsWithImages, setSimilarPostsWithImages] = useState([]);
  const [similarImagesLoading, setSimilarImagesLoading] = useState(true);

  // React Query로 전체 데이터 로딩 및 캐싱
  const { data, isLoading, error } = useQuery({
    queryKey: ["allData"],
    queryFn: loadDataFromAPI,
    staleTime: 5 * 60 * 1000,
    cacheTime: 10 * 60 * 1000,
  });

  // 데이터가 존재하면 각 데이터를 분리하여 사용
  const userData = data?.userData || [];
  const posts = data?.postData || [];
  const interactionData = data?.interactionData || [];
  const commentData = data?.commentData || [];

  // 선택된 게시글이 있을 경우, 유사 게시글도 React Query를 통해 불러옴
  const { data: similarPosts, isLoading: similarLoading } = useQuery({
    queryKey: ["similarPosts", selectedPost?.id],
    queryFn: () => SimilarityFromAPI(selectedPost.id),
    enabled: !!selectedPost,
  });

  // 선택된 게시글이 변경되면, 유사 게시글 데이터가 업데이트될 때마다 해당 게시글들의 이미지 URL도 불러옵니다.
  // loadImageUrlForPost는 각 게시글에 대해 이미지 URL을 불러오는 비동기 함수입니다.
  const loadImageUrlForPost = async (post) => {
    try {
      const response = await axios.get(
        `${process.env.REACT_APP_CURATING_API_URL}/post/image/${post.id}`
      );
      const imageUrl =
        response.data[0].url
          ? process.env.REACT_APP_IMAGE_URL.replace(
              "uploads",
              response.data[0].url.replace("src/main/resources/", "")
            )
          : null;
      return { ...post, imageUrl };
    } catch (err) {
      console.error("이미지 URL 불러오기 실패:", err);
      return post;
    }
  };

  useEffect(() => {
    if (similarPosts) {
      setSimilarImagesLoading(true);
      // 모든 유사 게시글에 대해 이미지 URL을 불러온 후 업데이트
      Promise.all(similarPosts.map((post) => loadImageUrlForPost(post))).then(
        (updatedPosts) => {
          setSimilarPostsWithImages(updatedPosts);
          setSimilarImagesLoading(false);
        }
      );
    }
  }, [similarPosts]);

  // 검색 핸들러
  const handleSearch = () => {
    if (!postId) return;

    const post = posts.find((p) => parseInt(p.id) === parseInt(postId));
    if (!post) {
      alert("해당 게시글을 찾을 수 없습니다.");
      return;
    }

    const likeCount = interactionData.filter(
      (interaction) =>
        parseInt(interaction.post_id) === parseInt(postId) &&
        interaction.type === "L"
    ).length;

    const unlikeCount = interactionData.filter(
      (interaction) =>
        parseInt(interaction.post_id) === parseInt(postId) &&
        interaction.type === "U"
    ).length;

    const commentCount = commentData.filter(
      (comment) => parseInt(comment.post_id) === parseInt(postId)
    ).length;

    // 작성자 정보 가져오기
    const author = userData.find((user) => user.id === post.profile_id) || {};

    setSelectedPost({
      ...post,
      like_count: likeCount,
      unlike_count: unlikeCount,
      comment_count: commentCount,
      nickname: author.nickname || "Unknown",
      profile_image: author.profile_image || "default_profile.png",
    });
  };

  // 메인 데이터 로딩 및 오류 처리
  if (isLoading) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", p: 3 }}>
        <Skeleton variant="rectangular" width="100%" height={300} />
      </Box>
    );
  }
  if (error) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", p: 3 }}>
        <Typography variant="body1" color="error">
          데이터 로드 중 오류 발생
        </Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ display: "flex" }}>
      {/* 오른쪽 메인 컨텐츠 영역 */}
      <Box
        sx={{
          flex: 1,
          padding: 3,
          gap: 3,
          display: "flex",
          flexDirection: "column",
        }}
      >
        {/* 검색창 영역 */}
        <Box sx={{ width: "100%", maxWidth: 800, margin: "0 auto" }}>
          <TextField
            fullWidth
            variant="outlined"
            placeholder="게시글 번호를 입력하세요"
            value={postId}
            onChange={(e) => setPostId(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleSearch()}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon sx={{ color: "#555" }} />
                </InputAdornment>
              ),
              sx: {
                borderRadius: "30px",
                backgroundColor: "#f7f9fc",
                padding: "5px 10px",
                transition: "all 0.3s ease",
                "&:hover": { backgroundColor: "#eef2f7" },
                "&.Mui-focused": {
                  backgroundColor: "#ffffff",
                  boxShadow: "0 0 8px rgba(0, 123, 255, 0.2)",
                },
              },
            }}
          />
        </Box>

        {/* 본문 컨텐츠 영역 */}
        <Box sx={{ display: "flex", gap: 3, marginTop: 3 }}>
          {/* 왼쪽: 게시글 정보 카드 및 유사 게시글 */}
          <Box sx={{ width: "35%" }}>
            <Card
              sx={{
                height: "fit-content",
                borderRadius: 2,
                boxShadow: "0 4px 12px rgba(0,0,0,0.1)",
                padding: 2,
              }}
            >
              {selectedPost ? (
                <CardContent sx={{ padding: 3 }}>
                  <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
                    <Avatar
                      src={selectedPost.profile_image}
                      alt={selectedPost.nickname}
                    />
                    <Typography variant="subtitle1" fontWeight="bold">
                      {selectedPost.nickname}
                    </Typography>
                  </Box>
                  <Typography variant="caption" color="text.secondary">
                    {new Date(selectedPost.created_at).toLocaleDateString()}
                  </Typography>
                  <Typography
                    variant="body1"
                    paragraph
                    sx={{ mt: 3, mb: 3, lineHeight: 1.6 }}
                  >
                    {selectedPost.content}
                  </Typography>
                  {/* 메인 게시글은 이미지가 로드되도록 처리 */}
                  {selectedPost.imageUrl && (
                    <CardMedia
                      component="img"
                      image={selectedPost.imageUrl}
                      alt="Post Image"
                      sx={{ mt: 2, borderRadius: 2 }}
                    />
                  )}
                  <Typography variant="body2">
                    👍좋아요: {selectedPost.like_count}  👎싫어요:{" "}
                    {selectedPost.unlike_count} 🗨️댓글: {selectedPost.comment_count}
                  </Typography>
                </CardContent>
              ) : (
                <CardContent
                  sx={{
                    padding: 3,
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "center",
                    height: 300,
                  }}
                >
                  <Typography variant="body1" color="text.secondary">
                    게시글을 검색해주세요
                  </Typography>
                </CardContent>
              )}
            </Card>

            {/* 유사 게시글 영역 */}
            {selectedPost && (
              <Box sx={{ mt: 3 }}>
                <Typography variant="h6" sx={{ mb: 2 }}>
                  ⭕ 유사한 게시글
                </Typography>
                {similarImagesLoading ? (
                  <Typography variant="body2">
                    유사한 게시글 불러오는 중..
                  </Typography>
                ) : (
                  <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
                    {similarPostsWithImages
                      .filter((post) => post.id !== selectedPost.id)
                      .slice(0, 5)
                      .map((post) => {
                        // 작성자 정보 가져오기
                        const author = userData.find(
                          (user) => user.id === post.profile_id
                        );
                        const nickname = author ? author.nickname : "Unknown";
                        const profileImage = author
                          ? author.profile_image
                          : "default_profile.png";
                        return (
                          <Card
                            key={post.id}
                            sx={{
                              height: "auto",
                              padding: 2,
                              borderRadius: 2,
                              boxShadow: "0 2px 6px rgba(0,0,0,0.1)",
                            }}
                          >
                            <CardContent>
                              <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
                                <Avatar src={profileImage} alt={nickname} />
                                <Typography variant="subtitle1" fontWeight="bold">
                                  {nickname}
                                </Typography>
                              </Box>
                              <Typography variant="caption" color="text.secondary">
                                {new Date(post.created_at).toLocaleDateString()}
                              </Typography>
                              <Typography variant="body2" sx={{ mt: 1 }}>
                                {post.content.length > 100
                                  ? post.content.substring(0, 100) + "..."
                                  : post.content}
                              </Typography>
                              {post.imageUrl && (
                                <CardMedia
                                  component="img"
                                  image={post.imageUrl}
                                  alt="Post Image"
                                  sx={{ mt: 2, borderRadius: 2 }}
                                />
                              )}
                            </CardContent>
                          </Card>
                        );
                      })}
                  </Box>
                )}
              </Box>
            )}
          </Box>

          {/* 오른쪽: 분석 차트 영역 */}
          <Card
            sx={{
              width: "65%",
              height: 600,
              padding: 2,
              borderRadius: 2,
              boxShadow: "0 4px 12px rgba(0,0,0,0.1)",
            }}
          >
            <CardContent>
              <Tabs
                value={activeTab}
                onChange={(e, newValue) => setActiveTab(newValue)}
                variant="fullWidth"
                sx={{ mb: 3 }}
              >
                <Tab label="Gender Chart" />
                <Tab label="Age Histogram" />
                <Tab label="User View" />
              </Tabs>
              <Box sx={{ minHeight: 400 }}>
                {activeTab === 0 && (
                  <GenderChart
                    postId={selectedPost?.id}
                    interactionData={interactionData}
                    userData={userData}
                  />
                )}
                {activeTab === 1 && (
                  <AgeHistogram
                    postId={selectedPost?.id}
                    interactionData={interactionData}
                    userData={userData}
                  />
                )}
                {activeTab === 2 && (
                  <UserActiveTrendChart
                    postId={selectedPost?.id}
                    interactionData={interactionData}
                  />
                )}
              </Box>
            </CardContent>
          </Card>
        </Box>
      </Box>
    </Box>
  );
};

export default Search;
