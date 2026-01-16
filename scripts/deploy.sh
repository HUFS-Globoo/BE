#!/bin/bash

REPOSITORY=/home/ubuntu/app
echo "> 현재 구동중인 애플리케이션 pid 확인"

# 'gitget' 대신 실제 JAR 파일 이름에 포함된 'backend'로 검색하거나 '.jar'로 검색
CURRENT_PID=$(pgrep -f "backend-0.0.1-SNAPSHOT.jar")

echo "현재 PID: $CURRENT_PID"

if [ -z "$CURRENT_PID" ]; then
    echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
    echo "> kill -15 $CURRENT_PID"
    kill -15 "$CURRENT_PID"
    sleep 5
fi

echo "> 새 애플리케이션 배포"

# 가장 최근에 빌드된 jar 파일 찾기
JAR_NAME=$(ls -tr $REPOSITORY/build/libs/*.jar | tail -n 1)

echo "> JAR Name: $JAR_NAME"
echo "> 새 애플리케이션 실행"

# 실행 (운영 환경 프로필 적용)
nohup java -jar \
    -Dspring.profiles.active=prod \
    "$JAR_NAME" > $REPOSITORY/nohup.out 2>&1 &