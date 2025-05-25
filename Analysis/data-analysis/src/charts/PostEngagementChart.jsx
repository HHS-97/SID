import React, { useState } from 'react';
import { Bar } from 'react-chartjs-2';
import { Chart, CategoryScale, LinearScale, BarElement, Tooltip, Legend } from 'chart.js';
import './PostEngagementChart.css';

Chart.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

const PostEngagementChart = ({ posts, comments, interactions }) => {
  const [page, setPage] = useState(0);
  const postsPerPage = 10;
  const totalPages = Math.ceil(posts.length / postsPerPage);

  const paginatedPosts = posts.slice(page * postsPerPage, (page + 1) * postsPerPage);

  const commentCountMap = {};
  comments.forEach(comment => {
    const postId = comment.post_id;
    if (!commentCountMap[postId]) {
      commentCountMap[postId] = 0;
    }
    commentCountMap[postId] += 1;
  });

  const likeCountMap = {};
  const unlikeCountMap = {};
  interactions.forEach(interaction => {
    const postId = interaction.post_id;
    if (interaction.type === 'L') {
      if (!likeCountMap[postId]) {
        likeCountMap[postId] = 0;
      }
      likeCountMap[postId] += 1;
    } else if (interaction.type === 'U') {
      if (!unlikeCountMap[postId]) {
        unlikeCountMap[postId] = 0;
      }
      unlikeCountMap[postId] += 1;
    }
  });

  const labels = paginatedPosts.map(post => `Post ${post.id}`);
  const likeCounts = paginatedPosts.map(post => likeCountMap[post.id] || 0);
  const unlikeCounts = paginatedPosts.map(post => unlikeCountMap[post.id] || 0);
  const commentCounts = paginatedPosts.map(post => commentCountMap[post.id] || 0);

  const chartData = {
    labels,
    datasets: [
      {
        label: '좋아요',
        data: likeCounts,
        backgroundColor: '#44a4ec',
      },
      {
        label: '싫어요',
        data: unlikeCounts,
        backgroundColor: '#ffa2a2',
      },
      {
        label: '댓글',
        data: commentCounts,
        backgroundColor: '#ffcf99',
      }
    ]
  };

  const chartOptions = {
    responsive: true,
    plugins: {
      legend: { position: "top" },
    },
    scales: {
      x: { stacked: true },
      y: { stacked: true },
    }
  };

  return (
    <div className="chart-container">
      <h3>게시글 참여도</h3>
      <Bar data={chartData} options={chartOptions} />
      <div className="pagination-controls">
        <button 
          className="pagination-button" 
          onClick={() => setPage(prev => Math.max(prev - 1, 0))} 
          disabled={page === 0}>
          &lt;
        </button>
        <span className="pagination-text">{page + 1} / {totalPages}</span>
        <button 
          className="pagination-button" 
          onClick={() => setPage(prev => Math.min(prev + 1, totalPages - 1))} 
          disabled={page === totalPages - 1}>
          &gt;
        </button>
      </div>
    </div>
  );
};

export default PostEngagementChart;
