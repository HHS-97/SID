import React from 'react';
import { Doughnut } from 'react-chartjs-2';
import { Chart, ArcElement, Tooltip, Legend } from 'chart.js';

Chart.register(ArcElement, Tooltip, Legend);

const InteractionTypeChart = ({ interactions }) => {
  // 각 상호작용 유형별 카운트 계산
  const typeCount = interactions.reduce((acc, interaction) => {
    const type = interaction.type;
    if (type) {
      acc[type] = (acc[type] || 0) + 1;
    }
    return acc;
  }, {});

  const chartData = {
    labels: Object.keys(typeCount),
    datasets: [{
      data: Object.values(typeCount),
      backgroundColor: ['#36A2EB', '#FF6384', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'],
    }]
  };

  return (
    <div className="chart-container">
      <h3>상호작용 유형 분포</h3>
      <Doughnut data={chartData} />
    </div>
  );
};

export default InteractionTypeChart;
