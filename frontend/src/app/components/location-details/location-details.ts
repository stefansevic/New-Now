import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { LocationService } from '../../services/location.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-location-details',
  templateUrl: './location-details.html',
  styleUrls: ['./location-details.css'],
  standalone: true,
  imports: [CommonModule]
})
export class LocationDetailsComponent implements OnInit {
  location: any;

  constructor(
    private route: ActivatedRoute,
    private locationService: LocationService
  ) { }

  ngOnInit(): void {
    const name = this.route.snapshot.paramMap.get('name');
    if (name) {
      this.locationService.getLocationDetails(name).subscribe({
        next: (data) => this.location = data,
        error: (err) => console.error(err)
      });
    }
  }
}
