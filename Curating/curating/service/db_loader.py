from sqlalchemy import create_engine
import pandas as pd
import os
import sys
from dotenv import load_dotenv
load_dotenv()

sys.path.append(os.path.dirname(os.path.abspath(__file__))) 
import mapper

SQL_SETTING = {
  'user': os.environ.get('DB_USER'),
  'password': os.environ.get('DB_PASSWORD'),
  'host': os.environ.get('DB_HOST'),
  'database': os.environ.get('DB_NAME'),
  'charset': os.environ.get('DB_CHARSET', 'utf8mb4')  # 기본값을 'utf8mb4'로 설정
}

engine = create_engine(f"mysql+pymysql://{SQL_SETTING['user']}:{SQL_SETTING['password']}@{SQL_SETTING['host']}:3306/{SQL_SETTING['database']}?charset={SQL_SETTING['charset']}")

# 필터 배열 전처리
def filter_to_tuple(filter):
  if(len(filter) == 0):
    filter = [-1]
  return tuple(filter)

# 유저 데이터 불러오기
def get_users():
  users = pd.read_sql(mapper.SELECT_ALL_USER, engine)
  return users

# 포스트 데이터 불러오기
def get_posts():
  posts = pd.read_sql(mapper.SELECT_ALL_POST, engine)
  #칼럼명 변경
  posts.rename(columns={"tag": "category"}, inplace=True)
  return posts

# 카테고리 데이터 불러오기
def get_categories():
  categories = pd.read_sql(mapper.SELECT_ALL_CATEGORY, engine)
  return categories

# 관심사 데이터 불러오기
def get_interests():
  interests = pd.read_sql(mapper.SELECT_ALL_INTEREST, engine)
  # 만약 id 칼럼이 있다면
  if 'id' in interests.columns:
    interests.drop('id', axis=1, inplace=True)
  return interests

# 상호작용 데이터 불러오기
def get_interactions():
  interactions = pd.read_sql(mapper.SELECT_ALL_INTERACTION, engine)
  interactions.drop('id', axis=1, inplace=True)
  return interactions

# 좋아요 데이터 불러오기
def get_likes():
  likes = pd.read_sql(mapper.SELECT_ALL_LIKE, engine)
  return likes

# 댓글 데이터 불러오기
def get_comments():
  comments = pd.read_sql(mapper.SELECT_ALL_COMMENT, engine)
  return comments

# 모든 포스트 id 불러오기
def get_all_post_ids(filter : list = []):

  params = {"filter" : filter_to_tuple(filter)}
  
  posts = pd.read_sql(mapper.SELECT_ALL_POST, engine, params=params)
  return posts['id'].values

# 큐레이팅 데이터 불러오기
def load_data():
  print("Load data from DB")
  
  users = get_users()
  print("User data loaded")
  
  posts = get_posts()
  print("post data loaded")
  
  categories = get_categories()
  print("category data loaded")
  
  interests = get_interests()
  print("interest data loaded")
  
  interactions = get_interactions()
  print("interaction data loaded")
  
  likes = get_likes()
  print("like data loaded")

  comments = get_comments()
  print("comment data loaded")

  # 좋아요 목록에서 행동데이터 추출
  likes = likes.apply(lambda like: {
    "created_at": like['created_at'],
    "type": "L" if like["positive"] == 1 else "U",
    "update_count": 0,
    "updated_at": like['created_at'],
    "post_id": like['post_id'],
    "user_id": like['profile_id'],
    "profile_id": like['profile_id']
  }, axis=1)
  likes = pd.DataFrame(likes.tolist())
  interactions = pd.concat([interactions, likes], ignore_index=True)
  interactions['created_at'] = pd.to_datetime(interactions['created_at'])

  # 게시글 목록에서 행동데이터 추출
  posts_interactions = posts.apply(lambda post: {
    "created_at": post['created_at'],
    "type": "W",
    "update_count": 0,
    "updated_at": post['created_at'],
    "post_id": post['id'],
    "user_id": post['profile_id'],
    "profile_id": post['profile_id']
  }, axis=1)
  posts_interactions = pd.DataFrame(posts_interactions.tolist())
  interactions = pd.concat([interactions, posts_interactions], ignore_index=True)
  interactions['created_at'] = pd.to_datetime(interactions['created_at'])

  # 댓글 목록에서 행동데이터 추출
  comments_interactions = comments.apply(lambda comment: {
    "created_at": comment['created_at'],
    "type": "C",
    "update_count": 0,
    "updated_at": comment['created_at'],
    "post_id": comment['post_id'],
    "user_id": comment['profile_id'],
    "profile_id": comment['profile_id']
  }, axis=1)
  comments_interactions = pd.DataFrame(comments_interactions.tolist())
  interactions = pd.concat([interactions, comments_interactions], ignore_index=True)
  interactions['created_at'] = pd.to_datetime(interactions['created_at'])

  # interaction 데이터 정렬
  interactions = interactions.sort_values(by=['created_at'])
  
  print("Data loaded")

  return {
    "user_data" : users,
    "category_data" : categories,
    "interest_data" : interests,
    "post_data" : posts,
    "interaction_data" : interactions,
    "comments" : comments,
  }

