import { elementFromTemplate, requireElement } from '../ui/dom';
import { COMPETENCES } from '../types';
import { ROUTES } from '../navigation';
import { getPlatformStats } from '../services/stats';
import type { PlatformStats } from '../services/stats';

const STAT_CARDS: [stat: keyof PlatformStats, label: string][] = [
  ['openVacancies', 'vagas abertas'],
  ['candidates', 'candidatos'],
  ['enterprises', 'empresas'],
];

const STEPS: [title: string, text: string][] = [
  ['Crie sua conta', 'Cadastre-se como candidato ou empresa e marque as competências que você tem ou procura.'],
  ['Conheça sem preconceito', 'Candidatos veem as vagas sem saber a empresa; empresas veem os candidatos sem nome, só formação e competências.'],
  ['Dê match <span class="badge text-bg-warning align-middle">em breve</span>', 'Quando o candidato curtir a vaga e a empresa curtir o candidato, os dois lados se conhecem de verdade.'],
];

function statCard([stat, label]: [string, string]): string {
  return `
    <div class="col"><div class="card h-100 shadow-sm"><div class="card-body">
      <p class="display-6 fw-bold text-primary mb-0" data-stat="${stat}"></p>
      <p class="text-body-secondary mb-0">${label}</p>
    </div></div></div>`;
}

function step([title, text]: [string, string], index: number): string {
  return `
    <div class="col">
      <span class="step rounded-circle bg-primary text-white fw-bold mb-2">${index + 1}</span>
      <h3 class="h5">${title}</h3>
      <p class="text-body-secondary">${text}</p>
    </div>`;
}

function audienceCard(title: string, benefits: string[], route: string, action: string): string {
  return `
    <div class="col">
      <div class="card h-100 shadow-sm"><div class="card-body p-4">
        <h2 class="h4">${title}</h2>
        <ul class="text-body-secondary">${benefits.map((benefit) => `<li>${benefit}</li>`).join('')}</ul>
        <a class="btn btn-primary" href="${route}">${action}</a>
      </div></div>
    </div>`;
}

const TEMPLATE = `
  <section class="hero rounded-4 text-white text-center px-3 py-5 mb-5">
    <h1 class="display-5 fw-bold">Bem-vindo ao Linketinder!</h1>
    <p class="lead mx-auto mb-4" style="max-width: 640px">
      O seu lugar para encontrar as melhores oportunidades de trabalho e os melhores talentos.
      Aqui ninguém é julgado pelo nome ou pela foto: candidatos e empresas se conhecem pelas competências.
    </p>
    <div class="d-flex flex-wrap justify-content-center gap-2">
      <a class="btn btn-light btn-lg" href="${ROUTES.register}">Quero uma vaga</a>
      <a class="btn btn-outline-light btn-lg" href="${ROUTES.registerEnterprise}">Quero contratar</a>
    </div>
    <p class="mt-3 mb-0 small">Já tem conta? <a class="link-light fw-semibold" href="${ROUTES.login}">Entrar</a></p>
  </section>

  <section class="row row-cols-1 row-cols-md-3 g-3 text-center mb-5" aria-label="Números da plataforma">
    ${STAT_CARDS.map(statCard).join('')}
  </section>

  <section class="mb-5">
    <h2 class="h3 text-center mb-4">Como funciona</h2>
    <div class="row row-cols-1 row-cols-md-3 g-4">${STEPS.map(step).join('')}</div>
  </section>

  <section class="row row-cols-1 row-cols-md-2 g-4 mb-5">
    ${audienceCard('Para candidatos', [
      'Veja todas as vagas abertas em um só lugar',
      'Descubra em quais vagas suas competências se encaixam',
      'Seja avaliado pelo que sabe, não por quem é',
    ], ROUTES.register, 'Cadastrar como candidato')}
    ${audienceCard('Para empresas', [
      'Conheça todos os candidatos cadastrados',
      'Veja quantos dominam cada competência',
      'Contrate com menos viés, olhando para as habilidades',
    ], ROUTES.registerEnterprise, 'Cadastrar como empresa')}
  </section>

  <section class="text-center">
    <h2 class="h5 mb-3">Competências que você encontra aqui</h2>
    <div class="d-flex flex-wrap justify-content-center gap-2">
      ${COMPETENCES.map((competence) => `<span class="badge rounded-pill text-bg-light border fs-6 fw-normal">${competence}</span>`).join('')}
    </div>
  </section>`;

export function homePage(): HTMLElement {
  const page = elementFromTemplate(TEMPLATE);
  const stats = getPlatformStats();
  STAT_CARDS.forEach(([stat]) => {
    requireElement(page, `[data-stat="${stat}"]`).textContent = String(stats[stat]);
  });
  return page;
}
