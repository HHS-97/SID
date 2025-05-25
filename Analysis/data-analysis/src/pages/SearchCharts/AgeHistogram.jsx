import React from "react";
import { Bar } from "react-chartjs-2";
import { Chart, CategoryScale, LinearScale, BarElement, Tooltip, Legend } from "chart.js";

Chart.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

const AgeHistogram = ({ postId, interactionData = [], userData = [] }) => {
  if (!postId) {
    return <div className="text-center text-gray-500">📌 게시글을 선택해주세요.</div>;
  }

  const interactedUserIds = interactionData
    .filter(interaction => interaction.post_id === postId)
    .map(interaction => interaction.user_id);

  const filteredUsers = userData.filter(user => interactedUserIds.includes(user.id));

  if (filteredUsers.length === 0) {
    return <div className="text-center text-gray-500">📌 해당 게시글에 대한 사용자 연령 데이터가 없습니다.</div>;
  }

  const ageGroups = filteredUsers.reduce((acc, user) => {
    const age = parseInt(user.age, 10);
  
    if (isNaN(age) || age <= 0) {
      acc["나이 모름"] = (acc["나이 모름"] || 0) + 1; // 0 이하 또는 없는 경우 "나이 모름"
    } else if (age < 10) {
      acc["10대 미만"] = (acc["10대 미만"] || 0) + 1; // 0~9세는 "10대 미만"
    } else {
      const group = Math.floor(age / 10) * 10; // 10단위 그룹화
      acc[group] = (acc[group] || 0) + 1;
    }
  
    return acc;
  }, {});
  

  const sortedAges = Object.keys(ageGroups)
  .sort((a, b) => {
    if (a === "나이 모름") return -1;
    if (b === "나이 모름") return 1;
    if (a === "10대 미만") return -1;
    if (b === "10대 미만") return 1;
    return a - b;
  });


  const chartData = {
    labels: sortedAges.map(age => (age === "나이 모름" || age === "10대 미만" ? age : `${age}대`)),
    datasets: [
      {
        label: "연령대 분포",
        data: sortedAges.map(age => ageGroups[age]),
        backgroundColor: "#36A2EB",
      },
    ],
  };

  return (
    <div className="chart-container w-full max-w-[600px] mx-auto">
      <h3 className="text-lg font-semibold text-center mb-4">연령 분포</h3>
      <div style={{ width: "100%", height: "400px" }}>
        <Bar 
          data={chartData} 
          options={{ 
            responsive: true, 
            maintainAspectRatio: true,
            scales: {
              x: { 
                ticks: { maxRotation: 45, minRotation: 30 } 
              },
              y: { 
                title: { display: true, text: "사용자 수" }, 
                beginAtZero: true 
              }
            },
            plugins: {
              legend: { display: false },
              tooltip: {
                callbacks: {
                  label: (tooltipItem) => `${tooltipItem.raw}명`,
                }
              }
            }
          }} 
        />
      </div>
    </div>
  );
};

export default AgeHistogram;
