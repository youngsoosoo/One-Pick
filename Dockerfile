# OpenJDK 11 Alpine image
FROM openjdk:11

# 컨테이너 내부의 /app 디렉토리 생성
WORKDIR /app
# 컨테이너 내부의 /app 디렉토리에 jar 파일 복사
ARG JAR_FILE=build/libs/One_Pick-0.0.1-SNAPSHOT.jar
ARG CONTAINER_JAR_FILE=One-Pick.jar
COPY ${JAR_FILE} /app/${CONTAINER_JAR_FILE}

# Spring Boot를 실행하기 위한 entry point 지정
ENTRYPOINT ["java", "-jar", "/app/One-Pick.jar"]

# 시간 설정
ENV TZ=Asia/Seoul
# tzdata 설치 (apt 사용)
RUN apt update && apt install -y tzdata