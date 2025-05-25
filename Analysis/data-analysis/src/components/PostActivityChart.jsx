import React, { useEffect, useState } from "react";
import { Line } from "react-chartjs-2";
import { Chart, CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, plugins } from "chart.js";

Chart.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend);

const PostActivityChart = ({ interactions = [] }) => {
  const [activityData, setActivityData] = useState([]);

  useEffect(() => {
    if (!Array.isArray(interactions) || interactions.length === 0) {
      // console.warn("PostActivityChart: interactions 데이터가 없습니다.");
      setActivityData([]);
      return;
    }

    const activityCount = {};

    interactions.forEach((interaction) => {
      if (!interaction || !interaction.created_at) return;

      // `created_at` 값이 Unix Timestamp라면, `new Date(Number(created_at))` 사용
      const date = new Date(Number(interaction.created_at));
      const hour = date.getHours(); // 시간 추출

      activityCount[hour] = (activityCount[hour] || 0) + 1;
    });

    const sortedData = Array.from({ length: 24 }, (_, i) => ({
      hour: `${i}:00`,
      count: activityCount[i] || 0,
    }));

    setActivityData(sortedData);
  }, [interactions]);

  if (activityData.length === 0) {
    return <div>게시글 활동량 데이터가 없습니다.</div>;
  }

  const chartData = {
    labels: activityData.map((d) => d.hour),
    datasets: [
      {
        label: "시간별 활동량",
        data: activityData.map((d) => d.count),
        borderColor: "#2563eb",
        backgroundColor: "rgba(37, 99, 235, 0.2)",
        fill: true,
      },
    ],
  };

  const chartOptions = {
    plugins: {
      legend: {
        labels: {
          usePointStyle: true,
          pointStyle: "circle",
          boxWidth: 4,
          boxHeight: 4,
          padding: 4,
        }
      }
    }
  }

  return (
    <div className="chart-container">
      <h3>사용자의 활동량 분석</h3>
      <Line data={chartData} options={chartOptions} />
    </div>
  );
};

export default PostActivityChart;
