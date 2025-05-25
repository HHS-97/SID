import React from "react";
import { Drawer, List, ListItem, ListItemText, IconButton, useMediaQuery } from "@mui/material";
import MenuIcon from "@mui/icons-material/Menu";
import { Link, useLocation } from "react-router-dom";
import { Home, Search } from "lucide-react"; 
import "./Sidebar.css"; 

const Sidebar = ({ open, handleDrawerToggle }) => {
  const location = useLocation();
  const isMobile = useMediaQuery("(max-width: 768px)"); 

  return (
    <>
      {/* 모바일에서 Drawer가 닫혀 있을 때만 햄버거 버튼 표시 */}
      {isMobile && !open && (
        <IconButton
          onClick={handleDrawerToggle}
          className="hamburger-btn"
          sx={{ position: "fixed", top: 10, left: 10, zIndex: 1300 }}
        >
          <MenuIcon />
        </IconButton>
      )}
      {/* 모바일용 임시 Drawer */}
      <Drawer
        anchor="left"
        variant="temporary"
        open={open}
        onClose={handleDrawerToggle}
        ModalProps={{
          keepMounted: true, 
        }}
        sx={{
          "& .MuiDrawer-paper": { width: 240, height: "100vh" },
          display: { xs: "block", md: "none" },
        }}
      >
        <SidebarContent location={location} handleDrawerToggle={handleDrawerToggle} />
      </Drawer>
      {/* 데스크탑용 영구 Drawer */}
      {!isMobile && (
        <Drawer
          anchor="left"
          variant="permanent"
          className="sidebar-container"
          sx={{
            width: 240,
            flexShrink: 0,
            "& .MuiDrawer-paper": {
              width: 240,
              height: "100vh",
              position: "fixed",
              top: 0,
              left: 0,
              zIndex: 1200,
            },
          }}
        >
          <SidebarContent location={location} />
        </Drawer>
      )}
    </>
  );
};

const SidebarContent = ({ location, handleDrawerToggle }) => (
  <List className="sidebar-menu">
    {menuItems.map((item) => (
      <ListItem
        button
        key={item.path}
        component={Link}
        to={item.path}
        onClick={handleDrawerToggle}
        selected={location.pathname === item.path}
        className={`sidebar-link ${location.pathname === item.path ? "active" : ""}`}
      >
        <item.icon className="sidebar-icon" />
        <ListItemText primary={item.label} />
      </ListItem>
    ))}
  </List>
);

//메뉴 항목 목록
const menuItems = [
  { label: "Dashboard", path: "/analysis/", icon: Home },
  { label: "Search", path: "/analysis/search", icon: Search },
];

export default Sidebar;
