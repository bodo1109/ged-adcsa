/**
 * Intercepteur d'authentification
 * 
 * Cet intercepteur ajoute automatiquement le token d'authentification
 * à toutes les requêtes HTTP sortantes. Il s'occupe de :
 * - Ajouter le token dans l'en-tête Authorization
 * - Gérer les erreurs d'authentification (401)
 * - Rediriger vers la page de connexion si nécessaire
 * 
 * Comment ça marche :
 * 1. À chaque requête HTTP, l'intercepteur est appelé
 * 2. Il récupère le token d'authentification
 * 3. Il l'ajoute à l'en-tête de la requête
 * 4. Si la requête échoue avec une erreur 401, il déconnecte l'utilisateur
 */

import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(
    private authService: AuthService,  // Service d'authentification
    private router: Router            // Service de navigation
  ) {}

  /**
   * Intercepte les requêtes HTTP pour ajouter le token
   * @param request La requête HTTP à intercepter
   * @param next Le prochain intercepteur dans la chaîne
   * @returns Un Observable qui émet la réponse HTTP
   */
  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Récupérer le token
    const token = this.authService.getToken();
    
    // Si le token existe, l'ajouter aux headers
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          // Token expiré ou invalide
          this.authService.logout();
          this.router.navigate(['/login']);
        } else if (error.status === 403) {
          // Accès non autorisé
          console.error('Accès non autorisé:', error);
          this.router.navigate(['/unauthorized']);
        }
        return throwError(() => error);
      })
    );
  }
} 