import random
import math
import numpy as np
import pandas as pd
import os

# 데이터 생성
# user_num : 유저 수
# start_date : 시작 날짜
# end_date : 끝 날짜
# save : 저장 여부
# dir : 저장할 폴더
def generate(user_num = 100, start_date = "2020/10/01 00:00:00", end_date = "2020/12/31 23:59:59", action_range = [30, 70],save = "False", dir = "data"):
  ###
  # 설정들
  ###

  # 큐레이팅 정확도
  curating_accuracy = 0.5

  # 최소 나이
  min_age = 10
  
  # 최대 나이
  max_age = 60
  
  # 최빈 나이
  mode_age = 23

  # 카테고리별 [이름, 가중치, [남자 가중치, 여자 가중치], [최빈 나이 최소, 최빈나이 최대, 가장 큰 가중치, 가장 작은 가중치]]
  category_list = [
    ["게임", 1, [1, 0.5], [10, 20, 1, 0.2]],
    ["스포츠", 1, [1, 0.3], [15, 40, 1, 0.8]],
    ["헬스", 0.8, [1, 0.7], [25, 35, 1, 0.6]],
    ["드라마/영화", 1, [0.4, 1], [10, 40, 1, 0.7]],
    ["만화/애니메이션", 0.5, [1, 1], [20, 30, 1, 0.2]],
    ["여행", 1, [1, 1], [10, 60, 1, 1]],
    ["뷰티", 1, [0.1, 1], [10, 25, 1, 0.1]],
    ["패션", 1, [1, 1], [25, 35, 1, 0.5]],
    ["음식", 1, [1, 1], [30, 40, 1, 0.7]],
    ["미술", 0.2, [1, 1], [25, 40, 1, 0,8]],
    ["공예", 0.5, [0.6, 1], [40, 50, 1, 0.2]],
    ["음악", 1, [1, 1], [10, 30, 1, 0.2]],
    ["사진", 0.7, [1, 1], [30, 35, 1, 0.7]],
    ["공학", 0.3, [1, 0.4], [20, 40, 1, 0.8]],
    ["천문학", 1, [1, 1], [10, 30, 1, 1]]
  ]
  
  # 상호작용 종류 [오래 머물기(S), 좋아요(L), 싫어요(U), 댓글 달기(C), 글 자세히 보기(R), 프로필 방문(V), 아무 액션도 안함(N)]
  # [종류, 좋아할 카테고리 시 가중치, 싫어하는 카테고리 시 가중치, 팔로우 한 카테고리 시 가중치]
  interaction_type_list = [
    ["S", 0, 0],      # 오래 머물기
    ["L", 100, 1],     # 좋아요
    ["U", 1, 50],     # 싫어요
    ["C", 20, 5],     # 댓글달기
    ["D", 200, 100],  # 글 자세히 보기
    ["V", 0, 0],    # 작성자 프로필 방문
    ["R", 50, 300],   # 단순히 읽음
  ]
  
  # 성별이 다를 때 싫은 상호작용 할 확률
  interaction_different_gender_weight = 0.1
  
  # 나이가 다를 수록 싫어하는 상호작용을 할 확률 증가량
  interaction_different_age_weight_delta = 0.005


  ## 필요한 도구 함수
  # 배열을 딕셔너리로
  def arr_to_dict(arr):
    dict = {}
    for i in range(len(arr)):
      dict[arr[i]] = i
    return dict
  
  
  # 카테고리 가중치 정보
  # [태그, 기본 가중치, 성별 가중치, 나이 가중치]
  category_weight_indicies = arr_to_dict([
    "tag",
    "base",
    "gender",
    "age"
  ])
  
  # 유저 정보
  # [인덱스, 이름, 성별, 나이, [관심사 배열]]
  # user_indices = arr_to_dict([
  #   "id",
  #   "nickname",
  #   "gender",
  #   "age",
  #   "interest"
  # ])
  
  # 게시글 정보
  # [인덱스, 작성자ID, 제목, 내용, 작성시간, 좋아요 수, 싫어요 수, 댓글 수, 카테고리]
  # post_indices = arr_to_dict([
  #   "id",
  #   "writer_id",
  #   "title",
  #   "content",
  #   "created_at",
  #   "like_count",
  #   "unlike_count",
  #   "comment_count",
  #   "category"
  # ])
  
  # 날짜 추출
  def generate_datetime():
    year = random.randint(2020, 2024)
    month = random.randint(1, 12)
    day = random.randint(1, 28)
    hour = random.randint(0, 23)
    minute = random.randint(0, 59)
    second = random.randint(0, 59)
  
    return f"{year}/{month}/{day} {hour}:{minute}:{second}"
  
  # 개수 추출
  def generate_count(min, max):
    n = random.randint(1 ** 2, (max - min + 2) ** 2 - 1)
    output = (max + 1) - int(math.sqrt(n))
    return output
  
  # 랜덤으로 성별 뽑기
  def generate_gender(n):
    gender_list = ['F', 'M']
    output = []
    for i in range(n):
      output.append(random.choice(gender_list))
    return output
  
  # 랜덤으로 나이 뽑기
  # n : 뽑을 데이터 개수
  # min_age : 최소 나이
  # max_age : 최대 나이
  # mode_age : 가장 많은 나이
  # init_slope : 초기 기울기 (최빈값에서 멀어질수록 곱해줄 값)
  # delta_slope : 기울기 변화량 (최빈값에서 멀어질 수록 기울기에 곱해줄 값)
  def generate_age(n, min_age, max_age, mode_age, init_slope, delta_slope):
    # 각 나이 별로 가중치 배열 생성
    ages = [age for age in range(min_age, max_age + 1)]
    weights = [1.0 for _ in range(len(ages))]
  
    # 최빈값 아래 계산
    slope = init_slope
    for i in range(mode_age - min_age - 1, -1, -1):
      weights[i] = weights[i + 1] * slope
      slope *= delta_slope
  
    # 최빈값 위 계산
    slope = init_slope
    for i in range(mode_age - min_age + 1, max_age - min_age + 1):
      weights[i] *= weights[i - 1] * slope
      slope *= delta_slope
  
    # 가중치 별 확률 계산
    sum_weight = 0
    for weight in weights:
      sum_weight += weight
  
    for i in range(len(weights)):
      weights[i] /= sum_weight
  
    # 계산된 가중치 출력
    # print(weights[i])
  
    # 확률에 따른 나이 계산
    ages = np.random.choice(ages, n, p=weights)
  
    return ages
  
  
  # 관심 카테고리 뽑기
  # [작성자, 내용, 카테고리]
  def generate_interest(user_num, category_list, gender_arr, age_arr, average_count=2):
    output = []
    sum_weight = 0
    categories = [category[category_weight_indicies["tag"]] for category in category_list]
  
    # 기본 가중치 합 계산
    for category in category_list:
      sum_weight += category[category_weight_indicies["base"]]
  
    # 기본 가중치 normalize
    base_weights = [0.0 for _ in range(len(category_list))]
  
    for i in range(len(base_weights)):
      base_weights[i] = category_list[i][category_weight_indicies["base"]] / sum_weight
  
    # 각 유저에 따라 관심 카테고리 선택
    for i in range(user_num):
      interests = []
      # 기본 가중치
  
      weights = []
      for weight in base_weights:
        weights.append(weight)
  
      # 각 카테고리에 대해
      for j in range(len(weights)):
  
        # 성별 가중치
        gender_weights = category_list[j][category_weight_indicies["gender"]]
        gender_sum = sum(gender_weights)
        gender_weight = 0
        if gender_arr[i] == 'M':
          gender_weight = gender_weights[0] / gender_sum
        else:
          gender_weight = gender_weights[1] / gender_sum
        weights[j] *= gender_weight
  
        # 나이 가중치
        age_weights = category_list[j][category_weight_indicies["age"]]
        max_age_diff = max(age_weights[0] - min_age, max_age - age_weights[1], 0)
        delta_age_weight = 0
        if(max_age_diff != 0):
          delta_age_weight = (age_weights[2] - age_weights[3]) / max_age_diff
  
        # 나이가 최빈 나이 최소 값보다 작을 때
        if age_arr[i] <= age_weights[0]:
          age_weight = age_weights[2] - (age_weights[0] - age_arr[i]) * delta_age_weight
        # 나이가 최빈 나이 최댓값 보다 클 때
        elif age_arr[i] >= age_weights[1]:
          age_weight = age_weights[2] - (age_arr[i] - age_weights[1]) * delta_age_weight
        # 그 사이 값일 때
        else:
          age_weight = age_weights[2]
        weights[j] *= age_weight / age_weights[2]
  
      # weights normalize
      weight_sum = sum(weights)
      for i in range(len(weights)):
        weights[i] /= weight_sum
  
      # 뽑기
      for category_idx in range(len(category_list)):
        if random.random() <= weights[category_idx] * average_count:
          interests.append(categories[category_idx])
  
      # 한 개도 안 뽑혔다면 하나를 집어넣어줌
      if len(interests) == 0:
        interests = np.random.choice(categories, 1, p=weights).tolist()
  
      output.append(interests)
    return output
  
  
  # 유저 뽑기
  def generate_user(n):
    gender_arr = generate_gender(n)
    age_arr = generate_age(n, min_age, max_age, mode_age, 0.95, 0.997)
    interest_arr = generate_interest(n, category_list, gender_arr, age_arr)
  
    users = []
    for i in range(n):
      # [인덱스, 이름, 성별, 나이, [관심사 배열]]
      name_prefix = random.choice(["", "하나둘", "둘둘", "셋둘","지존","최강", "우주최강","무서운"])
      name_center = random.choice(["책책","꽹가리","짹짹이","참새","춘봉","춘삼","덕춘","꿀벌","대상혁","잠만보","메타몽","고라파덕","토게피","참새","피카츄","라이츄","파이리","꼬부기","버터풀","야도란","피존투","또가스"])
      name_postfix = random.choice(["", "왕자", "공주", "왕", "여왕","친구"])
      user = {
        "id": i,
        "nickname": name_prefix + name_center + name_postfix + f"{i:0{len(str(n))}d}",
        "gender": gender_arr[i],
        "age": age_arr[i],
        "interest": interest_arr[i]
      }
      users.append(user)
  
    return users
  
  # 글 목록 뽑기
  def generate_post(user, date):
    age = user["age"]
    category = random.choice(user["interest"])
    gender_str = '남성' if user["gender"] == 'M' else '여성'

    title = f"{user["nickname"]}의 {category} 글"
    content = f"{age}세 {gender_str}이 쓴 {category}과 관련된 글"

    like_count = 0
    unlike_count = 0
    comment_count = 0

    # [인덱스, 작성자ID, 제목, 내용, 작성시간, 좋아요 수, 싫어요 수, 댓글 수, 카테고리]
    post = {
      "id": 0,
      "writer_id": user["id"],
      "title": title,
      "content": content,
      "created_at": date,
      "like_count": like_count,
      "unlike_count": unlike_count,
      "comment_count": comment_count,
      "category": category
    }
    return post
  

  # 행동 1개 뽑기
  def generate_interaction(user, post, is_interest, date):
    types = [t[0] for t in interaction_type_list]
    weights = []

    # 관심 카테고리 일 경우
    if is_interest:
      weight_sum = 0
      for type in interaction_type_list:
        weight_sum += type[1]

      weights = [t[1] / weight_sum for t in interaction_type_list]

    # 관심 없는 카테고리 일 경우
    else:
      weight_sum = 0
      for type in interaction_type_list:
        weight_sum += type[2]
      weights = [t[2] / weight_sum for t in interaction_type_list]

    type = random.choices(types, weights)[0]
    interaction = {
      "user_id": user["id"],
      "post_id": post["id"],
      "type": type,
      "created_at": date
    }

    return interaction 


  # 해당 글을 좋아할까 싫어할까
  def is_user_interest_post(user, writer, post):
    category = post["category"]

    # 글의 관심사가 관심있는 정보인지
    is_interest = False
    if category in user["interest"]:
      is_interest = True

    # # 성별, 나이에 따른 가중치
    user_age = user["age"]
    user_gender = user["gender"]
    writer_age = writer["age"]
    writer_gender = writer['gender']

    # 작성자와 성별이 다를 때 확률적으로 싫어함
    if (is_interest) and (user_gender != writer_gender):
      if random.random() < interaction_different_gender_weight:
        is_interest = False

    # 작성자와 나이가 다르면, 확률적으로 싫어함
    if (is_interest):
      if random.random() < abs(writer_age - user_age) * interaction_different_age_weight_delta:
        is_interest = False

    return is_interest

  # 시뮬레이션
  def simulation(users, start_date, end_date):
    posts = []
    interactions = []

    post_by_category = dict()
    for category in category_list:
      post_by_category[category[0]] = []
  
    action_list = {
      # 글 쓰기
      "W": 3,
      # 글 읽기
      "R": 500,
      # 흥미 변경
      "C": 0.01
    }
    
    action_weight_sum = sum(action_list.values())
    action_weight = []
    weight_sum = 0
    for action, weight in action_list.items():
      action_weight.append({
        "action": action,
        "range": weight_sum + weight / action_weight_sum
      })
      weight_sum += weight / action_weight_sum
    
    # 1시간 단위로 시뮬레이션
    date_range = pd.date_range(start_date, end_date, freq='h')
    month = 0
    for date in date_range:
      # 매 시간 행동을 함
      action_num = random.randint(action_range[0], action_range[1])

      for i in range(action_num):
        user = random.choice(users)

        action = ""
        # 만약 글이 적으면 글 작성 위주로 함
        if len(posts) < 100:
          if random.random() < 0.5:
            action = "W"
          else:
            action = "R"
            
        # 그 외에는 가중치대로
        else:
          weight_value = random.random()
          for i in range(len(action_weight)):
            if weight_value < action_weight[i]["range"]:
              action = action_weight[i]["action"]
              break

        # 글 쓰기
        if action == "W" or len(posts) == 0:
          post = generate_post(user, date)
          post["id"] = len(posts)
          post_category = post["category"]

          posts.append(post)
          post_by_category[post_category].append(post)
        
        # 글 읽기
        elif action == "R":
          post = None

          # post_by_category 를 관심글과 비관심글들로 분류
          interest_posts = []
          uninterest_posts = []
          for category in post_by_category:
            if category in user["interest"]:
              interest_posts += post_by_category[category]
            else:
              uninterest_posts += post_by_category[category]

          # 큐레이팅이 잘 되었다면 관심글을 뽑아줌
          if (random.random() < curating_accuracy and len(interest_posts) > 0) or len(uninterest_posts) == 0:
            try:
              post = random.choice(interest_posts)
            except:
              print("에러: ", interest_posts)
              raise Exception("관심글 추출 중 예외 발생")
          # 그게 아니라면 비관심 글을 뽑아줌
          else:
            post = random.choice(uninterest_posts)

          is_interest = is_user_interest_post(user, users[post["writer_id"]],post)
          interaction = generate_interaction(user, post, is_interest, date)
          interaction_type = interaction["type"]
          
          # 상호작용에 따라 데이터 업데이트
          if interaction_type == "L":
            post["like_count"] += 1
          elif interaction_type == "U":
            post["unlike_count"] += 1
          elif interaction_type == "C":
            post["comment_count"] += 1
          interactions.append(interaction)
          
        # 흥미 변경
        elif action == "C":
          user["interest"] = generate_interest(1, category_list, [user["gender"]], [user["age"]])[0]

      # 매 달마다 현재 날짜 출력
      if date.month != month:
        month = date.month
        print(f"시뮬레이션 진행중: {date.year}년 {date.month}월")
        print(f"글: {len(posts)}개 / 상호작용: {len(interactions)}개")
      
    return {
      "post_arr": posts,
      "interaction_arr": interactions,
    }

  #
  # csv 출력
  #
  
  # category.csv
  def category_to_csv(category_list, dir_prefix):
    columns = ["id", "tag"]
    datas = [[i, category_list[i][0]] for i in range(len(category_list))]
  
    df = pd.DataFrame(datas, columns=columns)
    df.to_csv(dir_prefix + 'category.csv', index=False)
  
  # 흥미 데이터 생성
  def make_interest_df(user_list, category_list):
    columns = ["user_id", "category_id"]
    datas = []
  
    # 빠른 탐색을 위한 카테고리 dict화
    category_dict = arr_to_dict([category[0] for category in category_list])
  
    for user in user_list:
      for interest in user["interest"]:
        datas.append([user["id"], category_dict[interest]])
        
    return pd.DataFrame(datas, columns=columns)
    
  def make_df(user_arr, post_arr, interaction_arr, category_list):
    # 유저
    user_df = pd.DataFrame(user_arr)
    user_df.drop(columns=["interest"], inplace=True)
    user_df.reset_index().rename(columns={"index": "id"}, inplace=True)
    # 게시글
    post_df = pd.DataFrame(post_arr)
    post_df.reset_index().rename(columns={"index": "id"}, inplace=True)
    
    # 상호작용
    interaction_df = pd.DataFrame(interaction_arr)
    
    # 카테고리
    category_df = pd.DataFrame(category_list)
    category_df = category_df.iloc[:, 0].astype(str)
    category_df = category_df.reset_index()
    category_df.columns = ["id", "tag"]
    
    # 흥미
    interest_df = make_interest_df(user_arr, category_list)

    return {
      "users": user_df,
      "posts": post_df,
      "interactions": interaction_df,
      "categories": category_df,
      "interests": interest_df
    }
  
  def make_csv(dir, file_name, data):
    # 폴더가 없다면 생성
    os.makedirs(dir, exist_ok=True)
    # os 따라 폴더 구분자
    divider = '/'
    if '\\' in __file__:
      divider = '\\'
      
    data.to_csv(dir + divider + file_name + ".csv", index=False)

  user_arr = generate_user(user_num)
  output = simulation(user_arr, start_date, end_date)
  print("시뮬레이션 완료")
  
  post_arr = output["post_arr"]
  interaction_arr = output["interaction_arr"]  
  dfs = make_df(user_arr, post_arr, interaction_arr, category_list)
  print("데이터 프레임 생성 완료")
  
    
  if save:
    csvs = [
      {
        "name": "user",
        "data": dfs["users"]
      },
      {
        "name": "post",
        "data": dfs["posts"]
      },
      {
        "name": "interaction",
        "data": dfs["interactions"]
      },
      {
        "name": "category",
        "data": dfs["categories"]
      },
      {
        "name": "interest",
        "data": dfs["interests"]
      }
    ]
    
    current_file_path = os.path.abspath(__file__)
    current_dir = os.path.dirname(current_file_path)
    dir = os.path.join(current_dir, dir)


    # csv 출력
    print(f"csv 출력 시작 : {dir}")
    for csv in csvs:
      make_csv(dir, csv["name"], csv["data"])
      print("csv 출력 완료 : " + csv["name"] + ".csv")
      
if __name__ == '__main__':
  generate(
    # 유저 수
    user_num = 1000,
    
    # 시작 날짜
    start_date = "2024/02/01 00:00:00",
    
    # 끝 날짜
    end_date = "2025/02/16 23:59:59",
    
    # 매 시간 상호작용 수
    action_range = [20, 40],
    
    # 저장 여부
    save = True, 
    
    # 저장할 폴더
    dir = "data3"
  )