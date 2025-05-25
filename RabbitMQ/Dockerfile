FROM rabbitmq:3.12-management

# STOMP 플러그인 활성화
RUN rabbitmq-plugins enable --offline rabbitmq_stomp

# AMQP(5672), 관리 콘솔(15672), STOMP(61613) 포트 열기
EXPOSE 5672 15672 61613