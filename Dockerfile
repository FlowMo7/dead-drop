FROM openjdk:8-jre-alpine

EXPOSE ${PORT:-8080}

RUN apk --no-cache add curl
HEALTHCHECK CMD curl -f http://0.0.0.0:${PORT} || exit 1

COPY ./build/libs/DeadDrop.jar /DeadDrop.jar

CMD ["java", "-jar", "DeadDrop.jar"]
