FROM anapsix/alpine-java
MAINTAINER Florian Moetz <florian@moetz.co.at>

COPY ./build/libs/DeadDrop.jar /DeadDrop.jar

CMD ["java", "-jar", "DeadDrop.jar"]
