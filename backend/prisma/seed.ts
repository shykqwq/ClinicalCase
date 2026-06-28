import { PrismaClient } from '@prisma/client';
import * as bcrypt from 'bcrypt';

const prisma = new PrismaClient();

async function main() {
  const department = await prisma.department.upsert({
    where: { id: process.env.SEED_DEPARTMENT_ID ?? '00000000-0000-0000-0000-000000000001' },
    update: {},
    create: {
      id: process.env.SEED_DEPARTMENT_ID ?? '00000000-0000-0000-0000-000000000001',
      name: process.env.SEED_DEPARTMENT_NAME ?? '乳腺外科',
      hospitalName: process.env.SEED_HOSPITAL_NAME,
      status: 'active',
    },
  });

  const username = process.env.SEED_ADMIN_USERNAME ?? 'admin';
  const password = process.env.SEED_ADMIN_PASSWORD ?? 'ChangeMe123';
  const passwordHash = await bcrypt.hash(password, 12);

  await prisma.user.upsert({
    where: {
      departmentId_username: {
        departmentId: department.id,
        username,
      },
    },
    update: {},
    create: {
      departmentId: department.id,
      username,
      passwordHash,
      displayName: process.env.SEED_ADMIN_DISPLAY_NAME ?? '系统管理员',
      role: 'admin',
      status: 'active',
    },
  });
}

main()
  .catch((error) => {
    console.error(error);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });

