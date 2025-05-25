import React from "react";
import { Pie } from "react-chartjs-2";
import "../chartConfig";

const UserStatistics = () => {
  const data = {
    labels: ["Male", "Female", "Other"],
    datasets: [
      {
        data: [60, 35, 5], // 예제 데이터
        backgroundColor: ["#36A2EB", "#FF6384", "#FFCE56"],
      },
    ],
  };

  return (
    <div>
      <h2>Gender Distribution</h2>
      <Pie data={data} />
    </div>
  );
};

export default UserStatistics;
