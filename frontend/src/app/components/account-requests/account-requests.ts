import { Component, OnInit } from '@angular/core';
import { AccountRequestService } from '../../services/account-request.service';
import { CommonModule } from '@angular/common';

interface AccountRequest {
  email: string;
  name: string;
  phoneNumber: string;
  birthday: string;
  address: string;
  city: string;
  status: string;
  createdAt: string;
}

@Component({
  selector: 'app-account-requests',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './account-requests.html',
  styleUrl: './account-requests.css'
})
export class AccountRequestsComponent implements OnInit {
  requests: AccountRequest[] = [];

  constructor(private requestService: AccountRequestService) {}

  ngOnInit(): void {
    this.loadRequests();
  }

  loadRequests(): void {
    this.requestService.getRequests().subscribe({
      next: (data: AccountRequest[]) => {
        this.requests = data.filter((req: AccountRequest) => req.status === 'PENDING');
      },
      error: (err: any) => console.error('Failed to load requests', err)
    });
  }

  approve(email: string): void {
    this.requestService.approveRequest(email).subscribe({
      next: () => this.loadRequests(),
      error: (err: any) => console.error('Failed to approve request', err)
    });
  }

  reject(email: string): void {
    const reason = prompt('Enter reason for rejection:');
    if (reason) {
      this.requestService.rejectRequest(email, reason).subscribe({
        next: () => this.loadRequests(),
        error: (err: any) => console.error('Failed to reject request', err)
      });
    }
  }
}
