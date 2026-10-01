import { candidatesSeed, enterprisesSeed, vacanciesSeed } from './seed';
import type { Candidate, Enterprise, PersonType, Vacancy } from './types';

const KEYS = {
  candidates: 'linketinder:candidates',
  enterprises: 'linketinder:enterprises',
  vacancies: 'linketinder:vacancies',
  session: 'linketinder:session',
  version: 'linketinder:version',
};

// Aumente quando o formato dos dados mudar, tudo é apagado e o seed é regravado
const VERSION = '3';

function read<T>(key: string): T[] {
  try {
    return JSON.parse(localStorage.getItem(key) ?? '[]') as T[];
  } catch {
    return [];
  }
}

function write<T>(key: string, items: T[]): void {
  localStorage.setItem(key, JSON.stringify(items));
}

if (localStorage.getItem(KEYS.version) !== VERSION) {
  Object.keys(localStorage)
    .filter((key) => key.startsWith('linketinder:'))
    .forEach((key) => localStorage.removeItem(key));
  write(KEYS.candidates, candidatesSeed);
  write(KEYS.enterprises, enterprisesSeed);
  write(KEYS.vacancies, vacanciesSeed);
  localStorage.setItem(KEYS.version, VERSION);
}

export const getCandidates = (): Candidate[] => read<Candidate>(KEYS.candidates);
export const getEnterprises = (): Enterprise[] => read<Enterprise>(KEYS.enterprises);
export const getVacancies = (): Vacancy[] => read<Vacancy>(KEYS.vacancies);

export function addCandidate(candidate: Candidate): void {
  write(KEYS.candidates, [...getCandidates(), candidate]);
}

export function addEnterprise(enterprise: Enterprise): void {
  write(KEYS.enterprises, [...getEnterprises(), enterprise]);
}

export function addVacancy(vacancy: Vacancy): void {
  write(KEYS.vacancies, [...getVacancies(), vacancy]);
}

export function removeCandidate(id: string): void {
  write(KEYS.candidates, getCandidates().filter((candidate) => candidate.id !== id));
}

export function removeEnterprise(id: string): void {
  write(KEYS.enterprises, getEnterprises().filter((enterprise) => enterprise.id !== id));
}

export function removeVacancy(id: string): void {
  write(KEYS.vacancies, getVacancies().filter((vacancy) => vacancy.id !== id));
}

export interface Session {
  type: PersonType;
  id: string;
}

export function getSession(): Session | null {
  try {
    return JSON.parse(localStorage.getItem(KEYS.session) ?? 'null') as Session | null;
  } catch {
    return null;
  }
}

export function setSession(session: Session): void {
  localStorage.setItem(KEYS.session, JSON.stringify(session));
}

export function clearSession(): void {
  localStorage.removeItem(KEYS.session);
}
