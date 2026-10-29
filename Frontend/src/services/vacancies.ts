import * as storage from '../data/storage';
import type { Competence, Enterprise, Vacancy, VacancyAnonymous } from '../types';

export interface NewVacancy {
  title: string;
  description: string;
  city: string;
  state: string;
  competences: Competence[];
}

function toAnonymousVacancy({ title, description, city, state, competences }: Vacancy): VacancyAnonymous {
  return { title, description, city, state, competences };
}

export function listOwnVacancies(enterprise: Enterprise): Vacancy[] {
  return storage.vacancies.getAll().filter((vacancy) => vacancy.enterpriseId === enterprise.id);
}

export function listOpenVacancies(): VacancyAnonymous[] {
  return storage.vacancies
    .getAll()
    .filter((vacancy) => vacancy.status === 'OPEN')
    .map(toAnonymousVacancy);
}

export function publishVacancy(enterprise: Enterprise, newVacancy: NewVacancy): void {
  storage.vacancies.add({ ...newVacancy, id: crypto.randomUUID(), enterpriseId: enterprise.id, status: 'OPEN' });
}

export function deleteOwnVacancy(enterprise: Enterprise, vacancyId: string): void {
  const isOwnVacancy = listOwnVacancies(enterprise).some((vacancy) => vacancy.id === vacancyId);
  if (isOwnVacancy) {
    storage.vacancies.remove(vacancyId);
  }
}
