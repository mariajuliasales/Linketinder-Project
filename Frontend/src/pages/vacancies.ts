import { requireLoggedCandidate } from './guards';
import { listOpenVacancies } from '../services/vacancies';
import { countMatchingCompetences } from '../services/matching';
import { card, createElement } from '../ui/dom';
import { competenceBadges, lockedInfo } from '../ui/badges';
import { attachHoverCard } from '../ui/hoverCard';
import type { Candidate, VacancyAnonymous } from '../types';

function formatLocation(vacancy: VacancyAnonymous): string {
  return `${vacancy.city} — ${vacancy.state}`;
}

function describeVacancy(vacancy: VacancyAnonymous, candidate: Candidate): HTMLElement {
  const matches = countMatchingCompetences(vacancy.competences, candidate.competences);
  const details = createElement('div');
  details.append(
    createElement('p', 'fw-semibold mb-1', vacancy.title),
    createElement('p', 'mb-1', `Local: ${formatLocation(vacancy)}`),
    createElement('p', 'mb-2', vacancy.description),
    createElement('p', 'mb-1', `Você tem ${matches} de ${vacancy.competences.length} competências pedidas:`),
    ...competenceBadges(vacancy.competences, candidate.competences),
    lockedInfo('Nome e dados da empresa revelados após o match'),
  );
  return details;
}

function vacancyCard(vacancy: VacancyAnonymous, candidate: Candidate): HTMLElement {
  const vacancyInfo = card([
    createElement('h2', 'h5 card-title', vacancy.title),
    createElement('p', 'small text-body-secondary mb-2', formatLocation(vacancy)),
    createElement('p', 'card-text', vacancy.description),
    ...competenceBadges(vacancy.competences, candidate.competences),
  ], { card: 'h-100' });
  attachHoverCard(vacancyInfo, () => describeVacancy(vacancy, candidate));
  const column = createElement('div', 'col');
  column.append(vacancyInfo);
  return column;
}

export function vacanciesPage(): HTMLElement {
  const page = createElement('div');
  const candidate = requireLoggedCandidate();
  if (!candidate) {
    return page;
  }

  const vacancies = listOpenVacancies();
  const grid = createElement('div', 'row row-cols-1 row-cols-md-2 row-cols-lg-3 g-3');
  grid.append(...vacancies.map((vacancy) => vacancyCard(vacancy, candidate)));
  page.append(
    createElement('h1', 'h3', `Olá, ${candidate.name}`),
    createElement('p', 'text-body-secondary', `${vacancies.length} vagas abertas. Em verde, as competências que você tem.`),
    grid,
  );
  return page;
}
