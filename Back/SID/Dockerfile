FROM openjdk:17
ARG JAR_FILE=build/libs/SID-Docker-Spring-0.0.1.jar
COPY ${JAR_FILE} app.jar

COPY src/main/resources/.env src/main/resources/.env


ENTRYPOINT ["java", "-jar", "/app.jar"]