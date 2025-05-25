import React from 'react';
import { Doughnut } from 'react-chartjs-2';
import { Chart, ArcElement, Tooltip, Legend } from 'chart.js';

Chart.register(ArcElement, Tooltip, Legend);

const GenderChart = ({ data }) => {
  const genderCount = data.reduce((acc, user) => {
    const gender = user.gender;
    if (gender) {
      acc[gender] = (acc[gender] || 0) + 1;
    }
    return acc;
  }, {});

  const chartData = {
    labels: Object.keys(genderCount),
    datasets: [{
      data: Object.values(genderCount),
      backgroundColor: ['#36A2EB', '#FF6384'],
    }],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false, // 크기 조정 가능하게 설정
    plugins: {
      legend: {
        position: "top",
        align: "center"
      },
    },
  };

  return (
    <div className="flex items-center justify-center w-full h-full">
        <Doughnut data={chartData} options={options} />
    </div>
  );
};
export default GenderChart;
