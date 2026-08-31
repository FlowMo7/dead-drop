FROM eclipse-temurin:21-jre AS jre

FROM node:22-slim

COPY --from=jre /opt/java/openjdk /opt/java/openjdk
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH=/opt/java/openjdk/bin:$PATH

RUN apt-get update \
    && apt-get install -y --no-install-recommends chromium dumb-init unzip \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /e2e

COPY e2e/package.json ./
ENV PUPPETEER_SKIP_DOWNLOAD=true
RUN npm install

COPY e2e/e2e.test.mjs e2e/entrypoint.sh ./
RUN chmod +x /e2e/entrypoint.sh

COPY build/distributions/dead-drop.zip /tmp/dead-drop.zip
RUN unzip /tmp/dead-drop.zip -d /var && \
    rm /tmp/dead-drop.zip && \
    mv /var/dead-drop /var/dead-drop-app

ENV E2E_BASE_URL=http://localhost:8080
ENV E2E_LANGUAGE=en
ENV PUPPETEER_EXECUTABLE_PATH=/usr/bin/chromium
ENV PORT=8080
ENV DOMAIN=localhost:8080
ENV IS_HTTPS=false

CMD ["/e2e/entrypoint.sh"]
