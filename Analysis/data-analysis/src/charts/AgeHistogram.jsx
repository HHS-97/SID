import React, { useEffect, useState } from "react";
import { Bar } from "react-chartjs-2";
import { Chart, CategoryScale, LinearScale, BarElement, Tooltip, Legend } from "chart.js";

Chart.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false, 
  scales: {
    x: {
      title: {
        display: true,
      },
    },
    y: {
      title: {
        display: true,
        text: "사용자 수",
      },
      beginAtZero: true,
    },
  },
  plugins: {
    legend: {
      display: false, 
    },
    tooltip: {
      enabled: true,
    },
  },
};

const AgeHistogram = ({ data = [] }) => {
  const [ageCounts, setAgeCounts] = useState({});
  const [sortedAges, setSortedAges] = useState([]);

  useEffect(() => {
    if (!Array.isArray(data) || data.length === 0) {
      // console.warn("AgeHistogram: 연령 데이터가 없습니다.");
      setAgeCounts({});
      setSortedAges([]);
      return;
    }

    try {
      // 연령대 필터링 및 집계
      const ageData = data.reduce((acc, user) => {
        if (!user || typeof user.age === "undefined" || user.age === null) return acc;

        const age = parseInt(user.age, 10);
        if (!isNaN(age) && age >= 0) {
          const group = Math.floor(age / 10) * 10; 
          acc[group] = (acc[group] || 0) + 1;
        }
        return acc;
      }, {});

      const sortedAgeKeys = Object.keys(ageData)
        .map(Number) 
        .sort((a, b) => a - b);

      setAgeCounts(ageData);
      setSortedAges(sortedAgeKeys);
    } catch (error) {
      console.error("📌 AgeHistogram: 연령 데이터 변환 중 오류 발생", error);
      setAgeCounts({});
      setSortedAges([]);
    }
  }, [data]);

  if (sortedAges.length === 0) {
    return <div>📌 연령 분포 데이터가 없습니다.</div>;
  }

  const chartData = {
    labels: sortedAges.map((age) => `${age}대`), 
    datasets: [
      {
        label: "사용자 수",
        data: sortedAges.map((age) => ageCounts[age]),
        backgroundColor: "rgba(54, 162, 235, 0.6)", 
        borderColor: "#2563eb",
        borderWidth: 1,
        barThickness: 30, 
        hoverBackgroundColor: "rgba(37, 99, 235, 0.8)", 
      },
    ],
  };

  return (
    <div className="chart-container" style={{ width: "100%", maxWidth: "600px", height: "400px", margin: "0 auto" }}>
      <h3>연령 분포</h3>
      <Bar data={chartData} options={chartOptions} />
    </div>
  );
};

export default AgeHistogram;
