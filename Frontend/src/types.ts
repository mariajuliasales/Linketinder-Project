export const COMPETENCIES = [
  'Python', 'Java', 'Spring Framework', 'Angular', 'Groovy', 'JavaScript',
  'TypeScript', 'SQL', 'Docker', 'Kafka', 'React',
] as const;
export type Competence = (typeof COMPETENCIES)[number];

export const STATES = [
  'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG', 'PA',
  'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO',
] as const;

export type PersonType = 'CANDIDATE' | 'ENTERPRISE';


export interface Address {
  cep: string;
  city: string;
  state: string;
  country: string;
}

export interface Person {
  id: string;
  type: PersonType;
  name: string;
  email: string;
  password: string; // sem criptografia por enquanto
  description: string;
  address: Address;
  competencies: Competence[];
}

export interface Candidate extends Person {
  type: 'CANDIDATE';
  cpf: string;
  birthDate: string; // YYYY-MM-DD
  training: string;
}

export interface Enterprise extends Person {
  type: 'ENTERPRISE';
  cnpj: string;
}

export interface Vacancy {
  id: string;
  enterpriseId: string;
  title: string;
  description: string;
  status: 'OPEN' | 'PAUSED' | 'CLOSED';
  city: string;
  state: string;
  competencies: Competence[];
}

// O que um lado pode ver do outro
export interface CandidateAnonymous {
  training: string;
  description: string;
  competencies: Competence[];
}

export interface VacancyAnonymous {
  title: string;
  description: string;
  city: string;
  state: string;
  competencies: Competence[];
}
