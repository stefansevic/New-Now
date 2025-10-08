import { Component, OnInit } from '@angular/core';
import { LocationService } from '../../services/location.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-locations',
  templateUrl: './locations.html',
  styleUrls: ['./locations.css'],
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class LocationsComponent implements OnInit {
  locations: any[] = [];

  constructor(private locationService: LocationService) { }

  ngOnInit(): void {
    this.locationService.getLocations().subscribe({
      next: (data) => this.locations = data,
      error: (err) => console.error(err)
    });
  }
}
