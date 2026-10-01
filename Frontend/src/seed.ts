import type { Candidate, Enterprise, Vacancy } from './types';

const PASSWORD = '123456';

export const candidatesSeed: Candidate[] = [
  {
    id: 'candidate-1', type: 'CANDIDATE', name: 'Maria Julia Sales', email: 'mariajuliasales@gmail.com', password: PASSWORD,
    cpf: '12345678900', birthDate: '2002-03-15', training: 'Ciência da Computação', description: 'Software Engineer',
    address: { cep: '36700386', city: 'São Paulo', state: 'SP', country: 'Brasil' }, competencies: ['Java', 'Groovy'],
  },
  {
    id: 'candidate-2', type: 'CANDIDATE', name: 'Lucas Andrade Silveira', email: 'lucas.andrade@gmail.com', password: PASSWORD,
    cpf: '38459201080', birthDate: '2000-07-02', training: 'Engenharia de Software', description: 'Backend Developer',
    address: { cep: '30110010', city: 'Belo Horizonte', state: 'MG', country: 'Brasil' }, competencies: ['Java', 'Spring Framework'],
  },
  {
    id: 'candidate-3', type: 'CANDIDATE', name: 'Beatriz Lima Rocha', email: 'beatriz.rocha@outlook.com', password: PASSWORD,
    cpf: '83741920040', birthDate: '1997-05-21', training: 'Análise e Desenvolvimento de Sistemas', description: 'Full Stack Engineer',
    address: { cep: '22041001', city: 'Rio de Janeiro', state: 'RJ', country: 'Brasil' }, competencies: ['Groovy', 'JavaScript'],
  },
  {
    id: 'candidate-4', type: 'CANDIDATE', name: 'Gabriel Santos Oliveira', email: 'gabriel.santos@dev.com', password: PASSWORD,
    cpf: '19283746030', birthDate: '2003-01-10', training: 'Estatística', description: 'Data Engineer',
    address: { cep: '80010000', city: 'Curitiba', state: 'PR', country: 'Brasil' }, competencies: ['Python', 'SQL', 'Docker'],
  },
  {
    id: 'candidate-5', type: 'CANDIDATE', name: 'Fernanda Costa Souza', email: 'fernanda.souza@gmail.com', password: PASSWORD,
    cpf: '56473829000', birthDate: '1995-08-30', training: 'Redes de Computadores', description: 'DevOps Engineer',
    address: { cep: '88010000', city: 'Florianópolis', state: 'SC', country: 'Brasil' }, competencies: ['Docker', 'Angular', 'JavaScript'],
  },
];

export const enterprisesSeed: Enterprise[] = [
  {
    id: 'enterprise-1', type: 'ENTERPRISE', name: 'TechSolutions Brasil', email: 'contato@techsolutions.com.br', password: PASSWORD,
    cnpj: '11222333000181', description: 'Desenvolvimento de software sob medida',
    address: { cep: '01310000', city: 'São Paulo', state: 'SP', country: 'Brasil' }, competencies: ['Java', 'Spring Framework'],
  },
  {
    id: 'enterprise-2', type: 'ENTERPRISE', name: 'Inovação Digital LTDA', email: 'rh@inovacaodigital.io', password: PASSWORD,
    cnpj: '22333444000192', description: 'Consultoria em transformação digital',
    address: { cep: '30110010', city: 'Belo Horizonte', state: 'MG', country: 'Brasil' }, competencies: ['Groovy', 'Docker'],
  },
  {
    id: 'enterprise-3', type: 'ENTERPRISE', name: 'DataAnalytics Global', email: 'carreiras@dataanalytics.com', password: PASSWORD,
    cnpj: '33444555000103', description: 'Soluções de dados e Big Data',
    address: { cep: '22041001', city: 'Rio de Janeiro', state: 'RJ', country: 'Brasil' }, competencies: ['Python', 'SQL'],
  },
  {
    id: 'enterprise-4', type: 'ENTERPRISE', name: 'CloudScale Services', email: 'jobs@cloudscale.com', password: PASSWORD,
    cnpj: '44555666000114', description: 'Infraestrutura multicloud',
    address: { cep: '88010000', city: 'Florianópolis', state: 'SC', country: 'Brasil' }, competencies: ['Java', 'Docker'],
  },
  {
    id: 'enterprise-5', type: 'ENTERPRISE', name: 'FrontCraft Studios', email: 'talent@frontcraft.dev', password: PASSWORD,
    cnpj: '55666777000125', description: 'Experiências web modernas e acessíveis',
    address: { cep: '80010000', city: 'Curitiba', state: 'PR', country: 'Brasil' }, competencies: ['React', 'JavaScript'],
  },
];

export const vacanciesSeed: Vacancy[] = [
  {
    id: 'vacancy-1', enterpriseId: 'enterprise-1', title: 'Desenvolvedor Full Stack Java/Spring', status: 'OPEN',
    description: 'Desenvolvimento ponta a ponta de produtos SaaS de alta escala.',
    city: 'São Paulo', state: 'SP', competencies: ['Java', 'Spring Framework'],
  },
  {
    id: 'vacancy-2', enterpriseId: 'enterprise-2', title: 'Engenheiro de Software Groovy', status: 'OPEN',
    description: 'Aplicações corporativas de alta performance e automação.',
    city: 'Belo Horizonte', state: 'MG', competencies: ['Groovy', 'Docker'],
  },
  {
    id: 'vacancy-3', enterpriseId: 'enterprise-3', title: 'Cientista de Dados', status: 'OPEN',
    description: 'Modelos preditivos, pipelines de dados e consultas SQL complexas.',
    city: 'Rio de Janeiro', state: 'RJ', competencies: ['Python', 'SQL'],
  },
  {
    id: 'vacancy-4', enterpriseId: 'enterprise-4', title: 'Especialista Cloud & DevOps', status: 'OPEN',
    description: 'Infraestrutura distribuída em nuvem e conteinerização de serviços.',
    city: 'Florianópolis', state: 'SC', competencies: ['Java', 'Docker'],
  },
  {
    id: 'vacancy-5', enterpriseId: 'enterprise-5', title: 'Desenvolvedor Frontend React', status: 'OPEN',
    description: 'Interfaces web modernas, responsivas e integradas a serviços REST.',
    city: 'Curitiba', state: 'PR', competencies: ['React', 'JavaScript'],
  },
];
