import { getLoggedCandidate, getLoggedEnterprise } from '../services/auth';
import { ROUTES, navigateTo } from '../navigation';
import type { Candidate, Enterprise } from '../types';

function redirectToLoginIfMissing<T>(account: T | undefined): T | undefined {
  if (!account) {
    navigateTo(ROUTES.login);
  }
  return account;
}

export function requireLoggedCandidate(): Candidate | undefined {
  return redirectToLoginIfMissing(getLoggedCandidate());
}

export function requireLoggedEnterprise(): Enterprise | undefined {
  return redirectToLoginIfMissing(getLoggedEnterprise());
}
