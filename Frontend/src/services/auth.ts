import * as storage from '../data/storage';
import { listOwnVacancies } from './vacancies';
import type { Account, Candidate, Enterprise } from '../types';

function allAccounts(): Account[] {
  return [...storage.candidates.getAll(), ...storage.enterprises.getAll()];
}

function isSameEmail(first: string, second: string): boolean {
  return first.toLowerCase() === second.toLowerCase();
}

function startSession(account: Account): void {
  storage.session.set({ type: account.type, id: account.id });
}

function removeEnterpriseWithVacancies(enterprise: Enterprise): void {
  listOwnVacancies(enterprise).forEach((vacancy) => storage.vacancies.remove(vacancy.id));
  storage.enterprises.remove(enterprise.id);
}

export function isEmailInUse(email: string): boolean {
  return allAccounts().some((account) => isSameEmail(account.email, email));
}

export function register(account: Account): void {
  if (account.type === 'CANDIDATE') {
    storage.candidates.add(account);
  } else {
    storage.enterprises.add(account);
  }
  startSession(account);
}

export function login(email: string, password: string): Account | undefined {
  const account = allAccounts().find((existing) => isSameEmail(existing.email, email) && existing.password === password);
  if (account) {
    startSession(account);
  }
  return account;
}

export function logout(): void {
  storage.session.clear();
}

export function getLoggedAccount(): Account | undefined {
  const current = storage.session.get();
  if (!current) {
    return undefined;
  }
  return allAccounts().find((account) => account.type === current.type && account.id === current.id);
}

export function getLoggedCandidate(): Candidate | undefined {
  const account = getLoggedAccount();
  return account?.type === 'CANDIDATE' ? account : undefined;
}

export function getLoggedEnterprise(): Enterprise | undefined {
  const account = getLoggedAccount();
  return account?.type === 'ENTERPRISE' ? account : undefined;
}

export function deleteLoggedAccount(): void {
  const account = getLoggedAccount();
  if (account?.type === 'CANDIDATE') {
    storage.candidates.remove(account.id);
  }
  if (account?.type === 'ENTERPRISE') {
    removeEnterpriseWithVacancies(account);
  }
  logout();
}
