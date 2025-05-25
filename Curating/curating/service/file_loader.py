import pandas as pd

encoding = "utf-8-sig"

# csv 데이터 불러오기
def load_data(dir = None):
  base_path = dir
  if(dir == None):
    # 현재 파일의 위치
    file_path = __file__

    # 현재 파일의 이름 (맥과 윈도우 환경 모두 고려)
    file_name = file_path
    divider = '/'
    if '\\' in file_name:
      divider = '\\'
    file_name = file_name.split(divider)[-1]
    
    # 현재 파일 아래의 data 폴더로 경로 설정
    base_path = file_path.replace(file_name, 'data' + divider)
  
  # 유저 데이터 불러오기
  try:
    users = pd.read_csv(base_path + 'user.csv', encoding=encoding)
  except Exception as e:
    print(f"유저 데이터 불러오기 중 에러 발생 : {e}")
    users = pd.DataFrame()
    
  # 카테고리 데이터 불러오기
  try:
    categories = pd.read_csv(base_path + 'category.csv', encoding=encoding)
  except Exception as e:
    print(f"카테고리 데이터 불러오기 중 에러 발생 : {e}")
    categories = pd.DataFrame()
  
  # 관심사 데이터 불러오기
  try:
    interests = pd.read_csv(base_path + 'interest.csv', encoding=encoding)
  except Exception as e:
    print(f"관심사 데이터 불러오기 중 에러 발생 : {e}")
    interests = pd.DataFrame()
    
  # 포스트 데이터 불러오기
  try:
    posts = pd.read_csv(base_path + 'post.csv', encoding=encoding)
  except Exception as e:
    print(f"포스트 데이터 불러오기 중 에러 발생 : {e}")
    posts = pd.DataFrame()
  
  # 상호작용 데이터 불러오기
  try:
    interactions = pd.read_csv(base_path + 'interaction.csv', encoding=encoding)
  except Exception as e:
    print(f"상호작용 데이터 불러오기 중 에러 발생 : {e}")
    interactions = pd.DataFrame()
  
  # 딕셔너리로 반환
  return {
    "user_data" : users,
    "category_data" : categories,
    "interest_data" : interests,
    "post_data" : posts,
    "interaction_data" : interactions
  }