import { ROUTES, currentPath } from './navigation';
import { candidatesPage } from './pages/candidates';
import { homePage } from './pages/home';
import { loginPage } from './pages/login';
import { myVacanciesPage } from './pages/myVacancies';
import { registerPage } from './pages/register';
import { vacanciesPage } from './pages/vacancies';
import { createElement, requireElement } from './ui/dom';
import { hideHoverCard } from './ui/hoverCard';
import { navbar } from './ui/navbar';

const PAGES: Record<string, () => HTMLElement> = {
  [ROUTES.home]: homePage,
  [ROUTES.register]: registerPage,
  [ROUTES.login]: loginPage,
  [ROUTES.vacancies]: vacanciesPage,
  [ROUTES.candidates]: candidatesPage,
  [ROUTES.myVacancies]: myVacanciesPage,
};

function render(app: HTMLElement): void {
  // A página antiga é removida sem disparar mouseleave, então o cartão precisa ser fechado aqui
  hideHoverCard();
  const showPage = PAGES[currentPath()] ?? homePage;
  const main = createElement('main', 'container pb-5');
  main.append(showPage());
  app.replaceChildren(navbar(), main);
}

export function startRouter(): void {
  const app = requireElement<HTMLDivElement>(document, '#app');
  window.addEventListener('hashchange', () => render(app));
  render(app);
}
