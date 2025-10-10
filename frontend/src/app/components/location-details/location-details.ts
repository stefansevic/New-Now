import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { LocationService } from '../../services/location.service';
import { EventService } from '../../services/event.service';
import { ReviewService } from '../../services/review.service';
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
  reviews: any[] = [];
  eligibleEvents: any[] = [];
  isAdmin: boolean = false;
  isManager: boolean = false;
  isLoggedIn: boolean = false;
  showEventForm: boolean = false;
  showReviewForm: boolean = false;
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

  reviewForm = {
    eventName: '',
    commentText: '',
    performance: null as number | null,
    soundAndLightning: null as number | null,
    venue: null as number | null,
    overallImpression: null as number | null
  };

  private adminSubscription!: Subscription;
  private loginSubscription!: Subscription;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private locationService: LocationService,
    private eventService: EventService,
    private reviewService: ReviewService,
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
      this.loadReviews(name);
      this.checkIfManager(name);
      if (this.isLoggedIn) {
        this.loadEligibleEvents(name);
      }
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

  loadReviews(locationName: string): void {
    this.reviewService.getReviewsByLocation(locationName).subscribe({
      next: (data) => this.reviews = data,
      error: (err) => console.error(err)
    });
  }

  loadEligibleEvents(locationName: string): void {
    this.reviewService.getEligibleEvents(locationName).subscribe({
      next: (data) => this.eligibleEvents = data,
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

  // Review management methods
  openReviewForm(): void {
    if (!this.isLoggedIn) {
      alert('Please log in to leave a review');
      return;
    }
    if (!this.location) return;
    
    this.loadEligibleEvents(this.location.location.name);
    this.showReviewForm = true;
    this.resetReviewForm();
  }

  resetReviewForm(): void {
    this.reviewForm = {
      eventName: '',
      commentText: '',
      performance: null,
      soundAndLightning: null,
      venue: null,
      overallImpression: null
    };
  }

  cancelReviewForm(): void {
    this.showReviewForm = false;
    this.resetReviewForm();
  }

  saveReview(): void {
    if (!this.location || !this.reviewForm.eventName) {
      alert('Please select an event');
      return;
    }

    // Check if at least one rating is provided
    const hasRating = this.reviewForm.performance !== null || 
                      this.reviewForm.soundAndLightning !== null ||
                      this.reviewForm.venue !== null || 
                      this.reviewForm.overallImpression !== null;

    if (!hasRating && !this.reviewForm.commentText) {
      alert('Please provide at least one rating or a comment');
      return;
    }

    const reviewData = {
      locationName: this.location.location.name,
      eventName: this.reviewForm.eventName,
      commentText: this.reviewForm.commentText,
      performance: this.reviewForm.performance,
      soundAndLightning: this.reviewForm.soundAndLightning,
      venue: this.reviewForm.venue,
      overallImpression: this.reviewForm.overallImpression
    };

    this.reviewService.createReview(reviewData).subscribe({
      next: () => {
        this.loadReviews(this.location.location.name);
        this.cancelReviewForm();
        alert('Review submitted successfully!');
      },
      error: (err) => {
        console.error('Error creating review:', err);
        alert('Failed to submit review: ' + (err.error || 'Unknown error'));
      }
    });
  }

  getAverageRating(review: any): number {
    if (!review.rate) return 0;
    
    const ratings = [
      review.rate.performance,
      review.rate.soundAndLightning,
      review.rate.venue,
      review.rate.overallImpression
    ].filter(r => r !== null && r !== undefined);

    if (ratings.length === 0) return 0;
    const sum = ratings.reduce((a, b) => a + b, 0);
    return sum / ratings.length;
  }
}
