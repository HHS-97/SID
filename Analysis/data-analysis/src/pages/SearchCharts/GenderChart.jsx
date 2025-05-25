import React, { useState } from "react";
import { Doughnut } from "react-chartjs-2";
import { Chart, ArcElement, Tooltip, Legend } from "chart.js";

Chart.register(ArcElement, Tooltip, Legend);

const GenderChart = ({ postId, interactionData = [], userData = [] }) => {
  const [selectedType, setSelectedType] = useState("L");

  if (!postId) {
    return <div className="text-center text-gray-500">📌 게시글을 선택해주세요.</div>;
  }

  const filteredInteractions = interactionData.filter(
    (interaction) => interaction.post_id === postId && ["L", "U", "D"].includes(interaction.type)
  );

  if (filteredInteractions.length === 0) {
    return <div className="text-center text-gray-500">📌 해당 게시글에 대한 상호작용 데이터가 없습니다.</div>;
  }

  const selectedInteractions = filteredInteractions.filter((interaction) => interaction.type === selectedType);
  const userIds = [...new Set(selectedInteractions.map((interaction) => interaction.user_id))];
  const filteredUsers = userData.filter((user) => userIds.includes(user.id));

  const genderCounts = filteredUsers.reduce((acc, user) => {
    const gender = user.gender;
    if (gender) {
      acc[gender] = (acc[gender] || 0) + 1;
    }
    return acc;
  }, {});

  const chartData = {
    labels: ["남성", "여성"],
    datasets: [
      {
        data: [genderCounts["M"] || 0, genderCounts["F"] || 0],
        backgroundColor: ["#36A2EB", "#FF6384"],
      },
    ],
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: "60%",
    plugins: {
      legend: {
        position: "bottom",
        labels: {
          padding: 20,
          font: {
            size: 14
          }
        }
      }
    }
  };

  return (
    <div className="chart-container w-full max-w-[600px] mx-auto bg-white rounded-lg shadow-md">
      <div className="p-6">
        {/* 제목 */}
        <h3 className="text-xl font-semibold text-center mb-4">
          성별 분포 (
          {selectedType === "L"
            ? "좋아요"
            : selectedType === "U"
            ? "싫어요"
            : "자세히 보기"}
          )
        </h3>
  
        {/* 차트 컨테이너 - 고정 크기 적용 */}
        <div style={{ width: "100%", height: "400px" }}>
          <Doughnut data={chartData} options={chartOptions} />
        </div>
  
        {/* 버튼 그룹 - 중앙 정렬 */}
        <div className="flex justify-center gap-4 mt-4">
          <button
            onClick={() => setSelectedType("L")}
            className={`px-6 py-2.5 rounded-full font-medium transition-all duration-200 shadow-sm ${
              selectedType === "L"
                ? "bg-gradient-to-r from-blue-500 to-blue-600 text-white ring-2 ring-blue-500 ring-opacity-50"
                : "bg-gray-50 text-gray-700 hover:bg-blue-50 hover:text-blue-600"
            }`}
          >
            👍 좋아요
          </button>
          <button
            onClick={() => setSelectedType("U")}
            className={`px-6 py-2.5 rounded-full font-medium transition-all duration-200 shadow-sm ${
              selectedType === "U"
                ? "bg-gradient-to-r from-red-500 to-red-600 text-white ring-2 ring-red-500 ring-opacity-50"
                : "bg-gray-50 text-gray-700 hover:bg-red-50 hover:text-red-600"
            }`}
          >
            👎 싫어요
          </button>
          <button
            onClick={() => setSelectedType("D")}
            className={`px-6 py-2.5 rounded-full font-medium transition-all duration-200 shadow-sm ${
              selectedType === "D"
                ? "bg-gradient-to-r from-green-500 to-green-600 text-white ring-2 ring-green-500 ring-opacity-50"
                : "bg-gray-50 text-gray-700 hover:bg-green-50 hover:text-green-600"
            }`}
          >
            🔍 자세히 보기
          </button>
        </div>
      </div>
    </div>
  );
  
};

export default GenderChart;