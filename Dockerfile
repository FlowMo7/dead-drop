FROM anapsix/alpine-java

COPY ./build/libs/DeadDrop.jar /DeadDrop.jar

CMD ["java", "-jar", "DeadDrop.jar"]
