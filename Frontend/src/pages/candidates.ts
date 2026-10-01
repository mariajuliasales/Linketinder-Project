import { countByCompetence, getLoggedEnterprise, listCandidates } from '../services';
import { badge, el, hoverCard, lockedInfo } from '../dom';
import type { CandidateAnonymous, Enterprise } from '../types';

export function candidatesPage(): HTMLElement {
  const page = el('div');
  const enterprise = getLoggedEnterprise();
  if (!enterprise) {
    location.hash = '#/login';
    return page;
  }

  const candidates = listCandidates();
  page.append(
    el('h1', 'h3', `Olá, ${enterprise.name}`),
    el('p', 'text-body-secondary', `${candidates.length} candidatos cadastrados. Passe o mouse sobre um candidato ou uma barra do gráfico para ver detalhes.`),
  );

  const table = el('table', 'table table-striped align-middle mb-0');
  table.innerHTML = '<thead><tr><th>Candidato</th><th>Formação</th><th>Descrição</th><th>Competências</th></tr></thead>';
  const tbody = el('tbody');
  candidates.forEach((candidate, i) => {
    const row = el('tr');
    const competencies = el('td');
    competencies.append(...candidate.competencies.map((c) => badge(c)));
    row.append(
      el('td', 'fw-semibold text-nowrap', `Candidato #${i + 1}`),
      el('td', '', candidate.training),
      el('td', '', candidate.description || '—'),
      competencies,
    );
    hoverCard(row, () => candidateInfo(candidate, i + 1, enterprise));
    tbody.append(row);
  });
  table.append(tbody);

  const tableCard = el('div', 'card shadow-sm mb-4');
  const tableWrapper = el('div', 'card-body table-responsive');
  tableWrapper.append(table);
  tableCard.append(tableWrapper);

  const chartCard = el('div', 'card shadow-sm');
  const chartBody = el('div', 'card-body');
  chartBody.append(el('h2', 'h5', 'Candidatos por competência'), barChart(countByCompetence(candidates), candidates.length));
  chartCard.append(chartBody);

  page.append(tableCard, chartCard);
  return page;
}

function candidateInfo(candidate: CandidateAnonymous, number: number, enterprise: Enterprise): HTMLElement {
  const info = el('div');
  const wanted = candidate.competencies.filter((c) => enterprise.competencies.includes(c)).length;
  info.append(
    el('p', 'fw-semibold mb-1', `Candidato #${number}`),
    el('p', 'mb-1', `Formação: ${candidate.training}`),
    el('p', 'mb-2', `Descrição: ${candidate.description || '—'}`),
    el('p', 'mb-1', `Tem ${wanted} de ${enterprise.competencies.length} competências que sua empresa busca:`),
    ...candidate.competencies.map((c) => badge(c, enterprise.competencies.includes(c) ? 'text-bg-success' : 'text-bg-secondary')),
    lockedInfo('Nome e contato revelados após o match'),
  );
  return info;
}

function barChart(data: [string, number][], total: number): SVGSVGElement {
  const ns = 'http://www.w3.org/2000/svg';
  const width = 660;
  const rowHeight = 34;
  const labelWidth = 170;
  const maxBar = 440;
  const max = Math.max(1, ...data.map(([, count]) => count));

  const svg = document.createElementNS(ns, 'svg');
  svg.setAttribute('viewBox', `0 0 ${width} ${data.length * rowHeight}`);
  svg.setAttribute('role', 'img');
  svg.setAttribute('aria-label', 'Quantidade de candidatos por competência');
  svg.style.maxWidth = '720px';
  svg.style.width = '100%';

  data.forEach(([competence, count], i) => {
    const y = i * rowHeight;
    const barWidth = (count / max) * maxBar;

    // Área transparente: a linha inteira reage ao mouse, não só a barra
    const row = document.createElementNS(ns, 'g');
    row.setAttribute('class', 'chart-row');
    row.setAttribute('aria-label', `${competence}: ${count} candidatos`);

    const hitArea = document.createElementNS(ns, 'rect');
    hitArea.setAttribute('y', String(y));
    hitArea.setAttribute('width', String(width));
    hitArea.setAttribute('height', String(rowHeight));
    hitArea.setAttribute('fill', 'transparent');

    const label = document.createElementNS(ns, 'text');
    label.setAttribute('class', 'chart-text');
    label.setAttribute('x', String(labelWidth - 8));
    label.setAttribute('y', String(y + 18));
    label.setAttribute('text-anchor', 'end');
    label.textContent = competence;

    const bar = document.createElementNS(ns, 'rect');
    bar.setAttribute('class', 'chart-bar');
    bar.setAttribute('x', String(labelWidth));
    bar.setAttribute('y', String(y + 4));
    bar.setAttribute('width', String(barWidth));
    bar.setAttribute('height', '22');
    bar.setAttribute('rx', '4');

    const value = document.createElementNS(ns, 'text');
    value.setAttribute('class', 'chart-text');
    value.setAttribute('x', String(labelWidth + barWidth + 6));
    value.setAttribute('y', String(y + 20));
    value.textContent = String(count);

    row.append(hitArea, label, bar, value);
    hoverCard(row, () => {
      const info = el('div');
      const percent = total > 0 ? Math.round((count / total) * 100) : 0;
      info.append(
        el('p', 'fw-semibold mb-1', competence),
        el('p', 'mb-0', `${count} ${count === 1 ? 'candidato possui' : 'candidatos possuem'} esta competência`),
        el('p', 'text-body-secondary mb-0', `${percent}% dos ${total} candidatos cadastrados`),
      );
      return info;
    });
    svg.append(row);
  });

  return svg;
}
