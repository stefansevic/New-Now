import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { LocationService } from '../../services/location.service';
import { EventService } from '../../services/event.service';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-location-details',
  templateUrl: './location-details.html',
  styleUrls: ['./location-details.css'],
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule]
})
export class LocationDetailsComponent implements OnInit, OnDestroy {
  location: any;
  events: any[] = [];
  isAdmin: boolean = false;
  isManager: boolean = false;
  isLoggedIn: boolean = false;
  showEventForm: boolean = false;
  editingEvent: any = null;
  
  eventForm = {
    name: '',
    address: '',
    type: '',
    date: '',
    price: null as number | null,
    recurrent: false,
    imagePath: ''
  };

  private adminSubscription!: Subscription;
  private loginSubscription!: Subscription;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private locationService: LocationService,
    private eventService: EventService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.adminSubscription = this.authService.isAdmin$.subscribe(isAdmin => {
      this.isAdmin = isAdmin;
    });

    this.loginSubscription = this.authService.loggedIn$.subscribe(loggedIn => {
      this.isLoggedIn = loggedIn;
    });

    const name = this.route.snapshot.paramMap.get('name');
    if (name) {
      this.loadLocation(name);
      this.loadEvents(name);
      this.checkIfManager(name);
    }
  }

  loadLocation(name: string): void {
    this.locationService.getLocationDetails(name).subscribe({
      next: (data) => this.location = data,
      error: (err) => console.error(err)
    });
  }

  loadEvents(locationName: string): void {
    this.eventService.getEventsByLocation(locationName).subscribe({
      next: (data) => this.events = data,
      error: (err) => console.error(err)
    });
  }

  checkIfManager(locationName: string): void {
    if (!this.isLoggedIn) {
      this.isManager = false;
      return;
    }
    
    this.locationService.getMyManagedLocations().subscribe({
      next: (locations) => {
        this.isManager = locations.some(loc => loc.location.name === locationName);
      },
      error: () => this.isManager = false
    });
  }

  ngOnDestroy(): void {
    if (this.adminSubscription) {
      this.adminSubscription.unsubscribe();
    }
    if (this.loginSubscription) {
      this.loginSubscription.unsubscribe();
    }
  }

  deleteLocation(): void {
    if (confirm('Are you sure you want to delete this location?')) {
      this.locationService.deleteLocation(this.location.location.name).subscribe({
        next: () => {
          console.log('Location deleted successfully');
          this.router.navigate(['/locations']);
        },
        error: (err) => {
          console.error('Error deleting location:', err);
          alert('Failed to delete location. Please try again.');
        }
      });
    }
  }

  getImageUrl(imagePath: string): string {
    if (imagePath.startsWith('http')) {
      return imagePath;
    }
    return `http://localhost:8080${imagePath}`;
  }

  // Event management methods
  openEventForm(): void {
    this.showEventForm = true;
    this.editingEvent = null;
    this.resetEventForm();
  }

  editEvent(event: any): void {
    this.showEventForm = true;
    this.editingEvent = event;
    this.eventForm = {
      name: event.name,
      address: event.address,
      type: event.type,
      date: event.date,
      price: event.price,
      recurrent: event.recurrent,
      imagePath: event.imagePath || ''
    };
  }

  resetEventForm(): void {
    this.eventForm = {
      name: '',
      address: '',
      type: '',
      date: '',
      price: null,
      recurrent: false,
      imagePath: ''
    };
  }

  cancelEventForm(): void {
    this.showEventForm = false;
    this.editingEvent = null;
    this.resetEventForm();
  }

  saveEvent(): void {
    if (!this.location) return;

    const eventData = {
      name: this.eventForm.name,
      address: this.eventForm.address,
      type: this.eventForm.type,
      date: this.eventForm.date,
      price: this.eventForm.price,
      recurrent: this.eventForm.recurrent,
      imagePath: this.eventForm.imagePath,
      locationName: this.location.location.name
    };

    if (this.editingEvent) {
      // Update existing event
      this.eventService.updateEvent(this.editingEvent.name, eventData).subscribe({
        next: () => {
          this.loadEvents(this.location.location.name);
          this.cancelEventForm();
        },
        error: (err) => {
          console.error('Error updating event:', err);
          alert('Failed to update event: ' + (err.error || 'Unknown error'));
        }
      });
    } else {
      // Create new event
      this.eventService.createEvent(eventData).subscribe({
        next: () => {
          this.loadEvents(this.location.location.name);
          this.cancelEventForm();
        },
        error: (err) => {
          console.error('Error creating event:', err);
          alert('Failed to create event: ' + (err.error || 'Unknown error'));
        }
      });
    }
  }

  deleteEvent(eventName: string): void {
    if (confirm('Are you sure you want to delete this event?')) {
      this.eventService.deleteEvent(eventName).subscribe({
        next: () => {
          this.loadEvents(this.location.location.name);
        },
        error: (err) => {
          console.error('Error deleting event:', err);
          alert('Failed to delete event');
        }
      });
    }
  }

  onImageUpload(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.locationService.uploadImages([file]).subscribe({
        next: (paths) => {
          if (paths && paths.length > 0) {
            this.eventForm.imagePath = paths[0];
          }
        },
        error: (err) => console.error('Error uploading image:', err)
      });
    }
  }
}
