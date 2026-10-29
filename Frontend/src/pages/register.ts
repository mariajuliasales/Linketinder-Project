import { HOME_BY_TYPE, ROUTES, currentQuery, navigateTo } from '../navigation';
import { isEmailInUse, register } from '../services/auth';
import { elementFromTemplate, requireElement } from '../ui/dom';
import { competenceFieldset, isFormValid, readForm, stateSelect } from '../ui/forms';
import type { FormReader } from '../ui/forms';
import type { Account, IsoDate, PersonType } from '../types';

const MINIMUM_AGE = 16;
const INVALID_EMAIL_MESSAGE = 'Informe um e-mail válido.';
const DUPLICATED_EMAIL_MESSAGE = 'Este e-mail já está cadastrado.';

function latestBirthDateFor(age: number): IsoDate {
  const today = new Date();
  return new Date(today.getFullYear() - age, today.getMonth(), today.getDate()).toISOString().slice(0, 10);
}

const TEMPLATE = `
  <div class="card shadow-sm mx-auto" style="max-width: 760px">
    <div class="card-body p-4">
      <h1 class="h3 mb-1">Crie sua conta</h1>
      <p class="text-body-secondary">Já tem conta? <a href="${ROUTES.login}">Entrar</a></p>

      <div class="btn-group w-100 mb-4" role="group" aria-label="Tipo de conta">
        <input type="radio" class="btn-check" name="type" id="type-candidate" value="CANDIDATE" checked>
        <label class="btn btn-outline-primary" for="type-candidate">Sou candidato</label>
        <input type="radio" class="btn-check" name="type" id="type-enterprise" value="ENTERPRISE">
        <label class="btn btn-outline-primary" for="type-enterprise">Sou empresa</label>
      </div>

      <form class="row g-3" novalidate>
        <div class="col-12">
          <label class="form-label" for="name">Nome</label>
          <input class="form-control" id="name" name="name" required minlength="2">
          <div class="invalid-feedback">Informe o nome.</div>
        </div>
        <div class="col-md-6">
          <label class="form-label" for="email">E-mail</label>
          <input class="form-control" id="email" name="email" type="email" required>
          <div class="invalid-feedback" id="email-error">${INVALID_EMAIL_MESSAGE}</div>
        </div>
        <div class="col-md-6">
          <label class="form-label" for="password">Senha</label>
          <input class="form-control" id="password" name="password" type="password" required minlength="6">
          <div class="invalid-feedback">A senha precisa ter pelo menos 6 caracteres.</div>
        </div>

        <div class="col-md-4" data-only="CANDIDATE">
          <label class="form-label" for="cpf">CPF</label>
          <input class="form-control" id="cpf" name="cpf" required pattern="\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}" placeholder="000.000.000-00">
          <div class="invalid-feedback">Informe um CPF válido.</div>
        </div>
        <div class="col-md-4" data-only="CANDIDATE">
          <label class="form-label" for="birthDate">Data de nascimento</label>
          <input class="form-control" id="birthDate" name="birthDate" type="date" required max="${latestBirthDateFor(MINIMUM_AGE)}">
          <div class="invalid-feedback">É preciso ter pelo menos ${MINIMUM_AGE} anos.</div>
        </div>
        <div class="col-md-4" data-only="CANDIDATE">
          <label class="form-label" for="training">Formação</label>
          <input class="form-control" id="training" name="training" required>
          <div class="invalid-feedback">Informe sua formação.</div>
        </div>

        <div class="col-12" data-only="ENTERPRISE" hidden>
          <label class="form-label" for="cnpj">CNPJ</label>
          <input class="form-control" id="cnpj" name="cnpj" required disabled pattern="\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}" placeholder="00.000.000/0000-00">
          <div class="invalid-feedback">Informe um CNPJ válido.</div>
        </div>

        <div class="col-md-3">
          <label class="form-label" for="cep">CEP</label>
          <input class="form-control" id="cep" name="cep" required pattern="\\d{5}-?\\d{3}" placeholder="00000-000">
          <div class="invalid-feedback">Informe um CEP válido.</div>
        </div>
        <div class="col-md-4">
          <label class="form-label" for="city">Cidade</label>
          <input class="form-control" id="city" name="city" required>
          <div class="invalid-feedback">Informe a cidade.</div>
        </div>
        <div class="col-md-2">${stateSelect('state')}</div>
        <div class="col-md-3">
          <label class="form-label" for="country">País</label>
          <input class="form-control" id="country" name="country" required value="Brasil">
          <div class="invalid-feedback">Informe o país.</div>
        </div>

        <div class="col-12">
          <label class="form-label" for="description">Descrição <span class="text-body-secondary">(opcional)</span></label>
          <textarea class="form-control" id="description" name="description" rows="3" maxlength="500"></textarea>
          <div class="form-text" data-only="CANDIDATE">Visível para as empresas: não coloque seu nome ou contato.</div>
        </div>

        ${competenceFieldset('Competências', 'competence')}

        <div class="col-12">
          <button class="btn btn-primary w-100" type="submit">Cadastrar</button>
        </div>
      </form>
    </div>
  </div>`;

