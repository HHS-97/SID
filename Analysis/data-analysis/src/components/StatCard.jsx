import React from "react";
import { Line } from "react-chartjs-2";
import { Chart, LineElement, PointElement, LinearScale, CategoryScale, Tooltip } from "chart.js";

Chart.register(LineElement, PointElement, LinearScale, CategoryScale, Tooltip);

const StatCard = ({ title, value, change, icon, bgColor, lineColor, data }) => {
  const chartData = {
    labels: ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"],
    datasets: [
      {
        data: data,
        borderColor: lineColor,
        borderWidth: 2,
        fill: false,
        tension: 0.4,
      },
    ],
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: { enabled: false },
    },
    scales: {
      x: { display: false },
      y: { display: false },
    },
  };

  return (
    <div className={`rounded-xl p-6 shadow-lg ${bgColor} w-[280px] h-[150px] flex flex-col justify-between`}>
      <div className="flex justify-between items-center">
        <div className="text-4xl">{icon}</div>
        <span className="text-sm font-medium">{change}</span>
      </div>

      <div>
        <h3 className="text-sm font-semibold">{title}</h3>
        <p className="text-2xl font-bold">{value}</p>
      </div>

      <div className="w-24 h-10 self-end">
        <Line data={chartData} options={chartOptions} />
      </div>
    </div>
  );
};

export default StatCard;