# 포스트 id로 포스트 불러오기
def get_posts_by_ids(ids):
  params = {"ids": tuple(ids)}
  posts = pd.read_sql(mapper.SELECT_POST_BY_IDS, engine, params=params)
  return posts

def get_posts_by_score_ids(score_ids, score_num = 10, filter = []):
  params = {
    "score_ids": tuple(score_ids),
    "score_num": score_num,
    "filter": filter_to_tuple(filter)
  }
  posts = pd.read_sql(mapper.SELECT_POST_BY_SCORE_IDS, engine, params=params)
  return posts

# 팔로잉 목록과 점수 기반 추천 불러오기
def get_posts_by_score_ids_and_following(user_id, score_ids, score_num = 10, follow_num = 10, total_num = 20, filter = []):
  params = {
    "user_id": user_id,
    "score_ids": tuple(score_ids),
    "score_num": score_num,
    "follow_num": follow_num,
    "filter": filter_to_tuple(filter),
    "total_num": total_num
  }
  posts = pd.read_sql(mapper.SELECT_POST_BY_SCORE_IDS_AND_FOLLOWING, engine, params=params)
  return posts

# 팔로잉 목록에서 불러오기
def get_following_posts(user_id, follow_num = 10, filter = []):
  params = {
    "user_id": user_id,
    "follow_num": follow_num,
    "filter": filter_to_tuple(filter)
  }
  posts = pd.read_sql(mapper.SELECT_FOLLOWING_POST, engine, params=params)
  return posts

# 나머지 쿼리
def get_ultimate_query(user_id, remain_num = 5, filter = []):
  params = {
    "user_id": user_id,
    "remain_num": remain_num,
    "filter": filter_to_tuple(filter),
    "candidate_num": remain_num * 10
  }
  posts = pd.read_sql(mapper.SELECT_POST_BY_RECENT_POPULARITY, engine, params=params)
  return posts

# 최신글 불러오기
def get_recent_posts(num = 5, filter = []):
  params = {
    "num": num,
    "filter": filter_to_tuple(filter)
  }
  posts = pd.read_sql(mapper.SELECT_RECENT_POST, engine, params=params)
  return posts

# 사용자가 좋아요 누른 상호작용 불러오기
def get_user_like_interactions(user_id, num = 9999999):
  params = {
    "user_id": user_id,
    "num": num
  }
  interactions = pd.read_sql(mapper.SELECT_LIKE_BY_USER_ID, engine, params=params)
  return interactions["post_id"].values

# 추가 함수-score_test_curator.py
def get_post_writer(post_id):
    posts = get_posts_by_ids([post_id])
    if not posts.empty:
        return posts.iloc[0]['writer_id']
    return None


def get_interactions_by_post(post_id):
    interactions = get_interactions()
    return interactions[interactions['post_id'] == post_id]


def get_user_interests(user_id):
    interests = get_interests()
    user_interests = interests[interests['user_id'] == user_id]
    if user_interests.empty:
        return []
    
    categories = get_categories()
    merged = pd.merge(user_interests, categories, left_on='category_id', right_on='id', how='left')
    return merged['tag'].tolist()


def get_user_positive_interactions(user_id, num=9999999):
    interactions = get_interactions()
    df = interactions[(interactions['user_id'] == user_id) & (interactions['type'] == 'L')]
    if len(df) > num:
        df = df.head(num)
    return df

# 게시글 이미지 불러오기
def get_post_image_by_id(post_id):
  params = {
    "post_id": post_id,
  }
  images = pd.read_sql(mapper.SELECT_POST_IMAGE_URL, engine, params=params)
  return images


# 메인
if __name__ == '__main__': 
  print("run db_loader")

  # print(get_users())

  # print(get_posts())
  
  # print(get_categories())

  # print(get_interests())

  # print(get_interactions())

  # print(get_posts_by_ids([1, 2, 3]))

  # print(get_user_like_interactions(1, 10))
