export type UserRole = 'admin' | 'doctor' | 'research_assistant' | 'readonly';

export interface CurrentUser {
  id: string;
  departmentId: string;
  role: UserRole;
  displayName: string;
}

