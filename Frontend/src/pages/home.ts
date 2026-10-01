import { el } from '../dom';
import { COMPETENCIES } from '../types';
import { getStats } from '../services';

const TEMPLATE = `
  <section class="hero rounded-4 text-white text-center px-3 py-5 mb-5">
    <h1 class="display-5 fw-bold">Bem-vindo ao Linketinder!</h1>
    <p class="lead mx-auto mb-4" style="max-width: 640px">
      O seu lugar para encontrar as melhores oportunidades de trabalho e os melhores talentos.
      Aqui ninguém é julgado pelo nome ou pela foto: candidatos e empresas se conhecem pelas competências.
    </p>
    <div class="d-flex flex-wrap justify-content-center gap-2">
      <a class="btn btn-light btn-lg" href="#/register">Quero uma vaga</a>
      <a class="btn btn-outline-light btn-lg" href="#/register?type=ENTERPRISE">Quero contratar</a>
    </div>
    <p class="mt-3 mb-0 small">Já tem conta? <a class="link-light fw-semibold" href="#/login">Entrar</a></p>
  </section>

  <section class="row row-cols-1 row-cols-md-3 g-3 text-center mb-5" aria-label="Números da plataforma">
    <div class="col"><div class="card h-100 shadow-sm"><div class="card-body">
      <p class="display-6 fw-bold text-primary mb-0" data-stat="vacancies"></p>
      <p class="text-body-secondary mb-0">vagas abertas</p>
    </div></div></div>
    <div class="col"><div class="card h-100 shadow-sm"><div class="card-body">
      <p class="display-6 fw-bold text-primary mb-0" data-stat="candidates"></p>
      <p class="text-body-secondary mb-0">candidatos</p>
    </div></div></div>
    <div class="col"><div class="card h-100 shadow-sm"><div class="card-body">
      <p class="display-6 fw-bold text-primary mb-0" data-stat="enterprises"></p>
      <p class="text-body-secondary mb-0">empresas</p>
    </div></div></div>
  </section>

  <section class="mb-5">
    <h2 class="h3 text-center mb-4">Como funciona</h2>
    <div class="row row-cols-1 row-cols-md-3 g-4">
      <div class="col">
        <span class="step rounded-circle bg-primary text-white fw-bold mb-2">1</span>
        <h3 class="h5">Crie sua conta</h3>
        <p class="text-body-secondary">Cadastre-se como candidato ou empresa e marque as competências que você tem ou procura.</p>
      </div>
      <div class="col">
        <span class="step rounded-circle bg-primary text-white fw-bold mb-2">2</span>
        <h3 class="h5">Conheça sem preconceito</h3>
        <p class="text-body-secondary">Candidatos veem as vagas sem saber a empresa; empresas veem os candidatos sem nome, só formação e competências.</p>
      </div>
      <div class="col">
        <span class="step rounded-circle bg-primary text-white fw-bold mb-2">3</span>
        <h3 class="h5">Dê match <span class="badge text-bg-warning align-middle">em breve</span></h3>
        <p class="text-body-secondary">Quando o candidato curtir a vaga e a empresa curtir o candidato, os dois lados se conhecem de verdade.</p>
      </div>
    </div>
  </section>

  <section class="row row-cols-1 row-cols-md-2 g-4 mb-5">
    <div class="col">
      <div class="card h-100 shadow-sm"><div class="card-body p-4">
        <h2 class="h4">Para candidatos</h2>
        <ul class="text-body-secondary">
          <li>Veja todas as vagas abertas em um só lugar</li>
          <li>Descubra em quais vagas suas competências se encaixam</li>
          <li>Seja avaliado pelo que sabe, não por quem é</li>
        </ul>
        <a class="btn btn-primary" href="#/register">Cadastrar como candidato</a>
      </div></div>
    </div>
    <div class="col">
      <div class="card h-100 shadow-sm"><div class="card-body p-4">
        <h2 class="h4">Para empresas</h2>
        <ul class="text-body-secondary">
          <li>Conheça todos os candidatos cadastrados</li>
          <li>Veja quantos dominam cada competência</li>
          <li>Contrate com menos viés, olhando para as habilidades</li>
        </ul>
        <a class="btn btn-primary" href="#/register?type=ENTERPRISE">Cadastrar como empresa</a>
      </div></div>
    </div>
  </section>

  <section class="text-center">
    <h2 class="h5 mb-3">Competências que você encontra aqui</h2>
    <div class="d-flex flex-wrap justify-content-center gap-2">
      ${COMPETENCIES.map((c) => `<span class="badge rounded-pill text-bg-light border fs-6 fw-normal">${c}</span>`).join('')}
    </div>
  </section>`;

export function homePage(): HTMLElement {
  const page = el('div');
  page.innerHTML = TEMPLATE;

  const stats = getStats();
  page.querySelector('[data-stat="vacancies"]')!.textContent = String(stats.vacancies);
  page.querySelector('[data-stat="candidates"]')!.textContent = String(stats.candidates);
  page.querySelector('[data-stat="enterprises"]')!.textContent = String(stats.enterprises);

  return page;
}
