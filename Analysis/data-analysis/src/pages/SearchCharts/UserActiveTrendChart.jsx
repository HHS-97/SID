import React, { useState, useEffect, useCallback } from "react";
import { Box, Typography } from "@mui/material";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from "recharts";

const UserActivityTrendChart = ({ postId, interactionData = [] }) => {
  const [chartData, setChartData] = useState([]);

  const prepareLast7DaysData = useCallback((filteredInteractions) => {
    if (!filteredInteractions || !Array.isArray(filteredInteractions)) return;

    const today = new Date();
    const data = [];

    for (let i = 6; i >= 0; i--) {
      const date = new Date(today);
      date.setDate(today.getDate() - i);
      const dateStr = date.toISOString().split("T")[0];

      const dayInteractions = filteredInteractions.filter((item) => {
        const itemDate = new Date(item.created_at);
        return itemDate.toISOString().split("T")[0] === dateStr;
      });

      const typeCounts = {
        D: new Set(dayInteractions.filter((i) => i.type === "D").map((i) => i.user_id)).size,
        R: new Set(dayInteractions.filter((i) => i.type === "R").map((i) => i.user_id)).size,
        U: new Set(dayInteractions.filter((i) => i.type === "U").map((i) => i.user_id)).size,
        L: new Set(dayInteractions.filter((i) => i.type === "L").map((i) => i.user_id)).size,
      };

      data.push({
        date: `${date.getMonth() + 1}/${date.getDate()}`,
        detailView: typeCounts.D,
        view: typeCounts.R,
        dislike: typeCounts.U,
        like: typeCounts.L,
      });
    }

    setChartData(data);
  }, []);

  useEffect(() => {
    if (!postId || !interactionData.length) {
      setChartData([]);
      return;
    }

    const postInteractions = interactionData.filter(
      (interaction) => parseInt(interaction.post_id) === parseInt(postId)
    );

    prepareLast7DaysData(postInteractions);
  }, [postId, interactionData, prepareLast7DaysData]); 

  if (!postId) return <Typography>게시글을 선택해주세요</Typography>;

  return (
    <Box sx={{ width: "100%", height: 400 }}>
      <Box sx={{ display: "flex", justifyContent: "center", alignItems: "center", mb: 2 }}>
      <h3 className="text-lg font-semibold text-center mb-4">최근 7일 사용자 활동 추이</h3>
      </Box>

      <ResponsiveContainer width="100%" height="85%">
        <LineChart data={chartData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="date" />
          <YAxis />
          <Tooltip />
          <Legend />
          <Line type="monotone" dataKey="detailView" name="자세히 보기" stroke="#ff7300" strokeWidth={2} dot={{ r: 4 }} />
          <Line type="monotone" dataKey="view" name="보기" stroke="#ffc658" strokeWidth={2} dot={{ r: 4 }} />
          <Line type="monotone" dataKey="dislike" name="싫어요" stroke="#82ca9d" strokeWidth={2} dot={{ r: 4 }} />
          <Line type="monotone" dataKey="like" name="좋아요" stroke="#8884d8" strokeWidth={2} dot={{ r: 4 }} />
        </LineChart>
      </ResponsiveContainer>
    </Box>
  );
};

export default UserActivityTrendChart;
