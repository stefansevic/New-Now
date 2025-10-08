import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home';
import { LocationsComponent } from './components/locations/locations';
import { LocationDetailsComponent } from './components/location-details/location-details';
import { LoginComponent } from './components/login/login';
import { RegisterComponent } from './components/register/register';
import { AccountRequestsComponent } from './components/account-requests/account-requests';
import { AdminHomeComponent } from './components/admin-home/admin-home';

export const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'locations', component: LocationsComponent },
  { path: 'locations/:name', component: LocationDetailsComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'admin/requests', component: AccountRequestsComponent },
  { path: 'admin/home', component: AdminHomeComponent }
];
