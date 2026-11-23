import { requireLoggedEnterprise } from './guards';
import { rerender } from '../navigation';
import { createRecruiter, listOwnRecruiters, removeRecruiter } from '../services/recruiters';
import { card, createElement, elementFromTemplate, requireElement } from '../ui/dom';
import { readForm } from '../ui/forms';
import { RECRUITMENT_AREAS } from '../types';
import type { Enterprise, RecruiterSummary } from '../types';

// O 404 do serviço traz o id interno ("<Entidade> com ID <id> não existe"); a tela nunca o exibe
const NOT_FOUND_PATTERN = / com ID .* não existe$/;
const GENERIC_NOT_FOUND_MESSAGE = 'Registro não encontrado. Atualize a página e tente de novo.';

const FORM_TEMPLATE = `
  <h2 class="h5 mb-3">Cadastrar recrutador</h2>
  <div class="alert alert-danger py-2" id="recruiter-error" role="alert" hidden></div>
  <form class="row g-3" novalidate>
    <div class="col-md-6">
      <label class="form-label" for="recruiter-name">Nome</label>
      <input class="form-control" id="recruiter-name" name="name" required>
      <div class="invalid-feedback">Informe o nome.</div>
    </div>
    <div class="col-md-6">
      <label class="form-label" for="recruiter-email">E-mail</label>
      <input class="form-control" id="recruiter-email" name="email" type="email" required>
      <div class="invalid-feedback">Informe um e-mail válido.</div>
    </div>
    <div class="col-md-6">
      <label class="form-label" for="recruiter-password">Senha</label>
      <input class="form-control" id="recruiter-password" name="password" type="password" required minlength="6" autocomplete="new-password">
      <div class="invalid-feedback">A senha deve ter pelo menos 6 caracteres.</div>
    </div>
    <div class="col-md-6">
      <label class="form-label" for="recruiter-area">Área de recrutamento</label>
      <select class="form-select" id="recruiter-area" name="area" required>
        <option value="" selected disabled>Escolha a área</option>
        ${RECRUITMENT_AREAS.map((area) => `<option>${area}</option>`).join('')}
      </select>
      <div class="invalid-feedback">Escolha a área.</div>
    </div>
    <div class="col-md-6">
      <label class="form-label" for="recruiter-cpf">CPF</label>
      <input class="form-control" id="recruiter-cpf" name="cpf" required pattern="\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}" placeholder="000.000.000-00" autocomplete="off">
      <div class="invalid-feedback">Informe um CPF no formato 000.000.000-00.</div>
    </div>
    <div class="col-md-6">
      <label class="form-label" for="recruiter-birth-date">Data de nascimento</label>
      <input class="form-control" id="recruiter-birth-date" name="birthDate" type="date" required>
      <div class="invalid-feedback">Informe a data de nascimento.</div>
    </div>
    <div class="col-12">
      <label class="form-label" for="recruiter-description">Descrição</label>
      <textarea class="form-control" id="recruiter-description" name="description" rows="2" maxlength="500"></textarea>
    </div>
    <div class="col-12">
      <button class="btn btn-primary" type="submit">Cadastrar recrutador</button>
    </div>
  </form>`;

function errorMessage(error: unknown): string {
  const message = error instanceof Error ? error.message : String(error);
  return NOT_FOUND_PATTERN.test(message) ? GENERIC_NOT_FOUND_MESSAGE : message;
}

function confirmRecruiterRemoval(enterprise: Enterprise, recruiter: RecruiterSummary): void {
  if (!confirm(`Remover o recrutador ${recruiter.name}? As curtidas dele também serão removidas.`)) {
    return;
  }
  try {
    // O id fica só neste closure, nunca no DOM
    removeRecruiter(enterprise, recruiter.id);
  } catch (error) {
    alert(errorMessage(error));
  }
  rerender();
}

function recruiterRow(enterprise: Enterprise, recruiter: RecruiterSummary): HTMLTableRowElement {
  const removeButton = createElement('button', 'btn btn-outline-danger btn-sm', 'Remover');
  removeButton.type = 'button';
  removeButton.setAttribute('aria-label', `Remover o recrutador ${recruiter.name}`);
  removeButton.addEventListener('click', () => confirmRecruiterRemoval(enterprise, recruiter));

  const actions = createElement('td', 'text-end');
  actions.append(removeButton);
  const row = createElement('tr');
  row.append(
    createElement('td', 'fw-semibold', recruiter.name),
    createElement('td', '', recruiter.email),
    createElement('td', '', recruiter.area),
    createElement('td', 'small text-body-secondary', recruiter.description),
    actions,
  );
  return row;
}

function recruiterTable(enterprise: Enterprise, recruiters: RecruiterSummary[]): HTMLElement {
  if (recruiters.length === 0) {
    return createElement('p', 'text-body-secondary mb-4', 'Nenhum recrutador cadastrado ainda.');
  }
  const head = createElement('thead');
  const headRow = createElement('tr');
  headRow.append(
    ...['Nome', 'E-mail', 'Área', 'Descrição'].map((title) => createElement('th', '', title)),
    createElement('th', 'visually-hidden', 'Ações'),
  );
  head.append(headRow);
  const body = createElement('tbody');
  body.append(...recruiters.map((recruiter) => recruiterRow(enterprise, recruiter)));

  const table = createElement('table', 'table align-middle mb-0');
  table.append(head, body);
  const wrapper = createElement('div', 'table-responsive shadow-sm rounded border mb-4');
  wrapper.append(table);
  return wrapper;
}

function newRecruiterForm(enterprise: Enterprise): HTMLElement {
  const formContent = elementFromTemplate(FORM_TEMPLATE);
  const form = requireElement<HTMLFormElement>(formContent, 'form');
  const formError = requireElement<HTMLDivElement>(formContent, '#recruiter-error');

  form.addEventListener('submit', (event) => {
    event.preventDefault();
    form.classList.add('was-validated');
    if (!form.checkValidity()) {
      return;
    }
    const fields = readForm(form);
    try {
      createRecruiter(enterprise, {
        name: fields.text('name'),
        email: fields.text('email'),
        password: String(new FormData(form).get('password') ?? ''),
        description: fields.text('description'),
        cpf: fields.text('cpf'),
        birthDate: fields.text('birthDate'),
        area: fields.text('area'),
      });
    } catch (error) {
      formError.textContent = errorMessage(error);
      formError.hidden = false;
      return;
    }
    rerender();
  });

  return card([formContent]);
}

export function recruitersPage(): HTMLElement {
  const page = createElement('div');
  const enterprise = requireLoggedEnterprise();
  if (!enterprise) {
    return page;
  }

  const recruiters = listOwnRecruiters(enterprise);
  page.append(
    createElement('h1', 'h3', 'Recrutadores'),
    createElement('p', 'text-body-secondary', `${recruiters.length} recrutadores cadastrados. Eles avaliam candidatos em nome da empresa.`),
    recruiterTable(enterprise, recruiters),
    newRecruiterForm(enterprise),
  );
  return page;
}
