import { requireLoggedRecruiter } from './guards';
import { getOwnEnterpriseName } from '../services/recruiters';
import { listEnterpriseVacanciesForRecruiter } from '../services/vacancies';
import { createElement } from '../ui/dom';
import { badge, competenceBadges } from '../ui/badges';
import type { VacancyCard, VacancyStatus } from '../types';

// O 404 do serviço traz o id interno ("<Entidade> com ID <id> não existe"); a tela nunca o exibe
const NOT_FOUND_PATTERN = / com ID .* não existe$/;
const GENERIC_NOT_FOUND_MESSAGE = 'Registro não encontrado. Atualize a página e tente de novo.';

const STATUS_LABELS: Record<VacancyStatus, string> = {
  OPEN: 'Aberta',
  PAUSED: 'Pausada',
  CLOSED: 'Fechada',
};

const STATUS_COLORS: Record<VacancyStatus, string> = {
  OPEN: 'text-bg-success',
  PAUSED: 'text-bg-warning',
  CLOSED: 'text-bg-secondary',
};

function errorMessage(error: unknown): string {
  const message = error instanceof Error ? error.message : String(error);
  return NOT_FOUND_PATTERN.test(message) ? GENERIC_NOT_FOUND_MESSAGE : message;
}

// Somente leitura: o recrutador não cria, edita nem exclui vagas, então não há botões
function vacancyItem(vacancy: VacancyCard): HTMLLIElement {
  const title = createElement('div', 'fw-semibold d-flex align-items-center gap-2');
  title.append(badge(STATUS_LABELS[vacancy.status], STATUS_COLORS[vacancy.status]), document.createTextNode(vacancy.title));

  const item = createElement('li', 'list-group-item');
  item.append(
    title,
    createElement('div', 'small text-body-secondary mb-1', `${vacancy.city} — ${vacancy.state}`),
    createElement('p', 'mb-2', vacancy.description),
    ...competenceBadges(vacancy.competences),
  );
  return item;
}

function vacancyList(vacancies: VacancyCard[]): HTMLUListElement {
  const list = createElement('ul', 'list-group shadow-sm mb-4');
  if (vacancies.length === 0) {
    list.append(createElement('li', 'list-group-item text-body-secondary', 'A empresa ainda não publicou vagas.'));
  }
  list.append(...vacancies.map(vacancyItem));
  return list;
}

export function recruiterVacanciesPage(): HTMLElement {
  const page = createElement('div');
  const recruiter = requireLoggedRecruiter();
  if (!recruiter) {
    return page;
  }

  let vacancies: VacancyCard[];
  let enterpriseName: string;
  try {
    vacancies = listEnterpriseVacanciesForRecruiter(recruiter);
    enterpriseName = getOwnEnterpriseName(recruiter);
  } catch (error) {
    const errorBox = createElement('div', 'alert alert-danger py-2', errorMessage(error));
    errorBox.setAttribute('role', 'alert');
    page.append(createElement('h1', 'h3', 'Vagas da empresa'), errorBox);
    return page;
  }

  page.append(
    createElement('h1', 'h3', `Vagas de ${enterpriseName}`),
    createElement('p', 'text-body-secondary',
      `${vacancies.length} vagas em todos os status. Somente leitura: quem publica e exclui vagas é a empresa.`),
    vacancyList(vacancies),
  );
  return page;
}
