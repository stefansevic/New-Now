import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home';
import { LocationsComponent } from './components/locations/locations';
import { LocationDetailsComponent } from './components/location-details/location-details';
import { LocationFormComponent } from './components/location-form/location-form';
import { LocationManagersComponent } from './components/location-managers/location-managers';
import { MyLocationsComponent } from './components/my-locations/my-locations';
import { LoginComponent } from './components/login/login';
import { RegisterComponent } from './components/register/register';
import { AccountRequestsComponent } from './components/account-requests/account-requests';
import { AdminHomeComponent } from './components/admin-home/admin-home';

export const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'admin/requests', component: AccountRequestsComponent },
  { path: 'admin/home', component: AdminHomeComponent },
  { path: 'requests', component: AccountRequestsComponent },
  { path: 'locations', component: LocationsComponent },
  { path: 'locations/new', component: LocationFormComponent },
  { path: 'locations/edit/:name', component: LocationFormComponent },
  { path: 'locations/:name/managers', component: LocationManagersComponent },
  { path: 'locations/:name', component: LocationDetailsComponent },
  { path: 'my-locations', component: MyLocationsComponent },
];
