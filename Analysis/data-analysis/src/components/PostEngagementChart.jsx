import React from "react";
import { Bar } from "react-chartjs-2";
import "../chartConfig";

const PostEngagementChart = ({ posts = [] }) => { 
  if (!Array.isArray(posts)) {
    console.error("📌 PostEngagementChart: posts가 배열이 아님", posts);
    return <div>📌 게시글 데이터 로드 중 오류 발생</div>;
  }

  console.log("📌 PostEngagementChart에서 받은 posts 데이터:", posts); // ✅ posts 데이터 확인

  const postLikes = posts.map((post) => post.like_count || 0);
  const postDislikes = posts.map((post) => post.unlike_count || 0);
  const postComments = posts.map((post) => post.comment_count || 0);

  const data = {
    labels: ["Likes", "Dislikes", "Comments"],
    datasets: [
      {
        label: "Engagement",
        data: [
          postLikes.reduce((a, b) => a + b, 0),
          postDislikes.reduce((a, b) => a + b, 0), 
          postComments.reduce((a, b) => a + b, 0), 
        ],
        backgroundColor: ["#36A2EB", "#FF6384", "#FFCE56"],
      },
    ],
  };

  return (
    <div>
      <h2>Post Engagement</h2>
      <Bar data={data} />
    </div>
  );
};


export default PostEngagementChart;
