import { requireLoggedRecruiter } from './guards';
import { rerender } from '../navigation';
import { listCandidateCardsForRecruiter } from '../services/candidates';
import { likeCandidate, unlikeCandidate } from '../services/likes';
import { getOwnEnterpriseName } from '../services/recruiters';
import { card, createElement } from '../ui/dom';
import { badge, competenceBadges, lockedInfo } from '../ui/badges';
import { attachHoverCard } from '../ui/hoverCard';
import type { CandidateCard, MatchedVacancy, Recruiter, VacancyStatus } from '../types';

// O 404 do serviço traz o id interno ("<Entidade> com ID <id> não existe"); a tela nunca o exibe
const NOT_FOUND_PATTERN = / com ID .* não existe$/;
const GENERIC_NOT_FOUND_MESSAGE = 'Registro não encontrado. Atualize a página e tente de novo.';

const STATUS_LABELS: Record<VacancyStatus, string> = {
  OPEN: 'Aberta',
  PAUSED: 'Pausada',
  CLOSED: 'Fechada',
};

const TABLE_HEADER =
  '<thead><tr><th>Candidato</th><th>Formação</th><th>Descrição</th><th>Competências</th>' +
  '<th class="visually-hidden">Ações</th></tr></thead>';

function errorMessage(error: unknown): string {
  const message = error instanceof Error ? error.message : String(error);
  return NOT_FOUND_PATTERN.test(message) ? GENERIC_NOT_FOUND_MESSAGE : message;
}

// Antes do match, "Candidato #N" pela posição na lista; o nome só existe no card com reveal (match com a empresa)
function candidateLabel(candidate: CandidateCard, position: number): string {
  return candidate.reveal?.name ?? `Candidato #${position}`;
}

function matchedVacanciesList(vacancies: MatchedVacancy[]): HTMLElement {
  const list = createElement('ul', 'list-unstyled mb-0');
  list.append(
    ...vacancies.map((vacancy) => {
      const item = createElement('li', 'small');
      item.append(badge(STATUS_LABELS[vacancy.status], 'text-bg-light border'), document.createTextNode(vacancy.title));
      return item;
    }),
  );
  return list;
}

function revealedInfo(reveal: NonNullable<CandidateCard['reveal']>): HTMLElement {
  const info = createElement('div', 'mt-2 border-top pt-2');
  info.append(
    createElement('p', 'mb-1', `E-mail: ${reveal.email}`),
    createElement('p', 'mb-1 fw-semibold', 'Vagas da empresa com match:'),
    matchedVacanciesList(reveal.matchedVacancies),
  );
  return info;
}

function describeCandidate(candidate: CandidateCard, position: number): HTMLElement {
  const details = createElement('div');
  details.append(
    createElement('p', 'fw-semibold mb-1', candidateLabel(candidate, position)),
    createElement('p', 'mb-1', `Formação: ${candidate.training}`),
    createElement('p', 'mb-2', `Descrição: ${candidate.description || '—'}`),
    ...competenceBadges(candidate.competences),
    candidate.reveal ? revealedInfo(candidate.reveal) : lockedInfo('Nome e contato revelados após o match'),
  );
  return details;
}

function toggleLike(recruiter: Recruiter, candidate: CandidateCard, errorBox: HTMLElement): void {
  try {
    // O id fica só neste closure, nunca no DOM
    if (candidate.likedByMe) {
      unlikeCandidate(recruiter.id, candidate.id);
    } else {
      likeCandidate(recruiter.id, candidate.id);
    }
  } catch (error) {
    errorBox.textContent = errorMessage(error);
    errorBox.hidden = false;
    return;
  }
  rerender();
}

function likeButton(recruiter: Recruiter, candidate: CandidateCard, position: number, errorBox: HTMLElement): HTMLElement {
  const label = candidateLabel(candidate, position);
  const button = createElement(
    'button',
    candidate.likedByMe ? 'btn btn-primary btn-sm' : 'btn btn-outline-primary btn-sm',
    candidate.likedByMe ? 'Descurtir' : 'Curtir',
  );
  button.type = 'button';
  button.setAttribute('aria-pressed', String(candidate.likedByMe));
  button.setAttribute('aria-label', `${candidate.likedByMe ? 'Descurtir' : 'Curtir'} ${label}`);
  button.addEventListener('click', () => toggleLike(recruiter, candidate, errorBox));
  return button;
}

function nameCell(candidate: CandidateCard, position: number): HTMLTableCellElement {
  const cell = createElement('td', 'fw-semibold text-nowrap', candidateLabel(candidate, position));
  if (candidate.reveal) {
    cell.append(createElement('div', 'small fw-normal text-body-secondary', candidate.reveal.email));
    cell.append(badge('Match', 'text-bg-success mt-1'));
  }
  return cell;
}

function candidateRow(
  recruiter: Recruiter,
  candidate: CandidateCard,
  position: number,
  errorBox: HTMLElement,
): HTMLTableRowElement {
  const competencesCell = createElement('td');
  competencesCell.append(...competenceBadges(candidate.competences));
  const descriptionCell = createElement('td', '', candidate.description || '—');
  if (candidate.reveal) {
    descriptionCell.append(revealedInfo(candidate.reveal));
  }
  const actions = createElement('td', 'text-end');
  actions.append(likeButton(recruiter, candidate, position, errorBox));

  const row = createElement('tr');
  row.append(
    nameCell(candidate, position),
    createElement('td', '', candidate.training),
    descriptionCell,
    competencesCell,
    actions,
  );
  attachHoverCard(row, () => describeCandidate(candidate, position));
  return row;
}

function candidatesTable(recruiter: Recruiter, candidates: CandidateCard[], errorBox: HTMLElement): HTMLElement {
  const table = createElement('table', 'table table-striped align-middle mb-0');
  table.innerHTML = TABLE_HEADER;
  const body = createElement('tbody');
  body.append(...candidates.map((candidate, index) => candidateRow(recruiter, candidate, index + 1, errorBox)));
  table.append(body);
  return card([table], { card: 'mb-4', body: 'table-responsive' });
}

export function recruiterCandidatesPage(): HTMLElement {
  const page = createElement('div');
  const recruiter = requireLoggedRecruiter();
  if (!recruiter) {
    return page;
  }

  const errorBox = createElement('div', 'alert alert-danger py-2');
  errorBox.setAttribute('role', 'alert');
  errorBox.hidden = true;

  let candidates: CandidateCard[];
  let enterpriseName: string;
  try {
    candidates = listCandidateCardsForRecruiter();
    enterpriseName = getOwnEnterpriseName(recruiter);
  } catch (error) {
    errorBox.textContent = errorMessage(error);
    errorBox.hidden = false;
    page.append(createElement('h1', 'h3', 'Candidatos'), errorBox);
    return page;
  }

  page.append(
    createElement('h1', 'h3', `Olá, ${recruiter.name}`),
    createElement('p', 'mb-1', `Recrutando para ${enterpriseName}`),
    createElement('p', 'text-body-secondary',
      `${candidates.length} candidatos cadastrados. Curta um candidato para avaliá-lo; nome e contato aparecem após o match.`),
    errorBox,
    candidatesTable(recruiter, candidates, errorBox),
  );
  return page;
}
