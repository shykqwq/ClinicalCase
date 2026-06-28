# Clinical Case Backend

NestJS backend for the breast surgery clinical case and research follow-up P0 app.

## Stack

- NestJS
- PostgreSQL
- Prisma
- JWT
- Object storage signed URLs

## Setup

```bash
npm install
cp .env.example .env
npm run prisma:generate
npm run start:dev
```

## Database

The first SQL migration is:

```text
database/migrations/001_init_p0.sql
```

Run it with:

```bash
npm run db:migrate:sql
```

### Local PostgreSQL with Docker

```bash
docker compose up -d postgres
docker exec -i clinical-case-postgres psql -U postgres -d clinical_case < database/migrations/001_init_p0.sql
npm run db:seed
```

Default seeded admin:

```text
username: admin
password: ChangeMe123
```

## API Base Path

```text
/api/v1
```

## Cloud Deployment

Production Docker deployment files are included:

- `Dockerfile`
- `docker-compose.prod.yml`
- `.env.production.example`

See:

```text
../docs/backend-cloud-deployment.md
```
