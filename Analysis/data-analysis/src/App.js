import React, { useState } from "react";
import { Routes, Route } from "react-router-dom";
import { useMediaQuery } from "@mui/material";
import Sidebar from "./components/Sidebar";
import Dashboard from "./pages/Dashboard";
import Search from "./pages/Search";

const App = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const isMobile = useMediaQuery("(max-width:768px)");

  const handleDrawerToggle = () => {
    setSidebarOpen(!sidebarOpen);
  };

  return (
    <div style={{ display: "flex", backgroundColor: "#f9fafb", minHeight: "100vh" }}>
      {/* Sidebar 컴포넌트 */}
      <Sidebar open={sidebarOpen} handleDrawerToggle={handleDrawerToggle} />

      {/* 메인 컨텐츠 영역*/}
      <div style={{ flexGrow: 1, padding: "20px", marginLeft: isMobile ? 0 : "280px" }}>
        <Routes>
          <Route path="/analysis/" element={<Dashboard />} />
          <Route path="/analysis/search" element={<Search />} />
        </Routes>
      </div>
    </div>
  );
};

export default App;