function selectedType(page: HTMLElement): PersonType {
  return requireElement<HTMLInputElement>(page, '#type-enterprise').checked ? 'ENTERPRISE' : 'CANDIDATE';
}

function showOnlyFieldsOf(page: HTMLElement, type: PersonType): void {
  page.querySelectorAll<HTMLElement>('[data-only]').forEach((field) => {
    const belongsToOtherType = field.dataset.only !== type;
    field.hidden = belongsToOtherType;
    // Inputs desabilitados ficam fora da validação do navegador e do FormData
    field.querySelectorAll('input').forEach((input) => {
      input.disabled = belongsToOtherType;
    });
  });
}

function preselectTypeFromUrl(page: HTMLElement): void {
  if (currentQuery().get('type') === 'ENTERPRISE') {
    requireElement<HTMLInputElement>(page, '#type-enterprise').checked = true;
    showOnlyFieldsOf(page, 'ENTERPRISE');
  }
}

function markDuplicatedEmail(page: HTMLElement, email: string): void {
  const isDuplicated = isEmailInUse(email);
  requireElement<HTMLInputElement>(page, '#email').setCustomValidity(isDuplicated ? DUPLICATED_EMAIL_MESSAGE : '');
  requireElement(page, '#email-error').textContent = isDuplicated ? DUPLICATED_EMAIL_MESSAGE : INVALID_EMAIL_MESSAGE;
}

function buildAccount(fields: FormReader, type: PersonType): Account {
  const common = {
    id: crypto.randomUUID(),
    name: fields.text('name'),
    email: fields.text('email'),
    password: fields.text('password'),
    description: fields.text('description'),
    address: {
      cep: fields.digits('cep'), city: fields.text('city'), state: fields.text('state'), country: fields.text('country'),
    },
    competences: fields.competences(),
  };
  return type === 'CANDIDATE'
    ? { ...common, type, cpf: fields.digits('cpf'), birthDate: fields.text('birthDate'), training: fields.text('training') }
    : { ...common, type, cnpj: fields.digits('cnpj') };
}

export function registerPage(): HTMLElement {
  const page = elementFromTemplate(TEMPLATE);
  const form = requireElement<HTMLFormElement>(page, 'form');
  const competencesError = requireElement<HTMLDivElement>(page, '#competence-error');

  page.querySelectorAll<HTMLInputElement>('input[name="type"]').forEach((radio) => {
    radio.addEventListener('change', () => showOnlyFieldsOf(page, selectedType(page)));
  });
  preselectTypeFromUrl(page);

  form.addEventListener('submit', (event) => {
    event.preventDefault();
    const fields = readForm(form);
    markDuplicatedEmail(page, fields.text('email'));
    if (!isFormValid(form, competencesError, fields.competences())) {
      return;
    }
    const type = selectedType(page);
    register(buildAccount(fields, type));
    navigateTo(HOME_BY_TYPE[type]);
  });

  return page;
}
