import { candidatesSeed, enterprisesSeed, vacanciesSeed } from './seed';
import type { Candidate, Enterprise, PersonType, Vacancy } from '../types';

const KEY_PREFIX = 'linketinder:';
const KEYS = {
  candidates: `${KEY_PREFIX}candidates`,
  enterprises: `${KEY_PREFIX}enterprises`,
  vacancies: `${KEY_PREFIX}vacancies`,
  session: `${KEY_PREFIX}session`,
  dataFormatVersion: `${KEY_PREFIX}version`,
};
const DATA_FORMAT_VERSION = '4';

export interface Session {
  type: PersonType;
  id: string;
}

interface Collection<T extends { id: string }> {
  getAll(): T[];
  add(item: T): void;
  remove(id: string): void;
}

function readJson<T>(key: string, fallback: T): T {
  try {
    const stored = localStorage.getItem(key);
    return stored === null ? fallback : (JSON.parse(stored) as T);
  } catch {
    return fallback;
  }
}

function writeJson(key: string, value: unknown): void {
  localStorage.setItem(key, JSON.stringify(value));
}

function createCollection<T extends { id: string }>(key: string): Collection<T> {
  const getAll = (): T[] => readJson<T[]>(key, []);
  return {
    getAll,
    add: (item) => writeJson(key, [...getAll(), item]),
    remove: (id) => writeJson(key, getAll().filter((item) => item.id !== id)),
  };
}

export const candidates = createCollection<Candidate>(KEYS.candidates);
export const enterprises = createCollection<Enterprise>(KEYS.enterprises);
export const vacancies = createCollection<Vacancy>(KEYS.vacancies);

export const session = {
  get: (): Session | null => readJson<Session | null>(KEYS.session, null),
  set: (current: Session): void => writeJson(KEYS.session, current),
  clear: (): void => localStorage.removeItem(KEYS.session),
};

export function resetStorageIfOutdated(): void {
  if (localStorage.getItem(KEYS.dataFormatVersion) === DATA_FORMAT_VERSION) {
    return;
  }
  Object.keys(localStorage)
    .filter((key) => key.startsWith(KEY_PREFIX))
    .forEach((key) => localStorage.removeItem(key));
  writeJson(KEYS.candidates, candidatesSeed);
  writeJson(KEYS.enterprises, enterprisesSeed);
  writeJson(KEYS.vacancies, vacanciesSeed);
  localStorage.setItem(KEYS.dataFormatVersion, DATA_FORMAT_VERSION);
}
