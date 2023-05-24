FROM adoptopenjdk/openjdk11:alpine-jre

ARG APP_NAME="user-service"
ARG APP_VERSION="0.0.1-SNAPSHOT"
ARG JAR_FILE="target/${APP_NAME}-${APP_VERSION}.jar"

WORKDIR /opt/app
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]