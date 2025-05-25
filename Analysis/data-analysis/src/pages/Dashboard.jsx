import React, { useEffect, useState } from 'react';
import Header from '../components/Header';
import AgeHistogram from '../charts/AgeHistogram';
import PostEngagementChart from '../charts/PostEngagementChart';
import { loadDataFromAPI } from '../utils/dataLoader_api';
import './Dashboard.css'; 
import { Box } from '@mui/material'; 
import PostActivityChart from '../components/PostActivityChart';
import CategoryInterestChart from '../charts/CategoryInterestChart';


const Dashboard = () => {
  const [postData, setPostData] = useState([]);
  const [userData, setUserData] = useState([]);
  const [interestData, setInterestData] = useState([]);
  const [categoryData, setCategoryData] = useState([]);
  const [interactionData, setInteractionData] = useState([]);
  const [commentData, setCommentData] = useState([]);


  useEffect(() => {
    const fetchData = async () => {
      const data = await loadDataFromAPI();

      setUserData(data.userData || []);
      setInterestData(data.interestData || []);
      setCategoryData(data.categoryData || []);
      setPostData(Array.isArray(data.postData) ? data.postData : []);
      setInteractionData(data.interactionData || []);
      setCommentData(data.commentData || []);
    };

    fetchData();
  }, []);
  // console.log("📌 Dashboard에서 postData 상태:", postData);
  // console.log("📌 Dashboard에서 UserData 상태:", userData);
  // console.log("📌 Dashboard에서 interestData 상태:", interestData);
  // console.log("📌 Dashboard에서 categoryData 상태:", categoryData);
  // console.log("📌 Dashboard에서 interactionData 상태:", interactionData);
  // console.log("📌 Dashboard에서 commentData 상태:", commentData);  

  return (
    
    <Box sx={{ display: "flex" }}>
      <Box sx={{ flexGrow: 1}}>
        <Header title="Data Statistics 📈" />
        <div className="dashboard-grid flex-wrap">
          <div className="dashboard-card flex items-center justify-center w-full h-full">
            {/* 사용자 활동량 분석 */ }
            <PostActivityChart interactions={interactionData || []} />
          </div>
          <div className="dashboard-card">
            {/* 서비스 연령 분포 */ }
            <AgeHistogram data={userData}/>
          </div>

          <div className="dashboard-card">
            {/* 카테고리별 관심도 */ }
            <CategoryInterestChart interests={interestData} categories={categoryData} />
            {/* <AgeReactionChart /> */}
          </div>
          <div className="dashboard-card">
            {/* 게시글 참여도 */ }
            <PostEngagementChart posts={postData} comments={commentData} interactions={interactionData || []} />
          </div>
        </div>
      </Box>
    </Box>
  );
};

export default Dashboard;
