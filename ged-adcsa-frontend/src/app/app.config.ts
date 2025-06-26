/**
 * Fichier de configuration globale de l'application (app.config.ts)
 *
 * Ce fichier centralise la configuration de l'application Angular :
 * - Déclare les providers globaux (animations, HTTP, routing)
 * - Configure les intercepteurs HTTP (ex : AuthInterceptor)
 * - Importe les routes principales
 *
 * Utilisé au démarrage de l'application (main.ts) pour initialiser tous les services nécessaires.
 */

import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideHttpClient } from '@angular/common/http';
import { AuthInterceptor } from './interceptors/auth.interceptor';
import { HTTP_INTERCEPTORS } from '@angular/common/http';

import { routes } from './app.routes';

/**
 * Configuration de l'application
 * 
 * Cette configuration est utilisée pour initialiser l'application
 * avec tous les providers et intercepteurs nécessaires.
 */
export const appConfig: ApplicationConfig = {
  providers: [
    // Active les animations Angular Material
    provideAnimations(),
    
    // Configure le routage avec les routes définies
    provideRouter(routes),
    
    // Configure le client HTTP
    provideHttpClient(),
    
    // Ajout de l'intercepteur HTTP avec l'ancienne syntaxe compatible
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ]
}; 