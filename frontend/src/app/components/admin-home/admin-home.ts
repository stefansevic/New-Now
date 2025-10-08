import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-home.html',
  styleUrls: ['./admin-home.css']
})
export class AdminHomeComponent implements OnInit {
  adminName: string | null = null;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.adminName = this.authService.getUserName();
  }
}
