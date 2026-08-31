FROM node:22-slim

RUN apt-get update \
    && apt-get install -y --no-install-recommends chromium \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /e2e

COPY package.json ./
ENV PUPPETEER_SKIP_DOWNLOAD=true
RUN npm install

COPY e2e.test.mjs ./

ENV E2E_BASE_URL=http://localhost:8080
ENV PUPPETEER_EXECUTABLE_PATH=/usr/bin/chromium

CMD ["npm", "test"]
