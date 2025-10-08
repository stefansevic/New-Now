import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule]
})
export class LoginComponent {
  loginForm: FormGroup;
  errorMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    this.errorMessage = null;
    if (this.loginForm.valid) {
      this.authService.login(this.loginForm.value).subscribe({
        next: (response) => {
          console.log('LoginComponent response:', response); // Original log
          this.authService.storeToken(response.token);

          console.log('Token after store:', response.token);
          const roles = this.authService.getUserRoles();
          console.log('Roles from token:', roles);
          const isAdmin = this.authService.isAdmin();
          console.log('Is Admin check:', isAdmin);

          if (isAdmin) {
            this.router.navigate(['/admin/requests']);
          } else {
            this.router.navigate(['/home']);
          }
        },
        error: (err) => {
          this.errorMessage = 'Pogrešan email ili lozinka.';
          console.error(err);
        }
      });
    }
  }
}
