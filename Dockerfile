FROM eclipse-temurin:21-jdk-alpine AS jre-build

RUN "$JAVA_HOME/bin/jlink" \
    --add-modules java.base,java.desktop,java.instrument,java.logging,java.management,java.naming,jdk.unsupported,jdk.crypto.ec \
    --strip-debug --no-man-pages --no-header-files --compress=2 \
    --output /javaruntime


FROM alpine:3.20

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH=$JAVA_HOME/bin:$PATH
COPY --from=jre-build /javaruntime $JAVA_HOME

RUN apk add --no-cache curl dumb-init

HEALTHCHECK CMD curl -f http://127.0.0.1:${PORT:-8080}/status || exit 1

COPY ./build/distributions/dead-drop.zip /usr/src/app.zip

RUN unzip /usr/src/app.zip -d /usr/src &&  \
    rm /usr/src/app.zip && \
    mv /usr/src/dead-drop /var/dead-drop

WORKDIR /var/dead-drop

EXPOSE ${PORT:-8080}

CMD ["dumb-init", "bin/dead-drop"]
