import random
from collections import deque
import sys
import os
import numpy as np
import pandas as pd
import traceback

sys.path.append(os.path.dirname(os.path.abspath(__file__)))
import db_loader

model = None
model_post_num = 0

def set_model(new_model, input_model_post_num):
  global model
  global model_post_num
  
  model = new_model
  model_post_num = input_model_post_num

def get_model():
  global model
  return model

def select_k(datas, k = 30, weights = []):
  # 뽑으려는 거보다 데이터 수가 적으면 그냥 반환
  if(k >= len(datas)):
    return datas

  # 가중치 안넘겨주면 그냥 가중치 1/n
  if(len(weights) != len(datas)):
    weights = [1 / len(datas) for _ in range(len(datas))]
  else:
    weights = np.array(weights)
    weights = weights / np.sum(weights)

  # k개 뽑기
  selected = np.random.choice(datas, k, p=weights, replace=False)
  
  # 순서 섞기
  random.shuffle(selected)
  
  return selected

# 사용자 이름으로 추천
def get_total_curating_by_user_id(user_id = None, like_num = 30, filter = [], total_num = 30):
  interactions = db_loader.get_user_like_interactions(user_id, num=like_num)
  return get_total_curating(user_id, like_list=interactions, filter=filter, total_num=total_num)

# 점수 뽑기
def get_scores(like_list = [], filter = [], num = 30):
  print("좋아요 리스트: ", like_list)

  score_ids = []
  scores = []
  if model != None:
    # 전체 게시글 불러오기
    post_ids = db_loader.get_all_post_ids(filter)

    # pd.values로 반환된 데이터의 최대값
    max_ids = int(model_post_num)

    num = min(num, len(post_ids))
    
    # 0으로 초기화된 1차원 배열
    # 점수가 전부 낮을 때 랜덤으로 나오도록 숫자를 랜덤으로 뽑음
    scores = np.array([random.uniform(0, 0.000001) for _ in range(max_ids)], dtype=np.float64)
    idxs = np.arange(max_ids)

    for idx in filter:
      if(idx >= max_ids):
        continue
      scores[idx] -= len(filter) + 1

    interactions = np.array(like_list)
    
    # max_idx보다 큰 값 뺌
    interactions = interactions[interactions < max_ids]
    
    # 해당하는 상호작용이 없으면 빈 배열 반환
    if interactions.size == 0:
      print("(post_curator) 해당하는 상호작용이 없습니다.")
      return np.array([]), np.array([])
    
    try:
      # interactions 를 순회
      for interaction in interactions:
        # index 값이 넘어가면 패스
        
        source_ids = np.array([interaction for _ in range(max_ids)])
        target_ids = np.arange(max_ids)

        result = model.predict([source_ids, target_ids], batch_size=1024, verbose=0)

        idxs = np.arange(max_ids + 1)
        result = np.array(result.flatten())

        sorted_result = sorted(zip(idxs, result), key=lambda x: x[1], reverse=True)
        for idx, score in sorted_result:
          scores[int(idx)] += score ** 3

        # print("result", sorted_result)
        # print("now score", np.array(sorted(scores, reverse=True), dtype=np.float32))

      # 필터에 없는 인덱스만 선택
      total_scores = list(zip(idxs, scores))
      total_scores = [
        (idx, score) for idx, score in total_scores if idx not in filter
      ]

      # 상위 n개만 남겨둠
      total_scores = sorted(total_scores, key=lambda x: x[1], reverse=True)[:min(len(post_ids), int(num))]

      # 다시 언패킹 
      score_ids, scores = zip(*total_scores)

    except Exception as e:
      print("에러 1", e)
      traceback.print_exc()
  
  else:
    print("모델이 로드되지 않았습니다.")

  return score_ids, scores

