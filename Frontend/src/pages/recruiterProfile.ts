import { requireLoggedRecruiter } from './guards';
import { getOwnEnterpriseName, updateOwnProfile } from '../services/recruiters';
import { card, createElement, elementFromTemplate, requireElement } from '../ui/dom';
import type { Recruiter } from '../types';

// O 404 do serviço traz o id interno ("<Entidade> com ID <id> não existe"); a tela nunca o exibe
const NOT_FOUND_PATTERN = / com ID .* não existe$/;
const GENERIC_NOT_FOUND_MESSAGE = 'Registro não encontrado. Atualize a página e tente de novo.';

// Sem minlength na senha: a regra (vazia mantém, curta dá erro) fica no serviço, com a mesma mensagem do back-end
const FORM_TEMPLATE = `
  <h2 class="h5 mb-3">Editar perfil</h2>
  <div class="alert alert-danger py-2" id="profile-error" role="alert" hidden></div>
  <div class="alert alert-success py-2" id="profile-success" role="status" hidden>Perfil atualizado.</div>
  <form class="row g-3" novalidate>
    <div class="col-12">
      <label class="form-label" for="profile-description">Descrição</label>
      <textarea class="form-control" id="profile-description" name="description" rows="3" maxlength="500"></textarea>
    </div>
    <div class="col-md-6">
      <label class="form-label" for="profile-password">Nova senha</label>
      <input class="form-control" id="profile-password" name="password" type="password" autocomplete="new-password" aria-describedby="profile-password-help">
      <div class="form-text" id="profile-password-help">Deixe em branco para manter a senha atual.</div>
    </div>
    <div class="col-12">
      <button class="btn btn-primary" type="submit">Salvar</button>
    </div>
  </form>`;

function errorMessage(error: unknown): string {
  const message = error instanceof Error ? error.message : String(error);
  return NOT_FOUND_PATTERN.test(message) ? GENERIC_NOT_FOUND_MESSAGE : message;
}

// Só dados do próprio recrutador; CPF e data de nascimento nunca aparecem
function summary(recruiter: Recruiter, enterpriseName: string): HTMLElement {
  const list = createElement('dl', 'row mb-0');
  const rows: [string, string][] = [
    ['Nome', recruiter.name],
    ['E-mail', recruiter.email],
    ['Área', recruiter.area],
    ['Empresa', enterpriseName],
  ];
  rows.forEach(([label, value]) => {
    list.append(createElement('dt', 'col-sm-3', label), createElement('dd', 'col-sm-9', value));
  });
  return card([list], { card: 'mb-4' });
}

function profileForm(recruiter: Recruiter): HTMLElement {
  const formContent = elementFromTemplate(FORM_TEMPLATE);
  const form = requireElement<HTMLFormElement>(formContent, 'form');
  const formError = requireElement<HTMLDivElement>(formContent, '#profile-error');
  const formSuccess = requireElement<HTMLDivElement>(formContent, '#profile-success');
  const description = requireElement<HTMLTextAreaElement>(formContent, '#profile-description');
  const password = requireElement<HTMLInputElement>(formContent, '#profile-password');
  // value, não innerHTML: a descrição é dado do usuário
  description.value = recruiter.description;

  form.addEventListener('submit', (event) => {
    event.preventDefault();
    formError.hidden = true;
    formSuccess.hidden = true;
    try {
      updateOwnProfile(recruiter, description.value.trim(), password.value);
    } catch (error) {
      formError.textContent = errorMessage(error);
      formError.hidden = false;
      return;
    }
    password.value = '';
    formSuccess.hidden = false;
  });

  return card([formContent]);
}

export function recruiterProfilePage(): HTMLElement {
  const page = createElement('div');
  const recruiter = requireLoggedRecruiter();
  if (!recruiter) {
    return page;
  }

  page.append(createElement('h1', 'h3', 'Meu perfil'));
  let enterpriseName: string;
  try {
    enterpriseName = getOwnEnterpriseName(recruiter);
  } catch (error) {
    const errorBox = createElement('div', 'alert alert-danger py-2', errorMessage(error));
    errorBox.setAttribute('role', 'alert');
    page.append(errorBox);
    return page;
  }

  page.append(
    createElement('p', 'text-body-secondary', 'Você pode editar a descrição e a senha. Os outros dados são mantidos pela empresa.'),
    summary(recruiter, enterpriseName),
    profileForm(recruiter),
  );
  return page;
}
