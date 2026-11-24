import { requireLoggedEnterprise } from './guards';
import { listCandidateCardsForEnterprise, rankCompetencesByCandidates } from '../services/candidates';
import { countMatchingCompetences } from '../services/matching';
import { card, createElement } from '../ui/dom';
import { badge, competenceBadges, lockedInfo } from '../ui/badges';
import { attachHoverCard } from '../ui/hoverCard';
import { barChart } from '../ui/barChart';
import type { BarChartEntry } from '../ui/barChart';
import type { CandidateAnonymous, CandidateCard, Enterprise } from '../types';

const TABLE_HEADER = '<thead><tr><th>Candidato</th><th>Formação</th><th>Descrição</th><th>Competências</th></tr></thead>';

function candidateLabel(position: number): string {
  return `Candidato #${position}`;
}

// O hover card não mostra dados pessoais, nem com match: nome e e-mail ficam só na linha da tabela
function describeCandidate(candidate: CandidateCard, position: number, enterprise: Enterprise): HTMLElement {
  const matches = countMatchingCompetences(enterprise.competences, candidate.competences);
  const details = createElement('div');
  details.append(
    createElement('p', 'fw-semibold mb-1', candidateLabel(position)),
    createElement('p', 'mb-1', `Formação: ${candidate.training}`),
    createElement('p', 'mb-2', `Descrição: ${candidate.description || '—'}`),
    createElement('p', 'mb-1', `Tem ${matches} de ${enterprise.competences.length} competências que sua empresa busca:`),
    ...competenceBadges(candidate.competences, enterprise.competences),
    candidate.reveal
      ? createElement('p', 'small text-body-secondary mt-2 mb-0', 'Match com sua empresa: nome e contato na tabela')
      : lockedInfo('Nome e contato revelados após o match'),
  );
  return details;
}

// Sem match, "Candidato #N" pela posição; o nome e o e-mail só existem no card com reveal (match com a empresa)
function nameCell(candidate: CandidateCard, position: number): HTMLTableCellElement {
  if (!candidate.reveal) {
    return createElement('td', 'fw-semibold text-nowrap', candidateLabel(position));
  }
  const cell = createElement('td', 'fw-semibold text-nowrap', candidate.reveal.name);
  cell.append(
    createElement('div', 'small fw-normal text-body-secondary', candidate.reveal.email),
    badge('Match', 'text-bg-success mt-1'),
  );
  return cell;
}

// A empresa não curte candidatos (CA36): a linha não tem botão de curtir
function candidateRow(candidate: CandidateCard, position: number, enterprise: Enterprise): HTMLTableRowElement {
  const competencesCell = createElement('td');
  competencesCell.append(...competenceBadges(candidate.competences));
  const row = createElement('tr');
  row.append(
    nameCell(candidate, position),
    createElement('td', '', candidate.training),
    createElement('td', '', candidate.description || '—'),
    competencesCell,
  );
  attachHoverCard(row, () => describeCandidate(candidate, position, enterprise));
  return row;
}

function candidatesTable(candidates: CandidateCard[], enterprise: Enterprise): HTMLElement {
  const table = createElement('table', 'table table-striped align-middle mb-0');
  table.innerHTML = TABLE_HEADER;
  const body = createElement('tbody');
  body.append(...candidates.map((candidate, index) => candidateRow(candidate, index + 1, enterprise)));
  table.append(body);
  return card([table], { card: 'mb-4', body: 'table-responsive' });
}

function describeCompetence([competence, count]: BarChartEntry, total: number): HTMLElement {
  const percent = total > 0 ? Math.round((count / total) * 100) : 0;
  const details = createElement('div');
  details.append(
    createElement('p', 'fw-semibold mb-1', competence),
    createElement('p', 'mb-0', `${count} ${count === 1 ? 'candidato possui' : 'candidatos possuem'} esta competência`),
    createElement('p', 'text-body-secondary mb-0', `${percent}% dos ${total} candidatos cadastrados`),
  );
  return details;
}

function competenceChart(candidates: CandidateAnonymous[]): HTMLElement {
  const chart = barChart(rankCompetencesByCandidates(candidates), {
    ariaLabel: 'Quantidade de candidatos por competência',
    unit: 'candidatos',
    describeEntry: (entry) => describeCompetence(entry, candidates.length),
  });
  return card([createElement('h2', 'h5', 'Candidatos por competência'), chart]);
}

export function candidatesPage(): HTMLElement {
  const page = createElement('div');
  const enterprise = requireLoggedEnterprise();
  if (!enterprise) {
    return page;
  }

  // A junção anônimo × revelado é feita no serviço (T28): cada card traz reveal só se houver match com a empresa
  const candidates = listCandidateCardsForEnterprise();
  page.append(
    createElement('h1', 'h3', `Olá, ${enterprise.name}`),
    createElement('p', 'text-body-secondary',
      `${candidates.length} candidatos cadastrados. Passe o mouse sobre um candidato ou uma barra do gráfico para ver detalhes.`),
    candidatesTable(candidates, enterprise),
    competenceChart(candidates),
  );
  return page;
}