# 추천 데이터 생성
def get_total_curating(user_id = None, like_list = [], filter = [], total_num = 30):
  global model

  score_num = 0
  if(model != None):
    score_num = int(random.uniform(0.6, 0.9) * total_num)
  follow_num = int(random.uniform(0.7, 0.8) * (total_num - score_num))

  # print(score_num, follow_num)
  
  filter = np.array(filter)
  original_filter = filter.copy()

  # 추천 데이터 생성
  posts = pd.DataFrame(columns = ['id', 'writer_id', 'title', 'content', 'created_at', 'like_count', 'unlike_count', 'comment_count', 'curated_by'])
  
  # 점수 기반으로 큐레이팅
  # follow가 없을 때를 대비하여 follow_num + score_num * 2 만큼 뽑음
  entire_score_ids, scores = get_scores(like_list=like_list, filter=filter, num=(score_num + follow_num)*2)

  # 점수에 따라서 확률로 게시글 뽑기
  # follow가 없을 때를 대비하여 follow_num + score_num 만큼 뽑음
  score_ids = select_k(entire_score_ids, k = score_num + follow_num, weights = scores)
  # print("인기 게시글 : ", score_ids)

  # 팔로우 목록과 함께 점수기반 추천
  filter = np.concatenate([filter, score_ids])
  if user_id == None:
    if len(score_ids) > 0:
      # 사용자 ID가 없으면 추천점수만 가지고 큐레이팅
      posts = db_loader.get_posts_by_score_ids(
        score_ids=score_ids, 
        score_num = score_num + follow_num,
        filter=filter
      )

  else:
    if len(score_ids) > 0:
      # 평범한 상황에선 팔로잉 + 점수 큐레이팅
      posts = db_loader.get_posts_by_score_ids_and_following(
        user_id=user_id, 
        score_ids=score_ids, 
        score_num = score_num + follow_num, 
        follow_num=follow_num, 
        total_num = score_num + follow_num, 
        filter=filter
      )
    else:
      # 추천 점수가 없으면 팔로우 목록만 가지고 큐레이팅
      posts = db_loader.get_following_posts(user_id=user_id, follow_num=follow_num, filter=filter)
  
  # print("step 1")
  # print(posts[["id", "content", "curated_by"]])

  # 남은 쿼리 날리기
  remain_num = max(total_num - len(posts), 0)
  # 원본 필터에 현재까지 뽑힌 게시글 추가
  filter = np.concatenate([original_filter, posts['id'].to_numpy(dtype=np.int64)])
  if(remain_num > 0) :
    remain_posts = db_loader.get_ultimate_query(user_id=user_id, filter=filter, remain_num=remain_num)
    # print("step 2")
    # print(remain_posts[["id", "content", "curated_by"]])
    # print()
    posts = pd.concat([posts, remain_posts], ignore_index=False)

  # posts중 id가 같은 값들 제외
  posts = posts[~posts['id'].duplicated()]

  # 그래도 남으면 최신글들 순서대로 추출
  remain_num = max(total_num - len(posts), 0)
  filter = np.concatenate([original_filter, posts['id'].to_numpy(dtype=np.int64)])
  if(remain_num > 0) :
    remain_posts = db_loader.get_recent_posts(num=remain_num, filter=filter)
    # print("step 3")
    # print(remain_posts[["id", "content", "curated_by"]])
    # print()
    posts = pd.concat([posts, remain_posts], ignore_index=False)


  # 모든 게시글들을 출력
  
  print(f"유저 {user_id}의 큐레이팅 결과 : ")
  for idx, row in posts.iterrows():
    print(
      row['id'],
      '\t', 
      row['curated_by'],
      '\t',
      row['content'][:20] + '...' if len(row['content']) > 20 else row['content']
    )
  print()
  
  
  # 뽑은 게시글 들 순서 섞기
  posts = posts.sample(frac=1).reset_index(drop=True)
  
  
  return posts

# 게시글과 비슷한 게시글 목록
def get_similar_posts(post_id, num = 20):
  if model == None:
    print("(get_similar_posts) 학습된 모델이 없습니다.")
    return pd.DataFrame()
  
  post_ids, scores = get_scores(like_list=[post_id], filter=[], num=num)
  
  posts = None
  if len(post_ids) == 0:
    posts = pd.DataFrame()
    print("(get_similar_posts) 비슷한 게시글이 없습니다.")
    
  else:
    posts = db_loader.get_posts_by_ids(post_ids)
    
    posts.reset_index(drop=True, inplace=True)
    # print(posts)
    post_list = pd.DataFrame()
    
    for id in post_ids:
      post = posts[posts['id'] == id]
      
      #post_list 에 추가
      post_list = pd.concat([post_list, post], ignore_index=True)
      
    posts = post_list
  return posts

# 팔로우 목록에서 큐레이팅
def get_follow_curating(user_id, follow_num = 20, filter = []):
  posts = db_loader.get_following_posts(user_id=user_id, follow_num=follow_num, filter=filter)
  return posts

if __name__ == "__main__":
  arr = [0, 1, 2, 3, 4]
  # print(arr[1:2])