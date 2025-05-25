import requests
import model_gpt as model
import pandas as pd
import json
import os
import h5py
from pydantic import BaseModel, Field
import traceback
import threading
import asyncio
from dotenv import load_dotenv
load_dotenv()

URL = {
  'LOCAL' :os.environ.get('LOCAL_CURATING_URL'),
  'EC2' : os.environ.get('EC2_CURATING_URL')
}

# BASE_URL = URL["LOCAL"]
BASE_URL = URL["EC2"]





loop_continue = True

class SampleModel(BaseModel):
  data: dict = Field(default=None)
  
  def set_data(self, data):
    self.data = data
  
  def train(self):
    return "trained model"

def load_data():
  # reqeust get 요청
  response = requests.get(BASE_URL + '/data/all')
  
  if response.status_code != 200:
    raise Exception(f"데이터 요청 실패: {response.status_code}")

  data = response.json()
  
  # 문자열 데이터를 json으로 변환
  for key in data.keys():
    if isinstance(data[key], str):
      data[key] = json.loads(data[key])
  
  # 데이터 프레임으로 변환
  user_data = pd.DataFrame(data['user_data'])
  category_data = pd.DataFrame(data['category_data'])
  interest_data = pd.DataFrame(data['interest_data'])
  post_data = pd.DataFrame(data['post_data'])
  interaction_data = pd.DataFrame(data['interaction_data'])
  
  return {
    "user_data" : user_data,
    "category_data" : category_data,
    "interest_data" : interest_data,
    "post_data" : post_data,
    "interaction_data" : interaction_data
  }

def send_model(model_path):
  if not os.path.exists(model_path):
    raise Exception("전송 실패: 모델 파일이 없습니다.")

  with open(model_path, 'rb') as model_file:
    files = {'new_model': ('trained_model.h5', model_file, 'application/octet-stream')}
    response = requests.post(BASE_URL + "/model/refresh", files=files)

  if response.status_code == 200:
    print("모델 업데이트 완료")
  else:
    raise Exception(f"모델 전송 실패: {response.status_code}")

# main 함수
async def main():
  global loop_continue
  while loop_continue:
    try:
      # 데이터 불러오기
      data = load_data()
      print("Data loaded")
      
      # 데이터 모델에 세팅
      try: 
        model.set_data(data)
        print("Data settting completed")
      except Exception as e:
        print(f"에러 발생 : {e}")
        traceback.print_exc()
        return
      
      # 학습 돌리기
      trained_model, post_num = model.train()
      print("Model training completed")

      # 모델 저장
      model_path = "trained_model.h5"
      trained_model.save(model_path)
      with h5py.File("trained_model.h5", "a") as f:
        f.attrs["post_input_size"] = post_num
      
      print("Model saved")
      
      # 모델 전송
      print("Start to send Model")
      # send_model(trained_model)
      send_model(model_path)
      print("Successfully sent model")
      
    except Exception as e:
      print(f"에러 발생 : {e}")
      traceback.print_exc()
    
    finally:
      await asyncio.sleep(30)
    
if __name__ == '__main__':
  # 스케줄 등록
  # 일정 주기마다 실행
  asyncio.run(main())
  print("프로그램 종료")