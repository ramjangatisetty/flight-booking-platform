import { TripType, CabinClass, Currency } from './enums';

export interface Airport {
  iata: string;
  city: string;
  name: string;
  country?: string;
}

export interface SearchCriteria {
  tripType: TripType;
  origin: Airport;
  destination: Airport;
  departDate: Date;
  returnDate?: Date;
  paxAdults: number;
  paxChildren: number;
  cabinClass: CabinClass;
  currency: Currency;
}

export interface QueryParams {
  tripType: string;
  from: string;
  to: string;
  departDate: string;
  returnDate?: string;
  paxAdults: string;
  paxChildren: string;
  cabinClass: string;
  currency: string;
}
