from sqlalchemy import create_engine, text
import os
import sys
import setting
import pandas as pd

###
# csv들 저장할 위치
###
target_dir = "../data/"


SQL_SETTING = setting.SQL_SETTING
engine = create_engine(f"mysql+pymysql://{SQL_SETTING['user']}:{SQL_SETTING['password']}@{SQL_SETTING['host']}:3306/{SQL_SETTING['database']}?charset={SQL_SETTING['charset']}")


# 실행시키는 위치
base_dir = os.path.dirname(os.path.abspath(__file__))

# 주소 세팅
divider = "/"
# linux, mac
if base_dir[-1] != "/":
  divider = "\\"
  target_dir = target_dir.replace("/", "\\")

dir = base_dir + divider + target_dir

# 유저 데이터 불러오기
def get_users():
  sql = "select * from curating_users"
  users = pd.read_sql(sql, engine)
  return users

# 포스트 데이터 불러오기
def get_posts():
  sql = "select curating_posts.*, tag as category from curating_posts join curating_categories on curating_posts.category_id = curating_categories.id"
  posts = pd.read_sql(sql, engine)
  #칼럼명 변경
  posts.rename(columns={"tag": "category"}, inplace=True)
  return posts

# 카테고리 데이터 불러오기
def get_categories():
  sql = "select * from curating_categories"
  categories = pd.read_sql(sql, engine)
  return categories

# 관심사 데이터 불러오기
def get_interests():
  sql = "select * from curating_interests"
  interests = pd.read_sql(sql, engine)
  interests.drop('id', axis=1, inplace=True)
  return interests

# 상호작용 데이터 불러오기
def get_interactions():
  sql = "select * from curating_interactions"
  interactions = pd.read_sql(sql, engine)
  interactions.drop('id', axis=1, inplace=True)
  return interactions

def get_datas():
  users = get_users()
  print("유저 불러오기 완료")
  
  posts = get_posts()
  print("포스트 불러오기 완료")
  
  categories = get_categories()
  print("카테고리 불러오기 완료")
  
  interests = get_interests()
  print("관심사 불러오기 완료")
  
  interactions = get_interactions()
  print("상호작용 불러오기 완료")
  
  return {
    "users": users,
    "posts": posts,
    "categories": categories,
    "interests": interests,
    "interactions": interactions
  }
  
def save_datas(csvs, dir):
  for csv in csvs:
    # 폴더가 없다면 생성
    os.makedirs(dir, exist_ok=True)
    csv["data"].to_csv(dir + divider + csv["name"] + ".csv", index=False)

    print("csv 출력 완료 : " + csv["name"] + ".csv")
    

if __name__ == "__main__":
  dfs = get_datas()
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
  save_datas(csvs, dir)