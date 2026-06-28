import { CurrentUser } from './current-user';

export function canViewSensitivePatientData(user: CurrentUser) {
  return ['admin', 'doctor', 'research_assistant'].includes(user.role);
}

export function maskName(value?: string | null) {
  if (!value) {
    return value;
  }
  return `${value.slice(0, 1)}**`;
}

export function maskPhone(value?: string | null) {
  if (!value || value.length < 7) {
    return value ? '***' : value;
  }
  return `${value.slice(0, 3)}****${value.slice(-4)}`;
}

export function maskIdentityNo(value?: string | null) {
  if (!value || value.length < 10) {
    return value ? '******' : value;
  }
  return `${value.slice(0, 6)}********${value.slice(-4)}`;
}

