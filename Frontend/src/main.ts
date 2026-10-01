import 'bootstrap/dist/css/bootstrap.min.css';
import './style.css';
import { registerPage } from './pages/register';
import { loginPage } from './pages/login';
import { vacanciesPage } from './pages/vacancies';
import { candidatesPage } from './pages/candidates';
import { homePage } from './pages/home';
import { myVacanciesPage } from './pages/myVacancies';
import { HOME_BY_TYPE, deleteAccount, getLoggedCandidate, getLoggedEnterprise, logout } from './services';
import { el, hideHoverCard } from './dom';

const routes: Record<string, () => HTMLElement> = {
  '#/home': homePage,
  '#/register': registerPage,
  '#/login': loginPage,
  '#/vacancies': vacanciesPage,
  '#/candidates': candidatesPage,
  '#/my-vacancies': myVacanciesPage,
};

const app = document.querySelector<HTMLDivElement>('#app')!;


function navbar(): HTMLElement {
  const nav = el('nav', 'navbar bg-primary mb-4');
  nav.setAttribute('data-bs-theme', 'dark');
  const container = el('div', 'container');
  const brand = el('a', 'navbar-brand fw-bold', 'Linketinder');
  const actions = el('div', 'd-flex flex-wrap align-items-center gap-2');

  const user = getLoggedCandidate() ?? getLoggedEnterprise();
  if (user) {
    brand.href = HOME_BY_TYPE[user.type];

    const links: [string, string][] =
      user.type === 'CANDIDATE'
        ? [['#/vacancies', 'Vagas']]
        : [['#/candidates', 'Candidatos'], ['#/my-vacancies', 'Minhas vagas']];
    for (const [href, text] of links) {
      const active = location.hash === href;
      const link = el('a', `link-light px-2 ${active ? 'fw-semibold' : 'text-decoration-none'}`, text);
      link.href = href;
      if (active) link.setAttribute('aria-current', 'page');
      actions.append(link);
    }

    const removeAccount = el('button', 'btn btn-link link-light btn-sm', 'Excluir conta');
    removeAccount.addEventListener('click', () => {
      if (!confirm('Excluir sua conta? Essa ação não pode ser desfeita.')) return;
      deleteAccount();
      location.hash = '#/home';
    });
    const exit = el('button', 'btn btn-outline-light btn-sm', 'Sair');
    exit.addEventListener('click', () => {
      logout();
      location.hash = '#/login';
    });
    actions.append(el('span', 'navbar-text text-white small ms-2', user.name), removeAccount, exit);
  } else {
    brand.href = '#/home';
    const signIn = el('a', 'btn btn-outline-light btn-sm', 'Entrar');
    signIn.href = '#/login';
    const signUp = el('a', 'btn btn-light btn-sm', 'Cadastre-se');
    signUp.href = '#/register';
    actions.append(signIn, signUp);
  }

  container.append(brand, actions);
  nav.append(container);
  return nav;
}

function render(): void {
  hideHoverCard(); // elementos removidos não disparam mouseleave
  // Ignora parâmetros depois do "?" (ex.: #/register?type=ENTERPRISE)
  const path = location.hash.split('?')[0];
  const page = routes[path] ?? homePage;
  const main = el('main', 'container pb-5');
  main.append(page());
  app.replaceChildren(navbar(), main);
}

window.addEventListener('hashchange', render);
render();
