FROM anapsix/alpine-java
MAINTAINER Florian Moetz <florian@moetz.co.at>

RUN apk add  --no-cache ffmpeg

COPY ./build/libs/DeadDrop.jar /DeadDrop.jar

CMD ["java", "-jar", "DeadDrop.jar"]
