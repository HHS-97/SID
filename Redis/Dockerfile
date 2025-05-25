# Redis 공식 이미지를 기반으로 설정
FROM redis:latest

# redis.conf 파일을 컨테이너에 복사
COPY redis.conf /usr/local/etc/redis/redis.conf

# 기본적으로 Redis는 6379 포트
EXPOSE 6379

# Redis 서버를 실행
CMD ["redis-server"]