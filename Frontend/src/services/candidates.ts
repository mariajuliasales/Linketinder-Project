import * as storage from '../data/storage';
import { getLoggedEnterprise } from './auth';
import type { Candidate, CandidateAnonymous, CandidateCard, Competence, MatchedVacancy } from '../types';

export type CompetenceCount = [Competence, number];

export function listAnonymousCandidates(): CandidateAnonymous[] {
  return storage.candidates
    .getAll()
    .map(({ training, description, competences }) => ({ training, description, competences }));
}

// Só as vagas de E com match com C (id, título e status). Vagas curtidas sem match não entram.
function matchedVacanciesOf(candidateId: string, enterpriseId: string): MatchedVacancy[] {
  const matchedVacancyIds = new Set(
    storage.matches
      .getAll()
      .filter((match) => match.candidateId === candidateId)
      .map((match) => match.vacancyId),
  );
  return storage.vacancies
    .getAll()
    .filter((vacancy) => vacancy.enterpriseId === enterpriseId && matchedVacancyIds.has(vacancy.id))
    .map(({ id, title, status }) => ({ id, title, status }));
}

// Montado campo a campo: sem cpf, birthDate, address nem password. reveal só existe com match entre C e E.
function toCandidateCard(candidate: Candidate, enterpriseId: string, likedByMe: boolean): CandidateCard {
  const { id, training, description, competences } = candidate;
  const card: CandidateCard = { id, training, description, competences, likedByMe };
  const matchedVacancies = matchedVacanciesOf(id, enterpriseId);
  if (matchedVacancies.length > 0) {
    const { name, email } = candidate;
    card.reveal = { name, email, matchedVacancies };
  }
  return card;
}

function toCandidateCards(enterpriseId: string, likedCandidateIds: Set<string>): CandidateCard[] {
  return storage.candidates
    .getAll()
    .map((candidate) => toCandidateCard(candidate, enterpriseId, likedCandidateIds.has(candidate.id)));
}

// O recrutador vem da sessão, nunca do chamador, e a empresa vem dele: o reveal nunca usa matches de outra empresa.
// likedByMe vem só das curtidas desse recrutador.
export function listCandidateCardsForRecruiter(): CandidateCard[] {
  const current = storage.session.get();
  if (current?.type !== 'RECRUITER') {
    throw new Error('Nenhum recrutador logado');
  }
  const recruiter = storage.recruiters.getAll().find((existing) => existing.id === current.id);
  if (!recruiter) {
    throw new Error(`Recrutador com ID ${current.id} não existe`);
  }
  const likedCandidateIds = new Set(
    storage.recruiterLikes
      .getAll()
      .filter((like) => like.recruiterId === recruiter.id)
      .map((like) => like.candidateId),
  );
  return toCandidateCards(recruiter.enterpriseId, likedCandidateIds);
}

// A empresa vem da sessão, nunca do chamador. A empresa não curte, então likedByMe é sempre false.
export function listCandidateCardsForEnterprise(): CandidateCard[] {
  const enterprise = getLoggedEnterprise();
  if (!enterprise) {
    throw new Error('Nenhuma empresa logada');
  }
  return toCandidateCards(enterprise.id, new Set());
}

export function rankCompetencesByCandidates(candidates: CandidateAnonymous[]): CompetenceCount[] {
  const candidatesByCompetence = new Map<Competence, number>();
  candidates
    .flatMap((candidate) => candidate.competences)
    .forEach((competence) => {
      candidatesByCompetence.set(competence, (candidatesByCompetence.get(competence) ?? 0) + 1);
    });
  return [...candidatesByCompetence].sort(([, first], [, second]) => second - first);
}
