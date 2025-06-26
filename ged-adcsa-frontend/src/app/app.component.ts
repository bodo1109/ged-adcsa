/**
 * Composant principal de l'application
 * 
 * Ce composant est le point d'entrée de l'application. Il :
 * - Gère la structure globale de l'application
 * - Affiche la barre de navigation uniquement sur le dashboard
 * - Gère l'affichage conditionnel des éléments
 * - S'occupe de la déconnexion
 * 
 * La barre de navigation est affichée uniquement :
 * - Sur la page dashboard
 */

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { AuthService } from './services/auth.service';
import { FooterComponent } from './components/footer/footer.component';
import { filter, take } from 'rxjs/operators';
import { Location } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    FooterComponent
  ],
  template: `
    <div class="app-container">
      <mat-toolbar color="primary" *ngIf="(isAuthenticated$ | async) && showNavbar">
        <img src="assets/images/img.png" alt="ADCSA Logo" class="logo">
        <span class="spacer"></span>
        <button mat-icon-button [matMenuTriggerFor]="menu" aria-label="Menu">
          <mat-icon>account_circle</mat-icon>
        </button>
        <mat-menu #menu="matMenu">
          <button mat-menu-item (click)="onLogout()">
            <mat-icon>exit_to_app</mat-icon>
            <span>Déconnexion</span>
          </button>
        </mat-menu>
      </mat-toolbar>
      
      <main class="main-content">
        <router-outlet></router-outlet>
      </main>
      
      <app-footer *ngIf="shouldShowFooter()"></app-footer>
    </div>
  `,
  styles: [`
    .app-container {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }
    
    .main-content {
      flex: 1;
    }
    
    .logo {
      height: 40px;
      margin-right: 16px;
    }
    .spacer {
      flex: 1 1 auto;
    }
  `]
})
export class AppComponent implements OnInit {
  isAuthenticated$ = this.authService.isAuthenticated();
  isConnecting$ = this.authService.isConnecting();
  showNavbar = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private location: Location
  ) {
    console.log('AppComponent initialisé');
  }

  ngOnInit() {
    console.log('AppComponent ngOnInit');
    
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.isAuthenticated$.pipe(take(1)).subscribe(isAuthenticated => {
          const publicRoutes = ['/login', '/reset-form', '/reset-request'];
          const currentPath = this.location.path().split('?')[0];
          console.log('ROUTE DEBUG:', this.router.url, 'currentPath:', currentPath, 'isAuthenticated:', isAuthenticated);
          if (
            !isAuthenticated &&
            !publicRoutes.includes(currentPath)
          ) {
            console.log('Redirection vers la page de connexion...');
            this.router.navigate(['/login']);
            this.showNavbar = false;
          }
        });
      }
    });

    // Surveiller l'état de connexion pour afficher la navbar avec un délai
    this.isConnecting$.subscribe(isConnecting => {
      if (!isConnecting) {
        // Attendre un peu avant d'afficher la navbar pour laisser le dashboard se charger
        setTimeout(() => {
          this.showNavbar = true;
        }, 500);
      } else {
        this.showNavbar = false;
      }
    });
  }

  shouldShowFooter(): boolean {
    // Afficher le footer sur toutes les pages sauf la page de connexion
    return this.router.url !== '/login';
  }

  onLogout(): void {
    console.log('Clic sur le bouton de déconnexion');
    try {
      this.authService.logout();
      console.log('Déconnexion effectuée avec succès');
    } catch (error) {
      console.error('Erreur lors de la déconnexion:', error);
    }
  }
}