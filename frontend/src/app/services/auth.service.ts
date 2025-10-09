import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { jwtDecode } from "jwt-decode";
import { tap } from 'rxjs/operators';

interface AuthToken {
  roles: string[];
  sub: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private authUrl = '/api/auth';
  private requestUrl = '/api/account-requests';

  private loggedIn = new BehaviorSubject<boolean>(this.isTokenPresent());
  private isAdminUser = new BehaviorSubject<boolean>(this.hasAdminRole());

  loggedIn$ = this.loggedIn.asObservable();
  isAdmin$ = this.isAdminUser.asObservable();

  constructor(private http: HttpClient) { }

  private isTokenPresent(): boolean {
    return !!localStorage.getItem('authToken');
  }

  private hasAdminRole(): boolean {
    const token = localStorage.getItem('authToken');
    if (token) {
      try {
        const decodedToken: AuthToken = jwtDecode(token);
        return decodedToken.roles && decodedToken.roles.includes('ROLE_ADMIN');
      } catch (e) {
        return false;
      }
    }
    return false;
  }

  private updateLoginState(): void {
    this.loggedIn.next(this.isTokenPresent());
    this.isAdminUser.next(this.hasAdminRole());
  }

  requestRegistration(user: any): Observable<any> {
    return this.http.post(this.requestUrl, user);
  }

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.authUrl}/login`, credentials).pipe(
      tap((response: any) => {
        this.storeToken(response.token);
      })
    );
  }

  storeToken(token: string): void {
    localStorage.setItem('authToken', token);
    this.updateLoginState();
  }

  logout(): void {
    localStorage.removeItem('authToken');
    this.updateLoginState();
  }

  isLoggedIn(): boolean {
    return this.loggedIn.getValue();
  }

  isAdmin(): boolean {
    return this.isAdminUser.getValue();
  }

  getUserName(): string | null {
    const token = localStorage.getItem('authToken');
    if (token) {
      try {
        const decodedToken: AuthToken = jwtDecode(token);
        return decodedToken.sub;
      } catch (e) {
        console.error('Error decoding token', e);
        return null;
      }
    }
    return null;
  }
}
