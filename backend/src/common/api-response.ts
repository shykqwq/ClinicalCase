export function ok<T>(data: T, message = 'ok') {
  return { success: true, data, message };
}

export function page<T>(items: T[], pageNumber: number, pageSize: number, total: number) {
  return ok({ items, page: pageNumber, pageSize, total });
}

