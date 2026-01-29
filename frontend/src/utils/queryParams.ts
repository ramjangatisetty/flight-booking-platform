import type { SearchCriteria, QueryParams, Airport } from '../domain/types';
import { TripType, CabinClass, Currency } from '../domain/enums';

export function formatLocalYmd(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function parseYmdToLocalDate(ymd: string): Date {
  const [year, month, day] = ymd.split('-').map(Number);
  return new Date(year, month - 1, day);
}

export function searchCriteriaToQueryParams(criteria: SearchCriteria): QueryParams {
  return {
    tripType: criteria.tripType,
    from: criteria.origin.iata,
    to: criteria.destination.iata,
    departDate: formatLocalYmd(criteria.departDate),
    returnDate: criteria.returnDate ? formatLocalYmd(criteria.returnDate) : undefined,
    paxAdults: criteria.paxAdults.toString(),
    paxChildren: criteria.paxChildren.toString(),
    cabinClass: criteria.cabinClass,
    currency: criteria.currency,
  };
}

export function queryParamsToSearchCriteria(
  params: URLSearchParams,
  airports: Airport[]
): Partial<SearchCriteria> {
  const findAirport = (iata: string) => airports.find(a => a.iata === iata);
  
  return {
    tripType: params.get('tripType') as TripType,
    origin: findAirport(params.get('from') || ''),
    destination: findAirport(params.get('to') || ''),
    departDate: params.get('departDate') ? parseYmdToLocalDate(params.get('departDate')!) : undefined,
    returnDate: params.get('returnDate') ? parseYmdToLocalDate(params.get('returnDate')!) : undefined,
    paxAdults: parseInt(params.get('paxAdults') || '1'),
    paxChildren: parseInt(params.get('paxChildren') || '0'),
    cabinClass: params.get('cabinClass') as CabinClass,
    currency: params.get('currency') as Currency,
  };
}
