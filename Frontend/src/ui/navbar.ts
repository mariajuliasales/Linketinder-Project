import { createElement } from './dom';
import { deleteLoggedAccount, getLoggedAccount, logout } from '../services/auth';
import { HOME_BY_TYPE, ROUTES, navigateTo } from '../navigation';
import type { Account } from '../types';

type NavLink = [route: string, text: string];

const LINKS_BY_TYPE: Record<Account['type'], NavLink[]> = {
  CANDIDATE: [[ROUTES.vacancies, 'Vagas']],
  ENTERPRISE: [[ROUTES.candidates, 'Candidatos'], [ROUTES.myVacancies, 'Minhas vagas']],
};

function link(route: string, className: string, text: string): HTMLAnchorElement {
  const anchor = createElement('a', className, text);
  anchor.href = route;
  return anchor;
}

function button(className: string, text: string, onClick: () => void): HTMLButtonElement {
  const element = createElement('button', className, text);
  element.addEventListener('click', onClick);
  return element;
}

function navLink([route, text]: NavLink): HTMLAnchorElement {
  const isActive = location.hash === route;
  const anchor = link(route, `link-light px-2 ${isActive ? 'fw-semibold' : 'text-decoration-none'}`, text);
  if (isActive) {
    anchor.setAttribute('aria-current', 'page');
  }
  return anchor;
}

function confirmAccountDeletion(): void {
  if (confirm('Excluir sua conta? Essa ação não pode ser desfeita.')) {
    deleteLoggedAccount();
    navigateTo(ROUTES.home);
  }
}

function loggedActions(account: Account): HTMLElement[] {
  return [
    ...LINKS_BY_TYPE[account.type].map(navLink),
    createElement('span', 'navbar-text text-white small ms-2', account.name),
    button('btn btn-link link-light btn-sm', 'Excluir conta', confirmAccountDeletion),
    button('btn btn-outline-light btn-sm', 'Sair', () => {
      logout();
      navigateTo(ROUTES.login);
    }),
  ];
}

function guestActions(): HTMLElement[] {
  return [
    link(ROUTES.login, 'btn btn-outline-light btn-sm', 'Entrar'),
    link(ROUTES.register, 'btn btn-light btn-sm', 'Cadastre-se'),
  ];
}

export function navbar(): HTMLElement {
  const account = getLoggedAccount();
  const brand = link(account ? HOME_BY_TYPE[account.type] : ROUTES.home, 'navbar-brand fw-bold', 'Linketinder');
  const actions = createElement('div', 'd-flex flex-wrap align-items-center gap-2');
  actions.append(...(account ? loggedActions(account) : guestActions()));

  const container = createElement('div', 'container');
  container.append(brand, actions);
  const nav = createElement('nav', 'navbar bg-primary mb-4');
  nav.setAttribute('data-bs-theme', 'dark');
  nav.append(container);
  return nav;
}
