FROM openjdk:19-jdk-alpine3.16

EXPOSE ${PORT:-8080}

RUN apk --no-cache add curl
HEALTHCHECK CMD curl -f http://127.0.0.1:${PORT}/status || exit 1

COPY ./build/libs/DeadDrop.jar /DeadDrop.jar

CMD ["java", "-jar", "DeadDrop.jar"]
