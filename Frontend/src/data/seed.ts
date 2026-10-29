import type { Address, Candidate, Enterprise, Vacancy } from '../types';

const SEED_PASSWORD = '123456';

const CANDIDATE_DEFAULTS = { type: 'CANDIDATE', password: SEED_PASSWORD } as const;
const ENTERPRISE_DEFAULTS = { type: 'ENTERPRISE', password: SEED_PASSWORD } as const;
const OPEN_VACANCY = { status: 'OPEN' } as const;

function address(cep: string, city: string, state: string): Address {
  return { cep, city, state, country: 'Brasil' };
}

export const candidatesSeed: Candidate[] = [
  {
    ...CANDIDATE_DEFAULTS, id: 'candidate-1', name: 'Maria Julia Sales', email: 'mariajuliasales@gmail.com',
    cpf: '12345678909', birthDate: '2002-03-15', training: 'Ciência da Computação', description: 'Software Engineer',
    address: address('36700386', 'São Paulo', 'SP'), competences: ['Java', 'Groovy'],
  },
  {
    ...CANDIDATE_DEFAULTS, id: 'candidate-2', name: 'Lucas Andrade Silveira', email: 'lucas.andrade@gmail.com',
    cpf: '38459201066', birthDate: '2000-07-02', training: 'Engenharia de Software', description: 'Backend Developer',
    address: address('30110010', 'Belo Horizonte', 'MG'), competences: ['Java', 'Spring Framework'],
  },
  {
    ...CANDIDATE_DEFAULTS, id: 'candidate-3', name: 'Beatriz Lima Rocha', email: 'beatriz.rocha@outlook.com',
    cpf: '83741920037', birthDate: '1997-05-21', training: 'Análise e Desenvolvimento de Sistemas',
    description: 'Full Stack Engineer', address: address('22041001', 'Rio de Janeiro', 'RJ'),
    competences: ['Groovy', 'JavaScript'],
  },
  {
    ...CANDIDATE_DEFAULTS, id: 'candidate-4', name: 'Gabriel Santos Oliveira', email: 'gabriel.santos@dev.com',
    cpf: '19283746031', birthDate: '2003-01-10', training: 'Estatística', description: 'Data Engineer',
    address: address('80010000', 'Curitiba', 'PR'), competences: ['Python', 'SQL', 'Docker'],
  },
  {
    ...CANDIDATE_DEFAULTS, id: 'candidate-5', name: 'Fernanda Costa Souza', email: 'fernanda.souza@gmail.com',
    cpf: '56473829083', birthDate: '1995-08-30', training: 'Redes de Computadores', description: 'DevOps Engineer',
    address: address('88010000', 'Florianópolis', 'SC'), competences: ['Docker', 'Angular', 'JavaScript'],
  },
  {
    ...CANDIDATE_DEFAULTS, id: 'candidate-6', name: 'Sandubinha da Silva', email: 'sandubinha@email.com',
    cpf: '52998224725', birthDate: '1999-11-20', training: 'Sistemas de Informação',
    description: 'Desenvolvedor apaixonado por backend e por bons lanches.', address: address('74000000', 'Goiânia', 'GO'),
    competences: ['Java', 'Spring Framework', 'Angular', 'Ilustrador'],
  },
];

