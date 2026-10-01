import { COMPETENCIES, STATES } from '../types';
import type { Competence } from '../types';
import { createVacancy, deleteVacancy, getLoggedEnterprise, listMyVacancies } from '../services';
import { badge, el } from '../dom';

const FORM_TEMPLATE = `
  <h2 class="h5 mb-3">Publicar nova vaga</h2>
  <form class="row g-3" novalidate>
    <div class="col-12">
      <label class="form-label" for="vacancy-title">Título</label>
      <input class="form-control" id="vacancy-title" name="title" required minlength="3" maxlength="150">
      <div class="invalid-feedback">Informe o título da vaga.</div>
    </div>
    <div class="col-12">
      <label class="form-label" for="vacancy-description">Descrição</label>
      <textarea class="form-control" id="vacancy-description" name="description" rows="3" required maxlength="500"></textarea>
      <div class="invalid-feedback">Descreva a vaga.</div>
    </div>
    <div class="col-md-8">
      <label class="form-label" for="vacancy-city">Cidade</label>
      <input class="form-control" id="vacancy-city" name="city" required>
      <div class="invalid-feedback">Informe a cidade.</div>
    </div>
    <div class="col-md-4">
      <label class="form-label" for="vacancy-state">UF</label>
      <select class="form-select" id="vacancy-state" name="state" required>
        <option value="" selected disabled>UF</option>
        ${STATES.map((uf) => `<option>${uf}</option>`).join('')}
      </select>
      <div class="invalid-feedback">Escolha.</div>
    </div>
    <fieldset class="col-12">
      <legend class="form-label fs-6">Competências exigidas</legend>
      <div class="row row-cols-2 row-cols-md-4 g-2">
        ${COMPETENCIES.map((c, i) => `
          <div class="col form-check">
            <input class="form-check-input" type="checkbox" name="competencies" value="${c}" id="vacancy-competence-${i}">
            <label class="form-check-label" for="vacancy-competence-${i}">${c}</label>
          </div>`).join('')}
      </div>
      <div class="text-danger small mt-1" id="vacancy-competencies-error" hidden>Selecione ao menos uma competência.</div>
    </fieldset>
    <div class="col-12">
      <button class="btn btn-primary" type="submit">Publicar vaga</button>
    </div>
  </form>`;

export function myVacanciesPage(): HTMLElement {
  const page = el('div');
  const enterprise = getLoggedEnterprise();
  if (!enterprise) {
    location.hash = '#/login';
    return page;
  }

  const vacancies = listMyVacancies(enterprise);
  page.append(
    el('h1', 'h3', 'Minhas vagas'),
    el('p', 'text-body-secondary', `${vacancies.length} vagas publicadas. Os candidatos veem as vagas sem o nome da empresa.`),
  );

  const list = el('ul', 'list-group shadow-sm mb-4');
  if (vacancies.length === 0) {
    list.append(el('li', 'list-group-item text-body-secondary', 'Nenhuma vaga publicada ainda.'));
  }
  for (const vacancy of vacancies) {
    const item = el('li', 'list-group-item d-flex justify-content-between align-items-start gap-3');
    const info = el('div');
    info.append(
      el('div', 'fw-semibold', vacancy.title),
      el('div', 'small text-body-secondary mb-1', `${vacancy.city} — ${vacancy.state}`),
      ...vacancy.competencies.map((c) => badge(c)),
    );
    const remove = el('button', 'btn btn-outline-danger btn-sm', 'Excluir');
    remove.type = 'button';
    remove.setAttribute('aria-label', `Excluir a vaga ${vacancy.title}`);
    remove.addEventListener('click', () => {
      if (!confirm(`Excluir a vaga "${vacancy.title}"?`)) return;
      deleteVacancy(enterprise, vacancy.id);
      window.dispatchEvent(new HashChangeEvent('hashchange')); // redesenha sem mudar o hash
    });
    item.append(info, remove);
    list.append(item);
  }

  const formCard = el('div', 'card shadow-sm');
  const formBody = el('div', 'card-body');
  formBody.innerHTML = FORM_TEMPLATE;
  formCard.append(formBody);

  const form = formBody.querySelector('form')!;
  const competenciesError = formBody.querySelector<HTMLDivElement>('#vacancy-competencies-error')!;

  form.addEventListener('submit', (event) => {
    event.preventDefault();
    const data = new FormData(form);
    const text = (field: string) => String(data.get(field) ?? '').trim();
    const competencies = data.getAll('competencies').map(String) as Competence[];

    competenciesError.hidden = competencies.length > 0;
    form.classList.add('was-validated');
    if (!form.checkValidity() || competencies.length === 0) return;

    createVacancy(enterprise, {
      title: text('title'),
      description: text('description'),
      city: text('city'),
      state: text('state'),
      competencies,
    });
    window.dispatchEvent(new HashChangeEvent('hashchange'));
  });

  page.append(list, formCard);
  return page;
}
