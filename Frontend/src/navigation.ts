import type { PersonType } from './types';

export const ROUTES = {
  home: '#/home',
  register: '#/register',
  registerEnterprise: '#/register?type=ENTERPRISE',
  login: '#/login',
  vacancies: '#/vacancies',
  candidates: '#/candidates',
  myVacancies: '#/my-vacancies',
} as const;

export const HOME_BY_TYPE: Record<PersonType, string> = {
  CANDIDATE: ROUTES.vacancies,
  ENTERPRISE: ROUTES.candidates,
};

export function navigateTo(route: string): void {
  location.hash = route;
}

export function currentPath(): string {
  return location.hash.split('?')[0];
}

export function currentQuery(): URLSearchParams {
  return new URLSearchParams(location.hash.split('?')[1] ?? '');
}

export function rerender(): void {
  window.dispatchEvent(new HashChangeEvent('hashchange'));
}
