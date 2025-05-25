FROM python:3.12-slim

# 작업 디렉토리 설정
WORKDIR /src

# 가상 환경 생성
RUN python -m venv /opt/venv

# 가상 환경 활성화하고 pip 업그레이드
RUN /opt/venv/bin/pip install --upgrade pip

# requirements.txt 복사 후 설치
COPY requirements.txt .
RUN /opt/venv/bin/pip install --no-cache-dir -r requirements.txt

# 소스 코드 복사
COPY . .

# 환경 변수 설정
COPY .env .env
COPY .env ./curating/.env

# PYTHONPATH 설정
ENV PYTHONPATH=/src/curating:/src

# 포트 열기
EXPOSE 8221

# 가상 환경을 사용하도록 환경 변수 설정
ENV PATH="/opt/venv/bin:$PATH"

# Uvicorn 서버 실행
CMD ["uvicorn", "curating.curating_controller:app", "--host", "0.0.0.0", "--port", "8221"]
