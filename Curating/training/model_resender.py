import os
import requests
from dotenv import load_dotenv
load_dotenv()

URL = {
  'LOCAL' :os.environ.get('LOCAL_CURATING_URL'),
  'EC2' : os.environ.get('EC2_CURATING_URL')
}

# BASE_URL = URL["LOCAL"]
BASE_URL = URL["EC2"]

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
def main():
  try:
    # 모델 파일 전송
    send_model("trained_model.h5")
  except Exception as e:
    print(f"모델 전송 중 에러 발생: {e}")

if __name__ == "__main__":
  main()