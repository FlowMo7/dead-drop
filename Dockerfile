FROM openjdk:22-jdk-bullseye

EXPOSE ${PORT:-8080}

RUN apt-get -y update && \
    apt-get install -y --no-install-recommends curl dumb-init

HEALTHCHECK CMD curl -f http://127.0.0.1:${PORT}/status || exit 1

COPY ./build/distributions/dead-drop-0.0.1.zip /usr/src/dead-drop-0.0.1.zip

RUN unzip /usr/src/dead-drop-0.0.1.zip -d /usr/src &&  \
    rm /usr/src/dead-drop-0.0.1.zip && \
    mv /usr/src/dead-drop-0.0.1 /var/dead-drop

WORKDIR /var/dead-drop

CMD ["dumb-init", "bin/dead-drop"]
