import { getLoggedCandidate, listVacancies } from '../services';
import { badge, el, hoverCard, lockedInfo } from '../dom';
import type { Candidate, VacancyAnonymous } from '../types';

export function vacanciesPage(): HTMLElement {
  const page = el('div');
  const candidate = getLoggedCandidate();
  if (!candidate) {
    location.hash = '#/login';
    return page;
  }

  const vacancies = listVacancies();
  page.append(
    el('h1', 'h3', `Olá, ${candidate.name}`),
    el('p', 'text-body-secondary', `${vacancies.length} vagas abertas. Em verde, as competências que você tem.`),
  );

  const grid = el('div', 'row row-cols-1 row-cols-md-2 row-cols-lg-3 g-3');
  for (const vacancy of vacancies) {
    const card = el('div', 'card h-100 shadow-sm');
    const body = el('div', 'card-body');
    body.append(
      el('h2', 'h5 card-title', vacancy.title),
      el('p', 'small text-body-secondary mb-2', `${vacancy.city} — ${vacancy.state}`),
      el('p', 'card-text', vacancy.description),
      ...vacancy.competencies.map((c) =>
        badge(c, candidate.competencies.includes(c) ? 'text-bg-success' : 'text-bg-secondary'),
      ),
    );
    card.append(body);
    hoverCard(card, () => vacancyInfo(vacancy, candidate));
    const column = el('div', 'col');
    column.append(card);
    grid.append(column);
  }

  page.append(grid);
  return page;
}

function vacancyInfo(vacancy: VacancyAnonymous, candidate: Candidate): HTMLElement {
  const info = el('div');
  const matching = vacancy.competencies.filter((c) => candidate.competencies.includes(c)).length;
  info.append(
    el('p', 'fw-semibold mb-1', vacancy.title),
    el('p', 'mb-1', `Local: ${vacancy.city} — ${vacancy.state}`),
    el('p', 'mb-2', vacancy.description),
    el('p', 'mb-1', `Você tem ${matching} de ${vacancy.competencies.length} competências pedidas:`),
    ...vacancy.competencies.map((c) => badge(c, candidate.competencies.includes(c) ? 'text-bg-success' : 'text-bg-secondary')),
    lockedInfo('Nome e dados da empresa revelados após o match'),
  );
  return info;
}
