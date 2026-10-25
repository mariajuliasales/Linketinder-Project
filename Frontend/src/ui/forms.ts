import { COMPETENCES, STATES } from '../types';
import type { Competence } from '../types';

export interface FormReader {
  text(field: string): string;
  digits(field: string): string;
  competences(): Competence[];
}

export function readForm(form: HTMLFormElement): FormReader {
  const formData = new FormData(form);
  const text = (field: string): string => String(formData.get(field) ?? '').trim();
  return {
    text,
    digits: (field) => text(field).replace(/\D/g, ''),
    competences: () => formData.getAll('competences').map(String) as Competence[],
  };
}

export function stateSelect(id: string): string {
  return `
    <label class="form-label" for="${id}">UF</label>
    <select class="form-select" id="${id}" name="state" required>
      <option value="" selected disabled>UF</option>
      ${STATES.map((state) => `<option>${state}</option>`).join('')}
    </select>
    <div class="invalid-feedback">Escolha.</div>`;
}

export function competenceFieldset(legend: string, idPrefix: string): string {
  const checkboxes = COMPETENCES.map((competence, index) => `
    <div class="col form-check">
      <input class="form-check-input" type="checkbox" name="competences" value="${competence}" id="${idPrefix}-${index}">
      <label class="form-check-label" for="${idPrefix}-${index}">${competence}</label>
    </div>`).join('');
  return `
    <fieldset class="col-12">
      <legend class="form-label fs-6">${legend}</legend>
      <div class="row row-cols-2 row-cols-md-4 g-2">${checkboxes}</div>
      <div class="text-danger small mt-1" id="${idPrefix}-error" hidden>Selecione ao menos uma competência.</div>
    </fieldset>`;
}

export function isFormValid(form: HTMLFormElement, competencesError: HTMLElement, competences: Competence[]): boolean {
  const hasCompetence = competences.length > 0;
  competencesError.hidden = hasCompetence;
  form.classList.add('was-validated');
  return form.checkValidity() && hasCompetence;
}
