import type { Competence } from '../types';

export function countMatchingCompetences(required: Competence[], owned: Competence[]): number {
  return required.filter((competence) => owned.includes(competence)).length;
}
