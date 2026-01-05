#!/bin/bash

# 배포된 빌드 파일들이 있는 위치
REPOSITORY=/home/ubuntu/app

# 프로젝트 이름 (자신의 프로젝트 이름으로 수정 필요할 수 있음, 보통 build/libs 안에 jar가 생김)
echo "> 현재 구동중인 애플리케이션 pid 확인"

# 현재 실행 중인 자바 프로세스 ID 확인
CURRENT_PID=$(pgrep -f gitget) # 'gitget'은 jar 파일 이름의 일부여야 함. 잘 모르면 java로 변경

echo "$CURRENT_PID"

if [ -z $CURRENT_PID ]; then
    echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
    echo "> kill -15 $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep 5
fi

echo "> 새 애플리케이션 배포"

# jar 파일 찾기
JAR_NAME=$(ls -tr $REPOSITORY/build/libs/*.jar | tail -n 1)

echo "> JAR Name: $JAR_NAME"

echo "> 새 애플리케이션 실행"

# nohup으로 백그라운드 실행 (로그 남기기)
nohup java -jar \
    -Dspring.config.location=classpath:/application.yml,classpath:/application-prod.yml \
    $JAR_NAME > $REPOSITORY/nohup.out 2>&1 &