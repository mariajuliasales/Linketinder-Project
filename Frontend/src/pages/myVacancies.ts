import { requireLoggedEnterprise } from './guards';
import { rerender } from '../navigation';
import { deleteOwnVacancy, listOwnVacancies, publishVacancy } from '../services/vacancies';
import { card, createElement, elementFromTemplate, requireElement } from '../ui/dom';
import { competenceBadges } from '../ui/badges';
import { competenceFieldset, isFormValid, readForm, stateSelect } from '../ui/forms';
import type { Enterprise, Vacancy } from '../types';

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
    <div class="col-md-4">${stateSelect('vacancy-state')}</div>
    ${competenceFieldset('Competências exigidas', 'vacancy-competence')}
    <div class="col-12">
      <button class="btn btn-primary" type="submit">Publicar vaga</button>
    </div>
  </form>`;

function confirmVacancyDeletion(enterprise: Enterprise, vacancy: Vacancy): void {
  if (confirm(`Excluir a vaga "${vacancy.title}"?`)) {
    deleteOwnVacancy(enterprise, vacancy.id);
    rerender();
  }
}

function vacancyItem(enterprise: Enterprise, vacancy: Vacancy): HTMLLIElement {
  const details = createElement('div');
  details.append(
    createElement('div', 'fw-semibold', vacancy.title),
    createElement('div', 'small text-body-secondary mb-1', `${vacancy.city} — ${vacancy.state}`),
    ...competenceBadges(vacancy.competences),
  );
  const deleteButton = createElement('button', 'btn btn-outline-danger btn-sm', 'Excluir');
  deleteButton.type = 'button';
  deleteButton.setAttribute('aria-label', `Excluir a vaga ${vacancy.title}`);
  deleteButton.addEventListener('click', () => confirmVacancyDeletion(enterprise, vacancy));

  const item = createElement('li', 'list-group-item d-flex justify-content-between align-items-start gap-3');
  item.append(details, deleteButton);
  return item;
}

function vacancyList(enterprise: Enterprise, vacancies: Vacancy[]): HTMLUListElement {
  const list = createElement('ul', 'list-group shadow-sm mb-4');
  if (vacancies.length === 0) {
    list.append(createElement('li', 'list-group-item text-body-secondary', 'Nenhuma vaga publicada ainda.'));
  }
  list.append(...vacancies.map((vacancy) => vacancyItem(enterprise, vacancy)));
  return list;
}

function newVacancyForm(enterprise: Enterprise): HTMLElement {
  const formContent = elementFromTemplate(FORM_TEMPLATE);
  const form = requireElement<HTMLFormElement>(formContent, 'form');
  const competencesError = requireElement<HTMLDivElement>(formContent, '#vacancy-competence-error');

  form.addEventListener('submit', (event) => {
    event.preventDefault();
    const fields = readForm(form);
    if (!isFormValid(form, competencesError, fields.competences())) {
      return;
    }
    publishVacancy(enterprise, {
      title: fields.text('title'),
      description: fields.text('description'),
      city: fields.text('city'),
      state: fields.text('state'),
      competences: fields.competences(),
    });
    rerender();
  });

  return card([formContent]);
}

export function myVacanciesPage(): HTMLElement {
  const page = createElement('div');
  const enterprise = requireLoggedEnterprise();
  if (!enterprise) {
    return page;
  }

  const vacancies = listOwnVacancies(enterprise);
  page.append(
    createElement('h1', 'h3', 'Minhas vagas'),
    createElement('p', 'text-body-secondary', `${vacancies.length} vagas publicadas. Os candidatos veem as vagas sem o nome da empresa.`),
    vacancyList(enterprise, vacancies),
    newVacancyForm(enterprise),
  );
  return page;
}