export const enterprisesSeed: Enterprise[] = [
  {
    ...ENTERPRISE_DEFAULTS, id: 'enterprise-1', name: 'TechSolutions Brasil', email: 'contato@techsolutions.com.br',
    cnpj: '11222333000181', description: 'Desenvolvimento de software sob medida',
    address: address('01310000', 'São Paulo', 'SP'), competences: ['Java', 'Spring Framework'],
  },
  {
    ...ENTERPRISE_DEFAULTS, id: 'enterprise-2', name: 'Inovação Digital LTDA', email: 'rh@inovacaodigital.io',
    cnpj: '22333444000181', description: 'Consultoria em transformação digital',
    address: address('30110010', 'Belo Horizonte', 'MG'), competences: ['Groovy', 'Docker'],
  },
  {
    ...ENTERPRISE_DEFAULTS, id: 'enterprise-3', name: 'DataAnalytics Global', email: 'carreiras@dataanalytics.com',
    cnpj: '33444555000181', description: 'Soluções de dados e Big Data',
    address: address('22041001', 'Rio de Janeiro', 'RJ'), competences: ['Python', 'SQL'],
  },
  {
    ...ENTERPRISE_DEFAULTS, id: 'enterprise-4', name: 'CloudScale Services', email: 'jobs@cloudscale.com',
    cnpj: '44555666000181', description: 'Infraestrutura multicloud',
    address: address('88010000', 'Florianópolis', 'SC'), competences: ['Java', 'Docker'],
  },
  {
    ...ENTERPRISE_DEFAULTS, id: 'enterprise-5', name: 'FrontCraft Studios', email: 'talent@frontcraft.dev',
    cnpj: '55666777000181', description: 'Experiências web modernas e acessíveis',
    address: address('80010000', 'Curitiba', 'PR'), competences: ['React', 'JavaScript'],
  },
  {
    ...ENTERPRISE_DEFAULTS, id: 'enterprise-6', name: 'Pastelsoft', email: 'pastelzinho.frito@pastelsoft.com.br',
    cnpj: '98765432000198', description: 'ERPs para redes de restaurantes e distribuidoras de bebida de todo o país.',
    address: address('74000000', 'Goiânia', 'GO'), competences: ['Spring Framework', 'Angular'],
  },
];

export const vacanciesSeed: Vacancy[] = [
  {
    ...OPEN_VACANCY, id: 'vacancy-1', enterpriseId: 'enterprise-1', title: 'Desenvolvedor Full Stack Java/Spring',
    description: 'Desenvolvimento ponta a ponta de produtos SaaS de alta escala.',
    city: 'São Paulo', state: 'SP', competences: ['Java', 'Spring Framework'],
  },
  {
    ...OPEN_VACANCY, id: 'vacancy-2', enterpriseId: 'enterprise-2', title: 'Engenheiro de Software Groovy',
    description: 'Aplicações corporativas de alta performance e automação.',
    city: 'Belo Horizonte', state: 'MG', competences: ['Groovy', 'Docker'],
  },
  {
    ...OPEN_VACANCY, id: 'vacancy-3', enterpriseId: 'enterprise-3', title: 'Cientista de Dados',
    description: 'Modelos preditivos, pipelines de dados e consultas SQL complexas.',
    city: 'Rio de Janeiro', state: 'RJ', competences: ['Python', 'SQL'],
  },
  {
    ...OPEN_VACANCY, id: 'vacancy-4', enterpriseId: 'enterprise-4', title: 'Especialista Cloud & DevOps',
    description: 'Infraestrutura distribuída em nuvem e conteinerização de serviços.',
    city: 'Florianópolis', state: 'SC', competences: ['Java', 'Docker'],
  },
  {
    ...OPEN_VACANCY, id: 'vacancy-5', enterpriseId: 'enterprise-5', title: 'Desenvolvedor Frontend React',
    description: 'Interfaces web modernas, responsivas e integradas a serviços REST.',
    city: 'Curitiba', state: 'PR', competences: ['React', 'JavaScript'],
  },
  {
    ...OPEN_VACANCY, id: 'vacancy-6', enterpriseId: 'enterprise-6', title: 'Desenvolvedor Backend Spring',
    description: 'APIs do ERP para redes de restaurantes, usando Spring Framework.',
    city: 'Goiânia', state: 'GO', competences: ['Java', 'Spring Framework'],
  },
  {
    ...OPEN_VACANCY, id: 'vacancy-7', enterpriseId: 'enterprise-6', title: 'Desenvolvedor Frontend Angular',
    description: 'Telas do ERP para distribuidoras de bebida, usando Angular.',
    city: 'São Paulo', state: 'SP', competences: ['Angular', 'TypeScript'],
  },
  {
    id: 'vacancy-8', enterpriseId: 'enterprise-6', title: 'Estágio em Suporte ao ERP', status: 'CLOSED',
    description: 'Atendimento e suporte aos clientes do ERP.',
    city: 'Goiânia', state: 'GO', competences: ['SQL'],
  },
];
