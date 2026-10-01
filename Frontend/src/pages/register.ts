import { COMPETENCIES, STATES } from '../types';
import type { Candidate, Competence, Enterprise, PersonType } from '../types';
import { HOME_BY_TYPE, emailInUse, register } from '../services';
import { el } from '../dom';

// Idade mínima de 16 anos
const maxBirthDate = new Date(new Date().getFullYear() - 16, new Date().getMonth(), new Date().getDate())
  .toISOString()
  .slice(0, 10);

// Template só com texto fixo; nenhum dado de usuário entra aqui
const TEMPLATE = `
  <div class="card shadow-sm mx-auto" style="max-width: 760px">
    <div class="card-body p-4">
      <h1 class="h3 mb-1">Crie sua conta</h1>
      <p class="text-body-secondary">Já tem conta? <a href="#/login">Entrar</a></p>

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
          <div class="invalid-feedback" id="email-error">Informe um e-mail válido.</div>
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
          <input class="form-control" id="birthDate" name="birthDate" type="date" required max="${maxBirthDate}">
          <div class="invalid-feedback">É preciso ter pelo menos 16 anos.</div>
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
        <div class="col-md-2">
          <label class="form-label" for="state">UF</label>
          <select class="form-select" id="state" name="state" required>
            <option value="" selected disabled>UF</option>
            ${STATES.map((uf) => `<option>${uf}</option>`).join('')}
          </select>
          <div class="invalid-feedback">Escolha.</div>
        </div>
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

        <fieldset class="col-12">
          <legend class="form-label fs-6">Competências</legend>
          <div class="row row-cols-2 row-cols-md-4 g-2">
            ${COMPETENCIES.map((c, i) => `
              <div class="col form-check">
                <input class="form-check-input" type="checkbox" name="competencies" value="${c}" id="competence-${i}">
                <label class="form-check-label" for="competence-${i}">${c}</label>
              </div>`).join('')}
          </div>
          <div class="text-danger small mt-1" id="competencies-error" hidden>Selecione ao menos uma competência.</div>
        </fieldset>

        <div class="col-12">
          <button class="btn btn-primary w-100" type="submit">Cadastrar</button>
        </div>
      </form>
    </div>
  </div>`;

export function registerPage(): HTMLElement {
  const page = el('div');
  page.innerHTML = TEMPLATE;

  const form = page.querySelector('form')!;
  const email = page.querySelector<HTMLInputElement>('#email')!;
  const emailError = page.querySelector<HTMLDivElement>('#email-error')!;
  const competenciesError = page.querySelector<HTMLDivElement>('#competencies-error')!;
  let type: PersonType = 'CANDIDATE';
  const enterpriseRadio = page.querySelector<HTMLInputElement>('#type-enterprise')!;

  // Campo disabled sai da validação e do FormData
  page.querySelectorAll<HTMLInputElement>('input[name="type"]').forEach((radio) => {
    radio.addEventListener('change', () => {
      type = radio.value === 'ENTERPRISE' ? 'ENTERPRISE' : 'CANDIDATE';
      page.querySelectorAll<HTMLElement>('[data-only]').forEach((field) => {
        const hide = field.dataset.only !== type;
        field.hidden = hide;
        field.querySelectorAll('input').forEach((input) => (input.disabled = hide));
      });
    });
  });

  // Vindo de "Quero contratar" na home, já abre com "Sou empresa" marcado
  if (location.hash.includes('type=ENTERPRISE')) {
    enterpriseRadio.checked = true;
    enterpriseRadio.dispatchEvent(new Event('change'));
  }

  form.addEventListener('submit', (event) => {
    event.preventDefault();
    const data = new FormData(form);
    const text = (field: string) => String(data.get(field) ?? '').trim();
    const digits = (field: string) => text(field).replace(/\D/g, '');
    const competencies = data.getAll('competencies').map(String) as Competence[];

    const duplicated = emailInUse(text('email'));
    email.setCustomValidity(duplicated ? 'duplicado' : '');
    emailError.textContent = duplicated ? 'Este e-mail já está cadastrado.' : 'Informe um e-mail válido.';
    competenciesError.hidden = competencies.length > 0;
    form.classList.add('was-validated');
    if (!form.checkValidity() || competencies.length === 0) return;

    const common = {
      id: crypto.randomUUID(),
      name: text('name'),
      email: text('email'),
      password: text('password'),
      description: text('description'),
      address: { cep: digits('cep'), city: text('city'), state: text('state'), country: text('country') },
      competencies,
    };

    const person: Candidate | Enterprise =
      type === 'CANDIDATE'
        ? { ...common, type, cpf: digits('cpf'), birthDate: text('birthDate'), training: text('training') }
        : { ...common, type, cnpj: digits('cnpj') };

    register(person);
    location.hash = HOME_BY_TYPE[type];
  });

  return page;
}
