import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { RegisterComponent } from './components/register/register';
import { HomeComponent } from './components/home/home';
import { LocationsComponent } from './components/locations/locations';
import { LocationDetailsComponent } from './components/location-details/location-details';
import { AccountRequestsComponent } from './components/account-requests/account-requests';

export const routes: Routes = [
    { path: '', redirectTo: '/home', pathMatch: 'full' },
    { path: 'home', component: HomeComponent },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'locations', component: LocationsComponent },
    { path: 'locations/:name', component: LocationDetailsComponent },
    { path: 'admin/requests', component: AccountRequestsComponent },
];
