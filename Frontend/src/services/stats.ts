import * as storage from '../data/storage';
import { listOpenVacancies } from './vacancies';

export interface PlatformStats {
  openVacancies: number;
  candidates: number;
  enterprises: number;
}

export function getPlatformStats(): PlatformStats {
  return {
    openVacancies: listOpenVacancies().length,
    candidates: storage.candidates.getAll().length,
    enterprises: storage.enterprises.getAll().length,
  };
}
