import * as storage from '../data/storage';
import { removeWhere } from '../data/storage';
import { listOwnVacancies } from './vacancies';
import type { Account, Candidate, Enterprise, Recruiter } from '../types';

function allAccounts(): Account[] {
  return [...storage.candidates.getAll(), ...storage.enterprises.getAll(), ...storage.recruiters.getAll()];
}

function normalizeEmail(email: string): string {
  return email.trim().toLowerCase();
}

function isSameEmail(first: string, second: string): boolean {
  return normalizeEmail(first) === normalizeEmail(second);
}

function startSession(account: Account): void {
  storage.session.set({ type: account.type, id: account.id });
}

// Espelha o ON DELETE CASCADE do banco: recrutadores, curtidas e matches ligados à empresa e às vagas dela
function removeEnterpriseWithDependents(enterprise: Enterprise): void {
  const vacancyIds = new Set(listOwnVacancies(enterprise).map((vacancy) => vacancy.id));
  const recruiterIds = new Set(
    storage.recruiters.getAll()
      .filter((recruiter) => recruiter.enterpriseId === enterprise.id)
      .map((recruiter) => recruiter.id),
  );
  removeWhere(storage.matches, (match) => vacancyIds.has(match.vacancyId));
  removeWhere(storage.candidateLikes, (like) => vacancyIds.has(like.vacancyId));
  removeWhere(storage.recruiterLikes, (like) => recruiterIds.has(like.recruiterId));
  removeWhere(storage.enterpriseLikes, (like) => like.enterpriseId === enterprise.id);
  removeWhere(storage.recruiters, (recruiter) => recruiterIds.has(recruiter.id));
  removeWhere(storage.vacancies, (vacancy) => vacancyIds.has(vacancy.id));
  storage.enterprises.remove(enterprise.id);
}

// Espelha o ON DELETE CASCADE do banco: curtidas feitas e recebidas e matches do candidato
function removeCandidateWithDependents(candidate: Candidate): void {
  removeWhere(storage.matches, (match) => match.candidateId === candidate.id);
  removeWhere(storage.candidateLikes, (like) => like.candidateId === candidate.id);
  removeWhere(storage.recruiterLikes, (like) => like.candidateId === candidate.id);
  removeWhere(storage.enterpriseLikes, (like) => like.candidateId === candidate.id);
  storage.candidates.remove(candidate.id);
}

export function isEmailInUse(email: string): boolean {
  return allAccounts().some((account) => isSameEmail(account.email, email));
}

// Só candidato e empresa: o recrutador é cadastrado pela empresa, sem iniciar sessão
export function register(account: Candidate | Enterprise): void {
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

export function getLoggedRecruiter(): Recruiter | undefined {
  const account = getLoggedAccount();
  return account?.type === 'RECRUITER' ? account : undefined;
}

export function deleteLoggedAccount(): void {
  const account = getLoggedAccount();
  // O recrutador não exclui a própria conta: quem o remove é a empresa (T27)
  if (account?.type === 'RECRUITER') {
    throw new Error('Recrutador não pode excluir a própria conta.');
  }
  if (account?.type === 'CANDIDATE') {
    removeCandidateWithDependents(account);
  }
  if (account?.type === 'ENTERPRISE') {
    removeEnterpriseWithDependents(account);
  }
  logout();
}
