FROM node:20-bookworm-slim

WORKDIR /app

RUN apt-get update \
  && apt-get install -y --no-install-recommends openssl postgresql-client wget \
  && rm -rf /var/lib/apt/lists/*

COPY package*.json ./
RUN npm ci

COPY prisma ./prisma
COPY database ./database
COPY nest-cli.json tsconfig*.json ./
COPY src ./src
COPY scripts ./scripts

RUN npm run prisma:generate
RUN npm run build
RUN chmod +x scripts/docker-entrypoint.sh

ENV NODE_ENV=production

EXPOSE 3001

CMD ["scripts/docker-entrypoint.sh"]
