import { requireLoggedEnterprise } from './guards';
import { listAnonymousCandidates, rankCompetencesByCandidates } from '../services/candidates';
import { countMatchingCompetences } from '../services/matching';
import { card, createElement } from '../ui/dom';
import { competenceBadges, lockedInfo } from '../ui/badges';
import { attachHoverCard } from '../ui/hoverCard';
import { barChart } from '../ui/barChart';
import type { BarChartEntry } from '../ui/barChart';
import type { CandidateAnonymous, Enterprise } from '../types';

const TABLE_HEADER = '<thead><tr><th>Candidato</th><th>Formação</th><th>Descrição</th><th>Competências</th></tr></thead>';

function candidateLabel(position: number): string {
  return `Candidato #${position}`;
}

function describeCandidate(candidate: CandidateAnonymous, position: number, enterprise: Enterprise): HTMLElement {
  const matches = countMatchingCompetences(enterprise.competences, candidate.competences);
  const details = createElement('div');
  details.append(
    createElement('p', 'fw-semibold mb-1', candidateLabel(position)),
    createElement('p', 'mb-1', `Formação: ${candidate.training}`),
    createElement('p', 'mb-2', `Descrição: ${candidate.description || '—'}`),
    createElement('p', 'mb-1', `Tem ${matches} de ${enterprise.competences.length} competências que sua empresa busca:`),
    ...competenceBadges(candidate.competences, enterprise.competences),
    lockedInfo('Nome e contato revelados após o match'),
  );
  return details;
}

function candidateRow(candidate: CandidateAnonymous, position: number, enterprise: Enterprise): HTMLTableRowElement {
  const competencesCell = createElement('td');
  competencesCell.append(...competenceBadges(candidate.competences));
  const row = createElement('tr');
  row.append(
    createElement('td', 'fw-semibold text-nowrap', candidateLabel(position)),
    createElement('td', '', candidate.training),
    createElement('td', '', candidate.description || '—'),
    competencesCell,
  );
  attachHoverCard(row, () => describeCandidate(candidate, position, enterprise));
  return row;
}

function candidatesTable(candidates: CandidateAnonymous[], enterprise: Enterprise): HTMLElement {
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

  const candidates = listAnonymousCandidates();
  page.append(
    createElement('h1', 'h3', `Olá, ${enterprise.name}`),
    createElement('p', 'text-body-secondary',
      `${candidates.length} candidatos cadastrados. Passe o mouse sobre um candidato ou uma barra do gráfico para ver detalhes.`),
    candidatesTable(candidates, enterprise),
    competenceChart(candidates),
  );
  return page;
}
