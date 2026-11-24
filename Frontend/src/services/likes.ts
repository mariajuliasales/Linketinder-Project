import * as storage from '../data/storage';
import { removeWhere } from '../data/storage';
import type { Candidate, Recruiter, Vacancy } from '../types';

// Espelha CandidateLikeService e RecruiterLikeService do back-end, com as mesmas mensagens de erro.
// Invariante: enterpriseLike(E, C) existe se e somente se algum recrutador de E curte C.
// Sem transação no localStorage: cada função valida tudo antes da primeira gravação.
// Nenhuma função devolve dados: só grava ou lança Error com a mensagem.

const OPEN_ONLY_MESSAGE = 'Só é possível curtir vagas abertas';
const SELF_LIKE_MESSAGE = 'Você não pode curtir o seu próprio perfil de candidato.';

function notFound(entityName: string, id: string): Error {
  return new Error(`${entityName} com ID ${id} não existe`);
}

function onlyDigits(value: string): string {
  return value.replace(/\D/g, '');
}

function requireCandidate(candidateId: string): Candidate {
  const candidate = storage.candidates.getAll().find((existing) => existing.id === candidateId);
  if (!candidate) {
    throw notFound('Candidato', candidateId);
  }
  return candidate;
}

function requireRecruiter(recruiterId: string): Recruiter {
  const recruiter = storage.recruiters.getAll().find((existing) => existing.id === recruiterId);
  if (!recruiter) {
    throw notFound('Recrutador', recruiterId);
  }
  return recruiter;
}

function findVacancy(vacancyId: string): Vacancy | undefined {
  return storage.vacancies.getAll().find((vacancy) => vacancy.id === vacancyId);
}

function enterpriseLikes(enterpriseId: string, candidateId: string): boolean {
  return storage.enterpriseLikes
    .getAll()
    .some((like) => like.enterpriseId === enterpriseId && like.candidateId === candidateId);
}

function addMatchIfAbsent(candidateId: string, vacancyId: string): void {
  const exists = storage.matches
    .getAll()
    .some((match) => match.candidateId === candidateId && match.vacancyId === vacancyId);
  if (!exists) {
    storage.matches.add({ id: crypto.randomUUID(), candidateId, vacancyId });
  }
}

function sameCpf(recruiter: Recruiter, candidate: Candidate): boolean {
  const recruiterCpf = onlyDigits(recruiter.cpf);
  return recruiterCpf !== '' && recruiterCpf === onlyDigits(candidate.cpf);
}

// Só vaga OPEN. Idempotente. Se E já curte C, cria o match (C, V).
export function likeVacancy(candidateId: string, vacancyId: string): void {
  requireCandidate(candidateId);
  const vacancy = findVacancy(vacancyId);
  if (!vacancy) {
    throw notFound('Vaga', vacancyId);
  }
  if (vacancy.status !== 'OPEN') {
    throw new Error(OPEN_ONLY_MESSAGE);
  }

  const alreadyLiked = storage.candidateLikes
    .getAll()
    .some((like) => like.candidateId === candidateId && like.vacancyId === vacancyId);
  if (!alreadyLiked) {
    storage.candidateLikes.add({ id: crypto.randomUUID(), candidateId, vacancyId });
  }
  if (enterpriseLikes(vacancy.enterpriseId, candidateId)) {
    addMatchIfAbsent(candidateId, vacancyId);
  }
}

// Qualquer status da vaga. Idempotente. Apaga só o match (C, V), como a FK em cascata do banco.
export function unlikeVacancy(candidateId: string, vacancyId: string): void {
  removeWhere(storage.matches, (match) => match.candidateId === candidateId && match.vacancyId === vacancyId);
  removeWhere(storage.candidateLikes, (like) => like.candidateId === candidateId && like.vacancyId === vacancyId);
}

// Idempotente. Na primeira curtida de E em C, grava o enterpriseLike e cria o match com cada vaga OPEN de E que C
// curtiu. A área do recrutador não filtra nada.
export function likeCandidate(recruiterId: string, candidateId: string): void {
  const recruiter = requireRecruiter(recruiterId);
  const candidate = requireCandidate(candidateId);
  if (sameCpf(recruiter, candidate)) {
    throw new Error(SELF_LIKE_MESSAGE);
  }
  const { enterpriseId } = recruiter;
  const enterpriseAlreadyLikes = enterpriseLikes(enterpriseId, candidateId);

  const alreadyLiked = storage.recruiterLikes
    .getAll()
    .some((like) => like.recruiterId === recruiterId && like.candidateId === candidateId);
  if (!alreadyLiked) {
    storage.recruiterLikes.add({ id: crypto.randomUUID(), recruiterId, candidateId });
  }
  if (enterpriseAlreadyLikes) {
    return;
  }

  storage.enterpriseLikes.add({ id: crypto.randomUUID(), enterpriseId, candidateId });
  const openVacancyIds = new Set(
    storage.vacancies
      .getAll()
      .filter((vacancy) => vacancy.enterpriseId === enterpriseId && vacancy.status === 'OPEN')
      .map((vacancy) => vacancy.id),
  );
  storage.candidateLikes
    .getAll()
    .filter((like) => like.candidateId === candidateId && openVacancyIds.has(like.vacancyId))
    .forEach((like) => addMatchIfAbsent(candidateId, like.vacancyId));
}

// Idempotente. Se nenhum outro recrutador de E curte C ("saiu o último"), apaga o enterpriseLike(E, C) e os matches
// de C com todas as vagas de E.
export function unlikeCandidate(recruiterId: string, candidateId: string): void {
  const recruiter = requireRecruiter(recruiterId);
  const isOwnLike = (like: { recruiterId: string; candidateId: string }) =>
    like.recruiterId === recruiterId && like.candidateId === candidateId;
  if (!storage.recruiterLikes.getAll().some(isOwnLike)) {
    return;
  }
  removeWhere(storage.recruiterLikes, isOwnLike);

  const { enterpriseId } = recruiter;
  const colleagueIds = new Set(
    storage.recruiters
      .getAll()
      .filter((existing) => existing.enterpriseId === enterpriseId && existing.id !== recruiterId)
      .map((existing) => existing.id),
  );
  const colleagueStillLikes = storage.recruiterLikes
    .getAll()
    .some((like) => like.candidateId === candidateId && colleagueIds.has(like.recruiterId));
  if (colleagueStillLikes) {
    return;
  }

  const enterpriseVacancyIds = new Set(
    storage.vacancies
      .getAll()
      .filter((vacancy) => vacancy.enterpriseId === enterpriseId)
      .map((vacancy) => vacancy.id),
  );
  removeWhere(storage.enterpriseLikes, (like) => like.enterpriseId === enterpriseId && like.candidateId === candidateId);
  removeWhere(
    storage.matches,
    (match) => match.candidateId === candidateId && enterpriseVacancyIds.has(match.vacancyId),
  );
}
