#!/bin/sh
set -e

echo "Waiting for database..."
until psql "$DATABASE_URL" -c "select 1" >/dev/null 2>&1; do
  sleep 2
done

if [ "$(psql "$DATABASE_URL" -tAc "select to_regclass('public.departments')")" = "" ]; then
  echo "Initializing database schema..."
  npm run db:migrate:sql
else
  echo "Database schema already exists, skip initialization."
fi

echo "Seeding default department/admin if needed..."
npm run db:seed

echo "Starting API..."
exec npm run start:prod
