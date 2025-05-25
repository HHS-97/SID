from fastapi import FastAPI, File, UploadFile, Query, APIRouter
import uvicorn
from service import post_curator as curator
from service import file_loader
from service import db_loader
import os
import sys
import tensorflow as tf
import tempfile
from typing import List
import h5py

from fastapi.middleware.cors import CORSMiddleware

sys.path.append(os.path.dirname(os.path.abspath(__file__)))
app = FastAPI()
router = APIRouter(prefix="/curating")

# CORS 설정(통계 로컬)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://localhost:3001", "http://43.202.53.108:3001", "https://i12c110.p.ssafy.io"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

def load_csv_data():
  # 불러올 파일 위치
  file_path = "data/testdata"
  
  # os에 따라 다른 경로 구분자
  divider = '/'
  if '\\' in __file__:
    divider = '\\'

  file_path = divider + divider.join(file_path.split('/')) + divider
  base_path = os.path.dirname(os.path.abspath(__file__))

  return file_loader.load_data(base_path + file_path)


# 초기 데이터 불러오기
def init_data():

  # csv 데이터 불러와서 각 변수에 넣기
  # 현재 메인함수 실행시킨 위치의 data 폴더에 있는 데이터 불러오기
  # data = load_csv_data()
  
  # db에서 데이터 불러오기
  data = db_loader.load_data()

  users = data['user_data']
  categories = data['category_data']
  interests = data['interest_data']
  posts = data['post_data']
  interactions = data['interaction_data']
  comments = data['comments']

  #각 데이터를 json 형태로 변환
  users = users.to_json(orient='records', force_ascii=False)
  categories = categories.to_json(orient='records', force_ascii=False)
  interests = interests.to_json(orient='records', force_ascii=False)
  posts = posts.to_json(orient='records', force_ascii=False)
  interactions = interactions.to_json(orient='records', force_ascii=False)
  comments = comments.to_json(orient='records', force_ascii=False)
  
  return {
    "user_data" : users,
    "category_data" : categories,
    "interest_data" : interests,
    "post_data" : posts,
    "interaction_data" : interactions,
    "comments" : comments
  }
  
# 추가 데이터들 불러오기
def load_new_data():
  users = []
  interactions = []
  categories  = []
  posts = []
  interests = []
  
  return {
    "user_data" : users,
    "category_data" : categories,
    "interest_data" : interests,
    "post_data" : posts,
    "interaction_data" : interactions
  }

# 글들의 ID 배열 추출 (dataframe -> list)
def get_post_ids(posts):
  return posts['id'].tolist()
    

@router.get("/")
def read_root():
  return {"message": "Curating Server Online!"}

@router.post('/model/refresh')
async def refresh_model(new_model: UploadFile = File(...)):
  try:
    model_post_num = 0
    
    # ✅ 임시 파일에 저장
    with tempfile.NamedTemporaryFile(delete=False, suffix=".h5") as temp_file:
      temp_file.write(await new_model.read())
      temp_model_path = temp_file.name
    
    # h5 파일에서 모델 크기 로드
    with h5py.File(temp_model_path, "r") as temp_file:
      # print(temp_file.attrs.keys())
      model_post_num = temp_file.attrs["post_input_size"]

    # ✅ TensorFlow 모델 로드
    model = tf.keras.models.load_model(temp_model_path)
    

    # 모델 갱신
    curator.set_model(model, model_post_num)
    print("모델 갱신 완료 !")
    return {"message": "모델 갱신 완료"}
  
  except Exception as e:
    print(f"모델 갱신 중 에러 발생: {e}")
    return {"message": f"모델 갱신 중 에러 발생: {e}"}
  
@router.get('/data/all')
async def get_init_data():  
  data = init_data()
  users = data['user_data']
  categories = data['category_data']
  interests = data['interest_data']
  posts = data['post_data']
  interactions = data['interaction_data']
  comments = data['comments']
    
  print("초기 데이터 생성 완료")
  
  return {
    "user_data" : users,
    "category_data" : categories,
    "interest_data" : interests,
    "post_data" : posts,
    "interaction_data" : interactions,
    "comments" : comments,
  }

# 유저 이름으로 큐레이팅 (자동으로 최근 상호작용 꺼내서 적용)
@router.get('/curating')
async def curating(
  profile_id : str, 
  viewed_posts : List[int] = Query([]),
  total_num : int = 30,
  like_num : int = 999
):
  posts = curator.get_total_curating_by_user_id(profile_id, like_num=like_num, filter=viewed_posts, total_num=total_num)
  post_ids = get_post_ids(posts)
  # print("큐레이팅 요청")
  # print("유저 이름: ", profile_id)
  # print("추천 게시글 수: ", total_num)
  # print("이미 본 게시글: ", viewed_posts)
  # print("결과: ", post_ids)
  return post_ids

# 상호작용을 직접 넣어줄 때
@router.get('/curating/action')
async def curating(
  profile_id: str = None, 
  like_list : List[int] = Query([]),
  viewed_posts : List[int] = Query([]),
  total_num : int = 30
):
  posts = curator.get_total_curating(user_id=profile_id, like_list=like_list, filter = viewed_posts, total_num=total_num)
  return get_post_ids(posts)

# 게시글을 그대로 뽑아줄 때 (상호작용 단위)
@router.get('/curating/action/original')
async def curating_original(
  user_id: str = None, 
  like_list : List[int] = Query([]), 
  viewed_posts : List[int] = Query([])
):
  posts = curator.get_total_curating(user_id=user_id, like_list=like_list, filter = viewed_posts, total_num=30)
  return posts.to_dict(orient="records")

# 원본 게시글 그대로 뽑아줌 (유저단위)
@router.get('/curating/original')
async def curating_original(
  user_id : str, 
  viewed_posts : List[int] = Query([]),
  like_num : int = 999
):
  posts = curator.get_total_curating_by_user_id(user_id, like_num=like_num, filter=viewed_posts, total_num=30)
  return posts.to_dict(orient="records")

# 비슷한 게시글들 뽑아주기
@router.get('/similarity')
async def curating_similar(
  post_id : int
):
  posts = curator.get_similar_posts(post_id)
  return posts.to_dict(orient="records")
    

@router.get('/post/image/{post_id}')
async def get_post(post_id: int):
  images = db_loader.get_post_image_by_id(post_id)
  return images.to_dict(orient="records")





app.include_router(router)
if __name__ == '__main__':
  print("run curating")
  uvicorn.run(app, host="0.0.0.0", port=8221)
