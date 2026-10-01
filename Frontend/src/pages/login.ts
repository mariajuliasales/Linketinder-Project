import { HOME_BY_TYPE, login } from '../services';
import { el } from '../dom';

const TEMPLATE = `
  <div class="card shadow-sm mx-auto" style="max-width: 420px">
    <div class="card-body p-4">
      <h1 class="h3 mb-1">Entrar</h1>
      <p class="text-body-secondary">Não tem conta? <a href="#/register">Cadastre-se</a></p>
      <div class="alert alert-danger py-2" id="login-error" hidden>E-mail ou senha incorretos.</div>
      <form class="d-grid gap-3">
        <div>
          <label class="form-label" for="login-email">E-mail</label>
          <input class="form-control" id="login-email" name="email" type="email" required>
        </div>
        <div>
          <label class="form-label" for="login-password">Senha</label>
          <input class="form-control" id="login-password" name="password" type="password" required>
        </div>
        <button class="btn btn-primary" type="submit">Entrar</button>
      </form>
      <p class="small text-body-secondary mt-3 mb-0">
        Para testar: <code>mariajuliasales@gmail.com</code> (candidato) ou
        <code>contato@techsolutions.com.br</code> (empresa), senha <code>123456</code>.
      </p>
    </div>
  </div>`;

export function loginPage(): HTMLElement {
  const page = el('div');
  page.innerHTML = TEMPLATE;

  const form = page.querySelector('form')!;
  const error = page.querySelector<HTMLDivElement>('#login-error')!;

  form.addEventListener('submit', (event) => {
    event.preventDefault();
    const data = new FormData(form);
    const person = login(String(data.get('email')).trim(), String(data.get('password')));
    error.hidden = person !== undefined;
    if (person) location.hash = HOME_BY_TYPE[person.type];
  });

  return page;
}
