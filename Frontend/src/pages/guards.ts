import { getLoggedCandidate, getLoggedEnterprise, getLoggedRecruiter } from '../services/auth';
import { ROUTES, navigateTo } from '../navigation';
import type { Candidate, Enterprise, Recruiter } from '../types';

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

export function requireLoggedRecruiter(): Recruiter | undefined {
  return redirectToLoginIfMissing(getLoggedRecruiter());
}
