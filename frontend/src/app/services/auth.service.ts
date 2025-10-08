import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { jwtDecode } from "jwt-decode";

interface AuthToken {
  roles: string[];
  sub: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private authUrl = 'http://localhost:8081/api/auth';
  private requestUrl = 'http://localhost:8081/api/account-requests';

  constructor(private http: HttpClient) { }

  requestRegistration(user: any): Observable<any> {
    return this.http.post(this.requestUrl, user);
  }

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.authUrl}/login`, credentials);
  }

  storeToken(token: string): void {
    localStorage.setItem('authToken', token);
  }

  logout(): void {
    localStorage.removeItem('authToken');
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('authToken');
  }

  getUserRoles(): string[] | null {
    const token = localStorage.getItem('authToken');
    if (token) {
      try {
        const decodedToken: AuthToken = jwtDecode(token);
        return decodedToken.roles;
      } catch (e) {
        console.error('Error decoding token', e);
        return null;
      }
    }
    return null;
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

  isAdmin(): boolean {
    const roles = this.getUserRoles();
    return roles ? roles.includes('ADMIN') : false;
  }
}
