import { Bar } from "react-chartjs-2";
import "../chartConfig";

const InterestsChart = () => {
  const data = {
    labels: ["Games", "Food", "Travel", "Tech"],
    datasets: [
      {
        label: "Interest Count",
        data: [400, 350, 300, 250],
        backgroundColor: "#42A5F5",
      },
    ],
  };

  return (
    <div>
      <h2>User Interests</h2>
      <Bar data={data} />
    </div>
  );
};

export default InterestsChart;
