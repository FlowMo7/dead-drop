FROM openjdk:8-jre-alpine

RUN apk --no-cache add curl
HEALTHCHECK CMD curl -f http://0.0.0.0:8080 || exit 1

COPY ./build/libs/DeadDrop.jar /DeadDrop.jar

CMD ["java", "-jar", "DeadDrop.jar"]
