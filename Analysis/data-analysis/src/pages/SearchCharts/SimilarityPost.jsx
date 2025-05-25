import React, { useState, useEffect } from "react";
import {SimilarityFromAPI} from "../../utils/similarity";

const SimilarityPost = () => {
    
    const [similarityPost, setSimilarityPost] = useState([]);

    useEffect(() => {
        const fetchData = async () => {
            const data = await SimilarityFromAPI();

            setSimilarityPost(data.posts || []);
        };
        fetchData();
    }, []);

    console.log("유사글 데이터 확인 : ",similarityPost );
}