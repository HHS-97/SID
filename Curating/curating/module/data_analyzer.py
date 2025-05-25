### 데이터 분석

from tabulate import tabulate
import pandas as pd
import os

class DataAnalyzer:
  def __init__(self, user, interest, category, post, interaction):
    self.user_data = user
    self.interest_data = interest
    self.category_data = category
    self.post_data = post
    self.interaction_data = interaction
    

    
  # 유저 분석
  def get_userinfo(self):
    avg_age = self.user_data["age"].mean()
    gender_counts = self.user_data["gender"].value_counts()
    male_count = gender_counts.get('M', 0)
    female_count = gender_counts.get('F', 0)
    total_count = male_count + female_count
    male_ratio = male_count / total_count * 100
    female_ratio = female_count / total_count * 100

    print('유저 분석')
    print(f"총 인원 수 : {total_count}")
    print(f"평균 나이 : {avg_age}")
    print(f"남: {male_count} 명 ({male_ratio:.2f} %), 여: {female_count} 명 ({female_ratio:.2f} %)")

  # 흥미 테이블 분석
  def get_interestinfo(self):

    # 카테고리 별 카운트
    categories = self.interest_data["category_id"]
    interest_counts = categories.value_counts().reset_index()
    # print(interest_counts)

    # 카테고리 별 평균나이
    user_interest = pd.merge(self.user_data, self.interest_data, left_on="id", right_on="user_id")
    grouped_user_interest = user_interest.groupby("category_id").agg(
        total_count = ('id', 'count'),
        avg_age = ('age', 'mean')
    )

    gender_counts = user_interest.pivot_table(index = 'category_id', columns='gender', aggfunc='size', fill_value = 0)

    # print(grouped_user_interest)
    # print(gender_counts)

    interest_counts = pd.merge(grouped_user_interest, gender_counts, left_on="category_id", right_on="category_id")
    interest_counts = interest_counts.sort_values(by="total_count", ascending=False)
    interest_counts = pd.merge(interest_counts, self.category_data, left_on="category_id", right_on="id")
    interest_counts.set_index("tag", inplace=True)
    interest_counts.drop(columns=["id"], inplace=True)

    # print("흥미 분석")
    # print(tabulate(interest_counts, headers='keys', showindex=True))

  # 게시글 테이블 분석
  def get_postinfo(self):

    post_counts = self.post_data["category"].value_counts().reset_index()
    post_counts.set_index("category", inplace=True)

    # print("게시글 분석")
    # print(tabulate(post_counts, headers='keys', showindex=True))
  
  
  # 전체 정보 출력
  def print_baseinfo(self):
    self.get_userinfo()
    print()
    self.get_interestinfo()
    print()
    self.get_postinfo()
  
  
  #유저 한명 분석
  def analyze_user(self, id, interaction_num = 10):
    print(f'[ id = {id} ]')

    # 유저 정보
    user_info = self.user_data[self.user_data["id"] == id].iloc[0]

    # 유저 흥미 정보
    user_interests = self.interest_data[self.interest_data["user_id"] == id]
    user_interests = pd.merge(user_interests, self.category_data, left_on="category_id", right_on="id")
    user_interests = user_interests["tag"].values


    # 유저가 작성한 글 정보
    user_posts = self.post_data[self.post_data["writer_id"] == id]
    user_posts = user_posts["category"].value_counts().reset_index()
    user_posts.set_index("category", inplace=True)

    # 상호 작용 정보
    user_interactions = self.interaction_data[self.interaction_data["user_id"] == id]
    user_interactions = pd.merge(user_interactions, self.post_data, left_on="post_id", right_on="id")
    user_interactions = user_interactions[['post_id', 'type', 'category', 'created_at_x']]

    user_interaction_counts = pd.pivot_table(user_interactions, index='category', columns='type', aggfunc='size', fill_value=0)
    user_interaction_counts = user_interaction_counts.reindex(columns=['R', 'D', 'S', 'C', 'V', 'L', 'U'], fill_value=0)
    user_interaction_counts.columns = ['읽음', '자세히', '머무름', '댓글', '더보기', '좋아요', '싫어요']

    print('>> 유저 정보')
    print(user_info)
    print()
    print('>> 흥미 정보')
    print(user_interests)
    print()
    print('>> 작성한 게시글')
    print(tabulate(user_posts, headers='keys', showindex=True))

    print()
    print('>> 상호 작용')
    print(f'총 개수 : {user_interactions.shape[0]}개')
    print(tabulate(user_interaction_counts, headers='keys', showindex=True))

    print()
    print(f'상위 {interaction_num}개 출력')
    print(tabulate(user_interactions.tail(interaction_num), headers='keys', showindex=True))
  
  # 유저 여러명 분석
  def analyze_users(self, id_list, interaction_num = 10):
    for id in id_list:
      print()
      self.analyze_user(id, interaction_num)

      print()
      print()
      
      
  #게시글 분석
  def analyze_post(self, id):
    print(f'[ id = {id} ]')

    user_data = self.user_data
    interest_data = self.interest_data
    category_data = self.category_data
    post_data = self.post_data

    target_post = post_data[post_data["id"] == id].iloc[0]
    writer_id = target_post["writer_id"]
    writer_info = user_data[user_data["id"] == writer_id].iloc[0]
    writer_interests = interest_data[interest_data["user_id"] == writer_id]
    writer_interests = pd.merge(writer_interests, category_data, left_on="category_id", right_on="id")
    writer_interests = writer_interests["tag"].values

    print("- 글 정보")
    print(f"글 번호 : {target_post['id']}")
    print(f"글 내용 : {target_post['content']}")
    print(f"카테고리 : {target_post['category']}")
    print(f"작성자 번호 : {target_post['writer_id']}")
    print(f"좋아요 {target_post['like_count']}개 / 싫어요 {target_post['unlike_count']}개 / 댓글 {target_post['comment_count']}개")
    print()
    print("- 작성자 정보")
    print(f"작성자 번호 : {writer_info['id']}")
    print(f"작성자 이름 : {writer_info['nickname']}")
    print(f"작성자 성별 : {writer_info['gender']}")
    print(f"작성자 나이 : {writer_info['age']}")
    print(f"작성자 관심사 : {writer_interests}")

  # 게시글 여러개 분석
  def analyze_posts(self, id_list):
    for id in id_list:
      print()
      self.analyze_post(id)

      print()
      print()
      
