import { Line } from "react-chartjs-2";
import "../chartConfig";

const InteractionAnalysis = () => {
  const data = {
    labels: ["Jan", "Feb", "Mar", "Apr", "May"],
    datasets: [
      {
        label: "User Interactions",
        data: [500, 700, 1000, 1200, 1500],
        borderColor: "#42A5F5",
        fill: false,
      },
    ],
  };

  return (
    <div>
      <h2>Interaction Trends</h2>
      <Line data={data} />
    </div>
  );
};

export default InteractionAnalysis;
