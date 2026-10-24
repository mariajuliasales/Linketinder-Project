import { createElement } from './dom';
import type { Competence } from '../types';

const MATCH_COLOR = 'text-bg-success';
const DEFAULT_COLOR = 'text-bg-secondary';

export function badge(text: string, color = DEFAULT_COLOR): HTMLSpanElement {
  return createElement('span', `badge ${color} me-1`, text);
}

export function competenceBadges(competences: Competence[], highlighted: Competence[] = []): HTMLSpanElement[] {
  return competences.map((competence) =>
    badge(competence, highlighted.includes(competence) ? MATCH_COLOR : DEFAULT_COLOR),
  );
}

export function lockedInfo(text: string): HTMLParagraphElement {
  return createElement('p', 'text-body-secondary mb-0 mt-2 border-top pt-2', `🔒 ${text}`);
}
