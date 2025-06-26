/**
 * Configuration des routes de l'application
 * 
 * Ce fichier définit toutes les routes de l'application et leurs comportements :
 * - Les routes publiques (accessibles sans connexion)
 * - Les routes protégées (nécessitent une authentification)
 * - Les redirections par défaut
 * - Le chargement paresseux des modules
 * 
 * Structure des routes :
 * - /login : Page de connexion (publique)
 * - / : Page d'accueil (protégée)
 * - /dashboard : Tableau de bord (protégée)
 * - /profile : Profil utilisateur (protégée)
 * - /settings : Paramètres (protégée)
 * - /** : Route par défaut (redirige vers /)
 */

import { Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  // Redirection par défaut vers la page de connexion
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  
  // Route de la page de connexion (publique)
  {
    path: 'login',
    loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent)
  },
  
  // route protégée (à décommenter si le composant existe)
  {
    path: 'dashboard',
    loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [AuthGuard]
  },

  {
    path: 'first-password',
    loadComponent: () => import('./components/first-password/first-password.component').then(m => m.FirstPasswordComponent),
    canActivate: [AuthGuard] // Si vous voulez que l'utilisateur soit authentifié pour y accéder
  },
  
  {
    path: 'reset-request',
    loadComponent: () => import('./components/reset-request/reset-request.component').then(m => m.ResetRequestComponent)
    // Pas de AuthGuard car c'est pour récupérer un mot de passe oublié
  },
  
  {
    path: 'reset-form',
    loadComponent: () => import('./components/reset-form/reset-form.component').then(m => m.ResetFormComponent),
  },
  // {
  //   path: 'profile',
  //   loadComponent: () => import('./components/profile/profile.component').then(m => m.ProfileComponent),
  //   canActivate: [AuthGuard]
  // },
  // {
  //   path: 'settings',
  //   loadComponent: () => import('./components/settings/settings.component').then(m => m.SettingsComponent),
  //   canActivate: [AuthGuard]
  // },
  // Route par défaut pour les URLs non reconnues
  { path: '**', redirectTo: '/login' }
];