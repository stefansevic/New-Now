import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-register',
  templateUrl: './register.html',
  styleUrls: ['./register.css'],
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule]
})
export class RegisterComponent {
  registerForm: FormGroup;
  successMessage: string | null = null;
  errorMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      phoneNumber: ['', Validators.required],
      birthday: ['', Validators.required],
      address: ['', Validators.required],
      city: ['', Validators.required]
    });
  }

  onSubmit(): void {
    this.successMessage = null;
    this.errorMessage = null;
    if (this.registerForm.valid) {
      this.authService.requestRegistration(this.registerForm.value).subscribe({
        next: () => {
          this.successMessage = 'Vaš zahtev za registraciju je uspešno poslat. Dobićete email kada bude odobren.';
          this.registerForm.reset();
        },
        error: (err) => {
          this.errorMessage = 'Došlo je do greške. Moguće je da email već postoji.';
          console.error(err);
        }
      });
    }
  }
}
