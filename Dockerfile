FROM openjdk:8-jre-alpine

COPY ./build/libs/DeadDrop.jar /DeadDrop.jar

CMD ["java", "-jar", "DeadDrop.jar"]
