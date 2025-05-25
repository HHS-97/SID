import React, { useEffect, useState } from "react";
import { Doughnut } from "react-chartjs-2";
import { Chart, ArcElement, Tooltip, Legend } from "chart.js";

Chart.register(ArcElement, Tooltip, Legend);

const CategoryInterestChart = ({ interests = [], categories = [] }) => {
  const [chartData, setChartData] = useState(null);
  const [clickedIndex, setClickedIndex] = useState(null); // 클릭한 섹션 추적

  useEffect(() => {
    if (!Array.isArray(interests) || interests.length === 0) {
      // console.warn("📌 CategoryInterestChart: 관심사 데이터가 없습니다.");
      setChartData(null);
      return;
    }

    if (!Array.isArray(categories) || categories.length === 0) {
      // console.warn("📌 CategoryInterestChart: 카테고리 데이터가 없습니다.");
      setChartData(null);
      return;
    }

    try {
      const categoryMap = categories.reduce((acc, category) => {
        acc[category.id] = category.tag;
        return acc;
      }, {});

      const interestCount = interests.reduce((acc, interest) => {
        const catId = interest.category_id;
        acc[catId] = (acc[catId] || 0) + 1;
        return acc;
      }, {});

      const sortedCategories = Object.keys(interestCount)
        .map(Number)
        .sort((a, b) => interestCount[b] - interestCount[a]);

      const labels = sortedCategories.map((catId) => categoryMap[catId] || `카테고리 ${catId}`);
      const dataValues = sortedCategories.map((catId) => interestCount[catId]);

      // 연한 색상 적용 (HSL에서 밝기 85% 이상 조정)
      const backgroundColors = labels.map(
        (_, index) => `hsl(${(index * 360) / labels.length}, 90%, 72%)`
      );

      setChartData({
        labels,
        datasets: [
          {
            label: "관심사 수",
            data: dataValues,
            backgroundColor: backgroundColors,
            borderWidth: 1,
          },
        ],
      });
    } catch (error) {
      console.error("📌 CategoryInterestChart: 데이터 변환 중 오류 발생", error);
      setChartData(null);
    }
  }, [interests, categories]);

  if (!chartData) {
    return <div>📌 카테고리별 관심도 데이터가 없습니다.</div>;
  }

  const handleChartClick = (event, elements) => {
    if (!elements.length) return;
    const index = elements[0].index;
    setClickedIndex((prevIndex) => (prevIndex === index ? null : index));
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: "50%", // 도넛 중앙 구멍 크기 조절
    onClick: handleChartClick, // 클릭 이벤트 적용
    plugins: {
      legend: {
        display: true,
        position: "left", // 범례를 왼쪽 정렬
        labels: {
          boxWidth: 15,
          padding: 10,
          font: {
            weight: "bold", 
          },
        },
      },
      tooltip: {
        enabled: true,
        callbacks: {
          label: function (tooltipItem) {
            let total = tooltipItem.dataset.data.reduce((acc, value) => acc + value, 0);
            let value = tooltipItem.raw;
            let percentage = ((value / total) * 100).toFixed(1);
            return ` ${tooltipItem.label}: ${value} (${percentage}%)`;
          },
        },
      },
    },
    elements: {
      arc: {
        borderWidth: 2,
      },
    },
  };

  const modifiedChartData = {
    ...chartData,
    datasets: chartData.datasets.map((dataset) => ({
      ...dataset,
      data: dataset.data.map((value, index) => {
        if (clickedIndex === index) {
          let total = dataset.data.reduce((acc, v) => acc + v, 0);
          let percentage = ((value / total) * 100).toFixed(1);
          return `${value} (${percentage}%)`; // 클릭한 곳에만 퍼센트 표시
        }
        return value;
      }),
    })),
  };

  return (
    <div style={{ maxWidth: "900px", margin: "0 auto", textAlign: "center" }}>
      <h3 style={{ marginBottom: "50px" }}>카테고리별 관심도</h3>
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", gap: "20px" }}>
        <div className="chart-container" style={{ width: "100%", maxWidth: "500px", height: "auto" }}>
          <Doughnut data={modifiedChartData} options={chartOptions} />
        </div>
      </div>
    </div>
  );
};

export default CategoryInterestChart;
