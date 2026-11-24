import * as storage from '../data/storage';
import type {
  Candidate,
  Competence,
  Enterprise,
  LikedVacancyCard,
  Recruiter,
  Vacancy,
  VacancyAnonymous,
  VacancyCard,
} from '../types';

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

// Montado campo a campo: sem enterpriseId nem qualquer dado da empresa.
function toVacancyCard({ id, title, description, city, state, competences, status }: Vacancy): VacancyCard {
  return { id, title, description, city, state, competences, status };
}

function likedVacancyIdsOf(candidate: Candidate): string[] {
  return storage.candidateLikes
    .getAll()
    .filter((like) => like.candidateId === candidate.id)
    .map((like) => like.vacancyId);
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

// Lista principal do candidato: só vagas OPEN, com a marca da própria curtida.
export function listOpenVacancyCards(candidate: Candidate): VacancyCard[] {
  const likedVacancyIds = new Set(likedVacancyIdsOf(candidate));
  return storage.vacancies
    .getAll()
    .filter((vacancy) => vacancy.status === 'OPEN')
    .map((vacancy) => ({ ...toVacancyCard(vacancy), likedByMe: likedVacancyIds.has(vacancy.id) }));
}

// "Minhas curtidas": vagas curtidas pelo candidato em qualquer status. Nome e e-mail da empresa só na vaga com match;
// nunca enterpriseId nem dado de recrutador.
export function listLikedVacancyCards(candidate: Candidate): LikedVacancyCard[] {
  const likedVacancyIds = new Set(likedVacancyIdsOf(candidate));
  const matchedVacancyIds = new Set(
    storage.matches
      .getAll()
      .filter((match) => match.candidateId === candidate.id)
      .map((match) => match.vacancyId),
  );
  return storage.vacancies
    .getAll()
    .filter((vacancy) => likedVacancyIds.has(vacancy.id))
    .map((vacancy) => {
      const card: LikedVacancyCard = toVacancyCard(vacancy);
      if (matchedVacancyIds.has(vacancy.id)) {
        const enterprise = storage.enterprises.getAll().find((existing) => existing.id === vacancy.enterpriseId);
        if (enterprise) {
          const { name, email } = enterprise;
          card.enterprise = { name, email };
        }
      }
      return card;
    });
}

// Somente leitura: todas as vagas da empresa do recrutador, em qualquer status, sem likedByMe.
export function listEnterpriseVacanciesForRecruiter(recruiter: Recruiter): VacancyCard[] {
  return storage.vacancies
    .getAll()
    .filter((vacancy) => vacancy.enterpriseId === recruiter.enterpriseId)
    .map(toVacancyCard);
}
