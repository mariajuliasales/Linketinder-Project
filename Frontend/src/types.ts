export const COMPETENCES = [
  'Python', 'Java', 'Spring Framework', 'Angular', 'Groovy', 'JavaScript',
  'TypeScript', 'SQL', 'Docker', 'Kafka', 'React', 'Ilustrador',
] as const;
export type Competence = (typeof COMPETENCES)[number];

export const STATES = [
  'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG', 'PA',
  'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO',
] as const;

export type PersonType = 'CANDIDATE' | 'ENTERPRISE';
export type VacancyStatus = 'OPEN' | 'PAUSED' | 'CLOSED';
export type IsoDate = string;

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
  password: string;
  description: string;
  address: Address;
  competences: Competence[];
}

export interface Candidate extends Person {
  type: 'CANDIDATE';
  cpf: string;
  birthDate: IsoDate;
  training: string;
}

export interface Enterprise extends Person {
  type: 'ENTERPRISE';
  cnpj: string;
}

export type Account = Candidate | Enterprise;

export interface Vacancy {
  id: string;
  enterpriseId: string;
  title: string;
  description: string;
  status: VacancyStatus;
  city: string;
  state: string;
  competences: Competence[];
}

export interface CandidateAnonymous {
  training: string;
  description: string;
  competences: Competence[];
}

export interface VacancyAnonymous {
  title: string;
  description: string;
  city: string;
  state: string;
  competences: Competence[];
}
