/**
 * Composant principal de l'application
 * 
 * Ce composant est le point d'entrée de l'application. Il :
 * - Gère la structure globale de l'application
 * - Affiche la barre de navigation
 * - Gère l'affichage conditionnel des éléments
 * - S'occupe de la déconnexion
 * 
 * La barre de navigation est affichée uniquement :
 * - Quand l'utilisateur est connecté
 * - Sur les pages qui ne sont pas la page de connexion
 */

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from './services/auth.service';
import { FooterComponent } from './components/footer/footer.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterModule, FooterComponent],
  template: `
    <div class="app-container">
      <!-- Barre de navigation -->
      <nav *ngIf="showNavbar">
        <div class="nav-content">
          <!-- Logo et titre -->
          <div class="nav-brand">
            <img src="assets/logo.png" alt="Logo" class="nav-logo">
            <span class="nav-title">GED</span>
          </div>

          <!-- Liens de navigation -->
          <div class="nav-links">
            <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}">
              Accueil
            </a>
            <a routerLink="/dashboard" routerLinkActive="active">
              Tableau de bord
            </a>
            <a routerLink="/profile" routerLinkActive="active">
              Profil
            </a>
            <a routerLink="/settings" routerLinkActive="active">
              Paramètres
            </a>
          </div>

          <!-- Bouton de déconnexion -->
          <button class="logout-btn" (click)="logout()">
            Se déconnecter
          </button>
        </div>
      </nav>

      <!-- Contenu principal -->
      <main>
        <router-outlet></router-outlet>
      </main>

      <!-- Pied de page -->
      <app-footer></app-footer>
    </div>
  `,
  styles: [`
    .app-container {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }

    /* Styles de la barre de navigation */
    nav {
      background-color: #ffffff;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      padding: 1rem;
    }

    .nav-content {
      max-width: 1200px;
      margin: 0 auto;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .nav-brand {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .nav-logo {
      height: 40px;
    }

    .nav-title {
      font-size: 1.5rem;
      font-weight: bold;
      color: #333;
    }

    .nav-links {
      display: flex;
      gap: 2rem;
    }

    .nav-links a {
      color: #666;
      text-decoration: none;
      padding: 0.5rem;
      border-radius: 4px;
      transition: all 0.3s ease;
    }

    .nav-links a:hover {
      color: #333;
      background-color: #f5f5f5;
    }

    .nav-links a.active {
      color: #1976d2;
      font-weight: 500;
    }

    .logout-btn {
      background-color: #f44336;
      color: white;
      border: none;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      cursor: pointer;
      transition: background-color 0.3s ease;
    }

    .logout-btn:hover {
      background-color: #d32f2f;
    }

    /* Styles du contenu principal */
    main {
      flex: 1;
      max-width: 1200px;
      margin: 2rem auto;
      padding: 0 1rem;
    }
  `]
})
export class AppComponent {
  // Indique si la barre de navigation doit être affichée
  showNavbar = false;

  constructor(private authService: AuthService) {
    // On s'abonne aux changements d'état d'authentification
    this.authService.isAuthenticated$.subscribe(isAuthenticated => {
      this.showNavbar = isAuthenticated;
    });
  }

  /**
   * Déconnecte l'utilisateur
   * Cette méthode est appelée quand l'utilisateur clique sur le bouton de déconnexion
   */
  logout(): void {
    this.authService.logout();
  }
}