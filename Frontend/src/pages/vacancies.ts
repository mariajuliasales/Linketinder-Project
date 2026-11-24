import { requireLoggedCandidate } from './guards';
import { rerender } from '../navigation';
import { listLikedVacancyCards, listOpenVacancyCards } from '../services/vacancies';
import { likeVacancy, unlikeVacancy } from '../services/likes';
import { countMatchingCompetences } from '../services/matching';
import { card, createElement } from '../ui/dom';
import { badge, competenceBadges, lockedInfo } from '../ui/badges';
import { attachHoverCard } from '../ui/hoverCard';
import type { Candidate, LikedVacancyCard, VacancyAnonymous, VacancyCard, VacancyStatus } from '../types';

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

function formatLocation(vacancy: VacancyAnonymous): string {
  return `${vacancy.city} — ${vacancy.state}`;
}

// Nome e e-mail da empresa só existem no card com match (LikedVacancyCard.enterprise)
function enterpriseInfo(vacancy: LikedVacancyCard): HTMLElement {
  if (!vacancy.enterprise) {
    return lockedInfo('Nome e dados da empresa revelados após o match');
  }
  const info = createElement('div', 'mt-2 border-top pt-2');
  info.append(
    badge('Match', 'text-bg-success mb-1'),
    createElement('p', 'mb-1 fw-semibold', `Empresa: ${vacancy.enterprise.name}`),
    createElement('p', 'mb-0 small', `E-mail: ${vacancy.enterprise.email}`),
  );
  return info;
}

function describeVacancy(vacancy: VacancyAnonymous, candidate: Candidate, companyInfo: HTMLElement): HTMLElement {
  const matches = countMatchingCompetences(vacancy.competences, candidate.competences);
  const details = createElement('div');
  details.append(
    createElement('p', 'fw-semibold mb-1', vacancy.title),
    createElement('p', 'mb-1', `Local: ${formatLocation(vacancy)}`),
    createElement('p', 'mb-2', vacancy.description),
    createElement('p', 'mb-1', `Você tem ${matches} de ${vacancy.competences.length} competências pedidas:`),
    ...competenceBadges(vacancy.competences, candidate.competences),
    companyInfo,
  );
  return details;
}

function toggleLike(candidate: Candidate, vacancyId: string, liked: boolean, errorBox: HTMLElement): void {
  try {
    // O id da vaga fica só neste closure, nunca no DOM
    if (liked) {
      unlikeVacancy(candidate.id, vacancyId);
    } else {
      likeVacancy(candidate.id, vacancyId);
    }
  } catch (error) {
    errorBox.textContent = errorMessage(error);
    errorBox.hidden = false;
    return;
  }
  rerender();
}

function likeButton(
  candidate: Candidate,
  vacancy: VacancyCard | LikedVacancyCard,
  liked: boolean,
  errorBox: HTMLElement,
): HTMLButtonElement {
  const action = liked ? 'Descurtir' : 'Curtir';
  const button = createElement('button', liked ? 'btn btn-primary btn-sm' : 'btn btn-outline-primary btn-sm', action);
  button.type = 'button';
  button.setAttribute('aria-pressed', String(liked));
  button.setAttribute('aria-label', `${action} a vaga ${vacancy.title}`);
  button.addEventListener('click', () => toggleLike(candidate, vacancy.id, liked, errorBox));
  return button;
}

function openVacancyCard(vacancy: VacancyCard, candidate: Candidate, errorBox: HTMLElement): HTMLElement {
  const liked = vacancy.likedByMe === true;
  const footer = createElement('div', 'mt-3');
  footer.append(likeButton(candidate, vacancy, liked, errorBox));
  const vacancyInfo = card([
    createElement('h2', 'h5 card-title', vacancy.title),
    createElement('p', 'small text-body-secondary mb-2', formatLocation(vacancy)),
    createElement('p', 'card-text', vacancy.description),
    ...competenceBadges(vacancy.competences, candidate.competences),
    footer,
  ], { card: 'h-100' });
  attachHoverCard(vacancyInfo, () =>
    describeVacancy(vacancy, candidate, lockedInfo('Nome e dados da empresa revelados após o match')));
  const column = createElement('div', 'col');
  column.append(vacancyInfo);
  return column;
}

function likedVacancyItem(vacancy: LikedVacancyCard, candidate: Candidate, errorBox: HTMLElement): HTMLLIElement {
  const title = createElement('div', 'fw-semibold d-flex align-items-center gap-2');
  title.append(badge(STATUS_LABELS[vacancy.status], STATUS_COLORS[vacancy.status]), document.createTextNode(vacancy.title));

  const content = createElement('div', 'flex-grow-1');
  content.append(
    title,
    createElement('div', 'small text-body-secondary mb-1', formatLocation(vacancy)),
    ...competenceBadges(vacancy.competences, candidate.competences),
    enterpriseInfo(vacancy),
  );

  const item = createElement('li', 'list-group-item d-flex gap-3 align-items-start');
  item.append(content, likeButton(candidate, vacancy, true, errorBox));
  attachHoverCard(content, () => describeVacancy(vacancy, candidate, enterpriseInfo(vacancy)));
  return item;
}

function likedVacanciesSection(likedVacancies: LikedVacancyCard[], candidate: Candidate, errorBox: HTMLElement): HTMLElement {
  const section = createElement('section', 'mt-5');
  section.append(createElement('h2', 'h4', 'Minhas curtidas'));
  if (likedVacancies.length === 0) {
    section.append(createElement('p', 'text-body-secondary', 'Você ainda não curtiu nenhuma vaga.'));
    return section;
  }
  section.append(createElement('p', 'text-body-secondary small',
    'Vagas que você curtiu, em qualquer status. Nome e e-mail da empresa aparecem só nas vagas com match.'));
  const list = createElement('ul', 'list-group');
  list.append(...likedVacancies.map((vacancy) => likedVacancyItem(vacancy, candidate, errorBox)));
  section.append(list);
  return section;
}

export function vacanciesPage(): HTMLElement {
  const page = createElement('div');
  const candidate = requireLoggedCandidate();
  if (!candidate) {
    return page;
  }

  const errorBox = createElement('div', 'alert alert-danger py-2');
  errorBox.setAttribute('role', 'alert');
  errorBox.hidden = true;

  let vacancies: VacancyCard[];
  let likedVacancies: LikedVacancyCard[];
  try {
    vacancies = listOpenVacancyCards(candidate);
    likedVacancies = listLikedVacancyCards(candidate);
  } catch (error) {
    errorBox.textContent = errorMessage(error);
    errorBox.hidden = false;
    page.append(createElement('h1', 'h3', 'Vagas'), errorBox);
    return page;
  }

  const grid = createElement('div', 'row row-cols-1 row-cols-md-2 row-cols-lg-3 g-3');
  grid.append(...vacancies.map((vacancy) => openVacancyCard(vacancy, candidate, errorBox)));
  page.append(
    createElement('h1', 'h3', `Olá, ${candidate.name}`),
    errorBox,
    createElement('h2', 'h4', 'Vagas abertas'),
    createElement('p', 'text-body-secondary',
      `${vacancies.length} vagas abertas. Em verde, as competências que você tem. Curta uma vaga para demonstrar interesse.`),
    grid,
    likedVacanciesSection(likedVacancies, candidate, errorBox),
  );
  return page;
}