if __name__ ==  '__main__':
  encoding = "utf-8-sig"
  # 현재 파일의 위치
  file_path = __file__

  # 현재 파일의 이름 (맥과 윈도우 환경 모두 고려)
  file_name = file_path
  divider = '/'
  if '\\' in file_name:
    divider = '\\'
  file_name = file_name.split(divider)[-1]
  
  # 현재 파일 아래의 data 폴더로 경로 설정
  base_url = file_path.replace(file_name, '..' + divider + 'data' + divider)
  user_data = pd.read_csv(base_url + "user.csv", encoding=encoding)
  interest_data = pd.read_csv(base_url + "interest.csv", encoding=encoding)
  category_data = pd.read_csv(base_url + "category.csv", encoding=encoding)
  post_data = pd.read_csv(base_url + "post.csv", encoding=encoding)
  interaction_data = pd.read_csv(base_url + "interaction.csv", encoding=encoding)
  
  da = DataAnalyzer(user_data, interest_data, category_data, post_data, interaction_data)
  
  # 유저 정보 검색
  # da.analyze_user(1)
  # da.analyze_users([1, 2, 3])
  
  # 게시글 정보 검색
  # da.analyze_post(1)
  da.analyze_posts([3, 7, 10, 40])
  
  while True:
    print("")
    print("----------------------")
    print("데이터 분석")
    print("1번 유저 분석 : u 1")
    print("2번 게시글 분석 : p 2")
    print("종료 : q")
    print()
    command = input("입력 : ")
    print("----------------------")
    try:
    # command를 2개로 쪼갬
      command_list = command.split()
      if command_list[0] == 'q':
        break
      elif command_list[0] == 'u':
        da.analyze_user(int(command_list[1]), interaction_num=20)
        
      elif command_list[0] == 'p':
        da.analyze_post(int(command_list[1]))
    except:
      print("잘못된 입력입니다.")
      print()