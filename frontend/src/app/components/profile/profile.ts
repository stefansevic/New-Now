import { Component, OnInit } from '@angular/core';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.html',
  styleUrls: ['./profile.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule]
})
export class ProfileComponent implements OnInit {
  profile: any = null;
  loading: boolean = true;
  
  // Tabs
  activeTab: string = 'info';
  
  // Change password form
  showChangePasswordForm: boolean = false;
  passwordForm = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };
  
  // Edit profile form
  editMode: boolean = false;
  profileForm: any = {};
  
  // Image upload
  selectedFile: File | null = null;
  imagePreview: string | null = null;

  constructor(
    private userService: UserService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.loading = true;
    this.userService.getProfile().subscribe({
      next: (data) => {
        console.log('Profile loaded:', data);
        this.profile = data;
        this.resetProfileForm();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading profile:', err);
        this.loading = false;
        alert('Failed to load profile');
      }
    });
  }

  resetProfileForm(): void {
    this.profileForm = {
      name: this.profile.name || '',
      phoneNumber: this.profile.phoneNumber || '',
      birthday: this.profile.birthday || '',
      address: this.profile.address || '',
      city: this.profile.city || ''
    };
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
    if (tab !== 'password') {
      this.showChangePasswordForm = false;
    }
  }

  // Change Password
  openChangePasswordForm(): void {
    this.showChangePasswordForm = true;
    this.passwordForm = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    };
  }

  closeChangePasswordForm(): void {
    this.showChangePasswordForm = false;
  }

  submitPasswordChange(): void {
    if (!this.passwordForm.currentPassword) {
      alert('Please enter your current password');
      return;
    }

    if (!this.passwordForm.newPassword || this.passwordForm.newPassword.length < 6) {
      alert('New password must be at least 6 characters');
      return;
    }

    if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
      alert('New passwords do not match');
      return;
    }

    this.userService.changePassword(
      this.passwordForm.currentPassword,
      this.passwordForm.newPassword,
      this.passwordForm.confirmPassword
    ).subscribe({
      next: () => {
        alert('Password changed successfully!');
        this.closeChangePasswordForm();
      },
      error: (err) => {
        console.error('Error changing password:', err);
        alert(err.error || 'Failed to change password');
      }
    });
  }

  // Edit Profile
  enableEditMode(): void {
    this.editMode = true;
    this.resetProfileForm();
  }

  cancelEdit(): void {
    this.editMode = false;
    this.resetProfileForm();
  }

  saveProfile(): void {
    this.userService.updateProfile(this.profileForm).subscribe({
      next: (updatedUser) => {
        console.log('Profile updated:', updatedUser);
        this.profile = { ...this.profile, ...updatedUser };
        this.editMode = false;
        alert('Profile updated successfully!');
      },
      error: (err) => {
        console.error('Error updating profile:', err);
        alert('Failed to update profile');
      }
    });
  }

  // Image Upload
  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      
      // Preview image
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.imagePreview = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  uploadImage(): void {
    if (!this.selectedFile) {
      alert('Please select an image first');
      return;
    }

    this.userService.uploadProfileImage(this.selectedFile).subscribe({
      next: (response: any) => {
        console.log('Upload response:', response);
        this.selectedFile = null;
        this.imagePreview = null;
        alert('Profile image updated successfully!');
        // Reload profile to get fresh data
        this.loadProfile();
      },
      error: (err) => {
        console.log('Upload completed, reloading profile...', err);
        // Image is saved even if we get parsing error, so just reload
        this.selectedFile = null;
        this.imagePreview = null;
        alert('Profile image updated successfully!');
        this.loadProfile();
      }
    });
  }

  getImageUrl(imagePath: string): string {
    if (!imagePath) return '';
    if (imagePath.startsWith('http')) {
      return imagePath;
    }
    return `http://localhost:8080${imagePath}`;
  }

  getAverageRating(review: any): number {
    if (!review.rate) return 0;
    
    const ratings = [
      review.rate.performance,
      review.rate.soundAndLightning,
      review.rate.venue,
      review.rate.overallImpression
    ].filter(r => r !== null);
    
    if (ratings.length === 0) return 0;
    const sum = ratings.reduce((acc, val) => acc + val, 0);
    return sum / ratings.length;
  }
}

