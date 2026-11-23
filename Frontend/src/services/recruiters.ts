import * as storage from '../data/storage';
import { isEmailInUse } from './auth';
import { unlikeCandidate } from './likes';
import { RECRUITMENT_AREAS } from '../types';
import type { Enterprise, IsoDate, Recruiter, RecruitmentArea, RecruiterSummary } from '../types';

// Espelha RecruiterValidator e RecruiterService do back-end, com as mesmas mensagens de erro.
// As mensagens nunca repetem o valor recebido (CPF, e-mail).

export interface NewRecruiter {
  name: string;
  email: string;
  password: string;
  description: string;
  cpf: string;
  birthDate: IsoDate;
  area: string;
}

const MIN_AGE = 18;
const MIN_PASSWORD_LENGTH = 6;
const EMAIL_FORMAT = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
const CPF_FORMAT = /^\d{3}\.?\d{3}\.?\d{3}-?\d{2}$/;
const ISO_DATE_FORMAT = /^(\d{4})-(\d{2})-(\d{2})$/;
const PASSWORD_MESSAGE = `A senha deve ter pelo menos ${MIN_PASSWORD_LENGTH} caracteres`;

function require(condition: boolean, message: string): void {
  if (!condition) {
    throw new Error(message);
  }
}

function notFound(entityName: string, id: string): Error {
  return new Error(`${entityName} com ID ${id} não existe`);
}

function onlyDigits(value: string): string {
  return value.replace(/\D/g, '');
}

function checkDigit(base: string, firstWeight: number): number {
  const sum = [...base].reduce((total, digit, index) => total + Number(digit) * (firstWeight - index), 0);
  const remainder = sum % 11;
  return remainder < 2 ? 0 : 11 - remainder;
}

function isValidCpf(cpf: string): boolean {
  if (!CPF_FORMAT.test(cpf)) {
    return false;
  }
  const digits = onlyDigits(cpf);
  if (new Set(digits).size === 1) {
    return false;
  }
  const first = checkDigit(digits.slice(0, 9), 10);
  const second = checkDigit(digits.slice(0, 9) + first, 11);
  return digits.endsWith(`${first}${second}`);
}

function isValidPassword(password: string): boolean {
  return password.length >= MIN_PASSWORD_LENGTH;
}

// Anos completos até hoje, como o Period.between do back-end. Quem faz 18 hoje já tem 18.
function ageOn(birth: Date, today: Date): number {
  const hadBirthdayThisYear =
    today.getMonth() > birth.getMonth()
    || (today.getMonth() === birth.getMonth() && today.getDate() >= birth.getDate());
  return today.getFullYear() - birth.getFullYear() - (hadBirthdayThisYear ? 0 : 1);
}

function parseIsoDate(value: IsoDate): Date | undefined {
  const parts = ISO_DATE_FORMAT.exec(value);
  if (!parts) {
    return undefined;
  }
  const [year, month, day] = [Number(parts[1]), Number(parts[2]), Number(parts[3])];
  const date = new Date(year, month - 1, day);
  const isRealDate = date.getFullYear() === year && date.getMonth() === month - 1 && date.getDate() === day;
  return isRealDate ? date : undefined;
}

function validateBirthDate(birthDate: IsoDate): void {
  const birth = parseIsoDate(birthDate.trim());
  require(birth !== undefined, 'Data de nascimento é obrigatória');
  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  require(birth! <= today, 'Data de nascimento não pode estar no futuro');
  require(ageOn(birth!, today) >= MIN_AGE, `O recrutador deve ter pelo menos ${MIN_AGE} anos`);
}

// Sem diferenciar maiúsculas e sem espaços nas pontas, como RecruitmentArea#fromName
function toArea(name: string): RecruitmentArea | undefined {
  const trimmed = name.trim().toLowerCase();
  return trimmed === '' ? undefined : RECRUITMENT_AREAS.find((area) => area.toLowerCase() === trimmed);
}

function validateNewRecruiter(data: NewRecruiter): RecruitmentArea {
  require(data.name.trim() !== '', 'Nome é obrigatório');
  require(EMAIL_FORMAT.test(data.email), 'E-mail inválido');
  require(isValidPassword(data.password), PASSWORD_MESSAGE);
  require(isValidCpf(data.cpf), 'CPF inválido');
  validateBirthDate(data.birthDate);
  const area = toArea(data.area);
  require(area !== undefined, 'Área de recrutamento inválida');
  return area!;
}

function requireEnterprise(enterprise: Enterprise): void {
  if (!storage.enterprises.getAll().some((existing) => existing.id === enterprise.id)) {
    throw notFound('Empresa', enterprise.id);
  }
}

function toSummary({ id, name, email, area, description }: Recruiter): RecruiterSummary {
  return { id, name, email, area, description };
}

// Valida, confere a empresa e o e-mail entre todas as contas, e só então grava. Não inicia sessão.
export function createRecruiter(enterprise: Enterprise, data: NewRecruiter): RecruiterSummary {
  const area = validateNewRecruiter(data);
  requireEnterprise(enterprise);
  require(!isEmailInUse(data.email), 'E-mail já cadastrado');

  const recruiter: Recruiter = {
    id: crypto.randomUUID(),
    type: 'RECRUITER',
    enterpriseId: enterprise.id,
    name: data.name,
    email: data.email,
    password: data.password,
    description: data.description,
    cpf: onlyDigits(data.cpf),
    birthDate: data.birthDate.trim(),
    area,
  };
  storage.recruiters.add(recruiter);
  return toSummary(recruiter);
}

// Só os recrutadores da própria empresa
export function listOwnRecruiters(enterprise: Enterprise): RecruiterSummary[] {
  requireEnterprise(enterprise);
  return storage.recruiters
    .getAll()
    .filter((recruiter) => recruiter.enterpriseId === enterprise.id)
    .map(toSummary);
}

// Só o nome da empresa do recrutador, como contexto (spec 4.4): nada de CNPJ, e-mail, endereço ou outros dados dela.
export function getOwnEnterpriseName(recruiter: Recruiter): string {
  const enterprise = storage.enterprises.getAll().find((existing) => existing.id === recruiter.enterpriseId);
  if (!enterprise) {
    throw notFound('Empresa', recruiter.enterpriseId);
  }
  return enterprise.name;
}

// Recrutador inexistente ou de outra empresa: o mesmo erro, sem distinguir os dois casos.
// Cada curtida dele sai pela regra de "saiu o último" de likes.ts, antes de apagar o recrutador.
export function removeRecruiter(enterprise: Enterprise, recruiterId: string): void {
  const recruiter = storage.recruiters
    .getAll()
    .find((existing) => existing.id === recruiterId && existing.enterpriseId === enterprise.id);
  if (!recruiter) {
    throw notFound('Recrutador', recruiterId);
  }
  storage.recruiterLikes
    .getAll()
    .filter((like) => like.recruiterId === recruiter.id)
    .forEach((like) => unlikeCandidate(recruiter.id, like.candidateId));
  storage.recruiters.remove(recruiter.id);
}

// Só descrição e senha. Senha vazia mantém a atual; só a senha nova é validada.
export function updateOwnProfile(recruiter: Recruiter, description: string, password: string): RecruiterSummary {
  const current = storage.recruiters.getAll().find((existing) => existing.id === recruiter.id);
  if (!current) {
    throw notFound('Recrutador', recruiter.id);
  }
  if (password !== '') {
    require(isValidPassword(password), PASSWORD_MESSAGE);
  }
  storage.recruiters.update(current.id, password === '' ? { description } : { description, password });
  return toSummary({ ...current, description });
}
