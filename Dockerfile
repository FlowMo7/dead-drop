FROM alpine/java:22-jdk

RUN apk add --no-cache curl dumb-init

HEALTHCHECK CMD curl -f http://127.0.0.1:${PORT}/status || exit 1

COPY ./build/distributions/dead-drop.zip /usr/src/app.zip

RUN unzip /usr/src/app.zip -d /usr/src &&  \
    rm /usr/src/app.zip && \
    mv /usr/src/dead-drop /var/dead-drop

WORKDIR /var/dead-drop

EXPOSE ${PORT:-8080}

CMD ["dumb-init", "bin/dead-drop"]
