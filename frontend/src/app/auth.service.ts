import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface Account {
  id: number;
  profileId: number | null;
  name: string;
  email: string;
  role: 'PATIENT' | 'DOCTOR' | 'NURSE' | 'ADMIN' | string;
}

export interface SignupPayload {
  role: string;
  name: string;
  email: string;
  password: string;
  age?: number | null;
  disease?: string;
  experience?: number | null;
  department?: string;
  phoneNumber?: string;
  adminSignupCode?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/auth';

  login(credentials: { email: string; password: string }): Observable<Account> {
    return this.http.post<Account>(`${this.baseUrl}/login`, credentials, { withCredentials: true });
  }

  signup(payload: SignupPayload): Observable<Account> {
    return this.http.post<Account>(`${this.baseUrl}/signup`, payload, { withCredentials: true });
  }

  me(): Observable<Account> {
    return this.http.get<Account>(`${this.baseUrl}/me`, { withCredentials: true });
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/logout`, {}, { withCredentials: true });
  }

  list<T>(resource: string): Observable<T[]> {
    return this.http.get<T[]>(`/api/${resource}`, { withCredentials: true });
  }

  get<T>(resource: string, id: number): Observable<T> {
    return this.http.get<T>(`/api/${resource}/${id}`, { withCredentials: true });
  }

  create<T>(resource: string, payload: unknown): Observable<T> {
    return this.http.post<T>(`/api/${resource}`, payload, { withCredentials: true });
  }

  update<T>(resource: string, id: number, payload: unknown): Observable<T> {
    return this.http.put<T>(`/api/${resource}/${id}`, payload, { withCredentials: true });
  }

  remove(resource: string, id: number): Observable<void> {
    return this.http.delete<void>(`/api/${resource}/${id}`, { withCredentials: true });
  }

  assignedNurses(patientId: number): Observable<Row[]> {
    return this.http.get<Row[]>(`/api/patients/${patientId}/nurses`, { withCredentials: true });
  }

  assignNurse(patientId: number, nurseId: number): Observable<unknown> {
    return this.http.put(`/api/patients/${patientId}/nurses/${nurseId}`, {}, { withCredentials: true });
  }

  unassignNurse(patientId: number, nurseId: number): Observable<unknown> {
    return this.http.delete(`/api/patients/${patientId}/nurses/${nurseId}`, { withCredentials: true });
  }
}

type Row = Record<string, any>;
