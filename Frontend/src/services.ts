import * as storage from './storage';
import type { Candidate, CandidateAnonymous, Competence, Enterprise, PersonType, Vacancy, VacancyAnonymous } from './types';

export const HOME_BY_TYPE: Record<PersonType, string> = {
  CANDIDATE: '#/vacancies',
  ENTERPRISE: '#/candidates',
};

function allPeople(): (Candidate | Enterprise)[] {
  return [...storage.getCandidates(), ...storage.getEnterprises()];
}

/** person.email é unique no banco*/
export function emailInUse(email: string): boolean {
  return allPeople().some((person) => person.email.toLowerCase() === email.toLowerCase());
}

export function register(person: Candidate | Enterprise): void {
  if (person.type === 'CANDIDATE') storage.addCandidate(person);
  else storage.addEnterprise(person);
  storage.setSession({ type: person.type, id: person.id });
}

export function login(email: string, password: string): Candidate | Enterprise | undefined {
  const person = allPeople().find(
    (p) => p.email.toLowerCase() === email.toLowerCase() && p.password === password,
  );
  if (person) storage.setSession({ type: person.type, id: person.id });
  return person;
}

export function logout(): void {
  storage.clearSession();
}

/** A empresa leva junto suas vagas como o ON DELETE CASCADE do banco. */
export function deleteAccount(): void {
  const candidate = getLoggedCandidate();
  const enterprise = getLoggedEnterprise();
  if (candidate) storage.removeCandidate(candidate.id);
  if (enterprise) {
    storage.getVacancies()
      .filter((vacancy) => vacancy.enterpriseId === enterprise.id)
      .forEach((vacancy) => storage.removeVacancy(vacancy.id));
    storage.removeEnterprise(enterprise.id);
  }
  storage.clearSession();
}

export interface NewVacancy {
  title: string;
  description: string;
  city: string;
  state: string;
  competencies: Competence[];
}

export function createVacancy(enterprise: Enterprise, data: NewVacancy): void {
  storage.addVacancy({ ...data, id: crypto.randomUUID(), enterpriseId: enterprise.id, status: 'OPEN' });
}

export function deleteVacancy(enterprise: Enterprise, vacancyId: string): void {
  const vacancy = storage.getVacancies().find((v) => v.id === vacancyId);
  if (vacancy?.enterpriseId === enterprise.id) storage.removeVacancy(vacancyId);
}

/** Sem anonimizar: são as vagas da própria empresa. */
export function listMyVacancies(enterprise: Enterprise): Vacancy[] {
  return storage.getVacancies().filter((vacancy) => vacancy.enterpriseId === enterprise.id);
}

export function getLoggedCandidate(): Candidate | undefined {
  const session = storage.getSession();
  if (session?.type !== 'CANDIDATE') return undefined;
  return storage.getCandidates().find((c) => c.id === session.id);
}

export function getLoggedEnterprise(): Enterprise | undefined {
  const session = storage.getSession();
  if (session?.type !== 'ENTERPRISE') return undefined;
  return storage.getEnterprises().find((e) => e.id === session.id);
}

export function listVacancies(): VacancyAnonymous[] {
  return storage
    .getVacancies()
    .filter((vacancy) => vacancy.status === 'OPEN')
    .map(({ title, description, city, state, competencies }) => ({ title, description, city, state, competencies }));
}

export function listCandidates(): CandidateAnonymous[] {
  return storage
    .getCandidates()
    .map(({ training, description, competencies }) => ({ training, description, competencies }));
}

export function getStats(): { vacancies: number; candidates: number; enterprises: number } {
  return {
    vacancies: listVacancies().length,
    candidates: storage.getCandidates().length,
    enterprises: storage.getEnterprises().length,
  };
}

/** Quantos candidatos têm cada competência do maior para o menor. */
export function countByCompetence(candidates: CandidateAnonymous[]): [string, number][] {
  const count = new Map<string, number>();
  for (const candidate of candidates) {
    for (const competence of candidate.competencies) {
      count.set(competence, (count.get(competence) ?? 0) + 1);
    }
  }
  return [...count].sort((a, b) => b[1] - a[1]);
}
