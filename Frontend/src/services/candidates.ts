import * as storage from '../data/storage';
import type { CandidateAnonymous, Competence } from '../types';

export type CompetenceCount = [Competence, number];

export function listAnonymousCandidates(): CandidateAnonymous[] {
  return storage.candidates
    .getAll()
    .map(({ training, description, competences }) => ({ training, description, competences }));
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
