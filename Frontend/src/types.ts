export const COMPETENCES = [
  'Python', 'Java', 'Spring Framework', 'Angular', 'Groovy', 'JavaScript',
  'TypeScript', 'SQL', 'Docker', 'Kafka', 'React', 'Ilustrador',
] as const;
export type Competence = (typeof COMPETENCES)[number];

export const STATES = [
  'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG', 'PA',
  'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO',
] as const;

export const RECRUITMENT_AREAS = [
  'TI', 'RH', 'Marketing', 'Segurança da Informação', 'Financeiro', 'Comercial', 'Operações',
] as const;
export type RecruitmentArea = (typeof RECRUITMENT_AREAS)[number];

export type PersonType = 'CANDIDATE' | 'ENTERPRISE' | 'RECRUITER';
// O recrutador é cadastrado pela empresa, nunca pelo cadastro público
export type SelfRegisteredType = Exclude<PersonType, 'RECRUITER'>;
export type VacancyStatus = 'OPEN' | 'PAUSED' | 'CLOSED';
export type IsoDate = string;

export interface Address {
  cep: string;
  city: string;
  state: string;
  country: string;
}

interface AccountBase {
  id: string;
  type: PersonType;
  name: string;
  email: string;
  password: string;
  description: string;
}

export interface Person extends AccountBase {
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

// Sem endereço e sem competências
export interface Recruiter extends AccountBase {
  type: 'RECRUITER';
  enterpriseId: string;
  area: RecruitmentArea;
  cpf: string;
  birthDate: IsoDate;
}

export type Account = Candidate | Enterprise | Recruiter;

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

export interface CandidateLike {
  id: string;
  candidateId: string;
  vacancyId: string;
}

export interface RecruiterLike {
  id: string;
  recruiterId: string;
  candidateId: string;
}

// Curtida consolidada da empresa: existe enquanto algum recrutador dela curte o candidato
export interface EnterpriseLike {
  id: string;
  enterpriseId: string;
  candidateId: string;
}

export interface Match {
  id: string;
  candidateId: string;
  vacancyId: string;
}

// Tipos entregues a pages/. Sem cpf, birthDate, password, address, cnpj nem enterpriseId.

// Visto só pela própria empresa
export interface RecruiterSummary {
  id: string;
  name: string;
  email: string;
  area: RecruitmentArea;
  description: string;
}

export interface MatchedVacancy {
  id: string;
  title: string;
  status: VacancyStatus;
}

// reveal só existe quando há match entre o candidato e alguma vaga da empresa
export interface CandidateCard extends CandidateAnonymous {
  id: string;
  likedByMe: boolean;
  reveal?: {
    name: string;
    email: string;
    matchedVacancies: MatchedVacancy[];
  };
}

// likedByMe só na visão do candidato; a visão do recrutador (somente leitura) não tem o campo
export interface VacancyCard extends VacancyAnonymous {
  id: string;
  status: VacancyStatus;
  likedByMe?: boolean;
}

// enterprise só existe quando há match com a vaga
export interface LikedVacancyCard extends VacancyAnonymous {
  id: string;
  status: VacancyStatus;
  enterprise?: {
    name: string;
    email: string;
  };
}
