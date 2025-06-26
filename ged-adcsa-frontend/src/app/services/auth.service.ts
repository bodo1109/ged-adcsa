/**
 * Service d'authentification (AuthService)
 *
 * Ce service centralise toute la logique liée à l'authentification :
 * - Connexion et déconnexion des utilisateurs
 * - Gestion et stockage du token JWT
 * - Vérification du statut d'authentification
 * - Gestion des rôles et des informations utilisateur
 * - Gestion de la première connexion et du changement de mot de passe
 * - Gestion de la récupération et réinitialisation du mot de passe
 *
 * Interfaces fournies :
 * - LoginCredentials : Identifiants de connexion
 * - User : Informations utilisateur
 * - Role : Rôle utilisateur
 * - AuthResponse : Réponse du serveur après authentification
 *
 * Propriétés principales :
 * - isAuthenticated$ : Observable de l'état d'authentification
 * - currentUser$ : Observable de l'utilisateur courant
 * - isConnecting$ : Observable de l'état de connexion (pour l'UI)
 *
 * Méthodes principales :
 * - login(credentials) : Authentifie l'utilisateur
 * - logout() : Déconnecte l'utilisateur
 * - getToken() : Récupère le token JWT
 * - hasRole(role) : Vérifie le rôle utilisateur
 * - firstPasswordChange(data) : Change le mot de passe à la première connexion
 * - requestPasswordReset(email) : Demande de réinitialisation
 * - resetPasswordWithToken(token, newPassword, confirmPassword) : Réinitialise le mot de passe
 *
 * Ce service est injecté partout où l'authentification est requise.
 */

import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

// Interface pour les identifiants de connexion
export interface LoginCredentials {
  username: string;   // Nom d'utilisateur ou email
  password: string; // Le mot de passe de l'utilisateur
}

// Interface pour les informations de l'utilisateur
export interface User {
  id: number;
  email: string;
  nom: string;
  prenom: string;
  username: string;
  statut: string;
  derniereConnexion: string | null;
  dateCreation: string;
  dateModification: string | null;
  dateExpirationToken: string | null;
  dateVerrouillage: string | null;
  tentativesEchec: number | null;
  compteVerrouille: boolean | null;
  tokenResetPassword: string | null;
  passwordChangedAt: string | null;
  firstLoginExpiresAt: string | null;
  roles: Role[];
}

// Interface pour les rôles
export interface Role {
  id: number;
  nom: string;
  description: string | null;
}

// Interface pour la réponse d'authentification
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  utilisateur: User;
  isFirstLogin?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  // URL de l'API d'authentification
  private apiUrl = environment.apiUrl;
  
  // Observable pour suivre l'état de l'authentification
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  // Observable pour suivre l'utilisateur courant
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  // Observable pour gérer l'état de l'animation de connexion
  private isConnectingSubject = new BehaviorSubject<boolean>(false);
  public isConnecting$ = this.isConnectingSubject.asObservable();

  private readonly TOKEN_KEY = 'auth_token';

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    // Vérifier si un token existe au démarrage
    const token = this.getToken();
    console.log('Token au démarrage:', token);
    
    // Vérifier si le token est valide
    if (token) {
      try {
        // Vérifier si le token est expiré
        const tokenData = JSON.parse(atob(token.split('.')[1]));
        const expirationDate = new Date(tokenData.exp * 1000);
        
        if (expirationDate > new Date()) {
          console.log('Token valide trouvé');
          this.isAuthenticatedSubject.next(true);
        } else {
          console.log('Token expiré');
          this.clearAuthData();
          this.isAuthenticatedSubject.next(false);
        }
      } catch (error) {
        console.error('Erreur lors de la vérification du token:', error);
        this.clearAuthData();
        this.isAuthenticatedSubject.next(false);
      }
    } else {
      console.log('Aucun token trouvé');
      this.isAuthenticatedSubject.next(false);
    }
  }

  private clearAuthData(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem('refreshToken');
    this.currentUserSubject.next(null);
  }

  /**
   * Vérifie si l'utilisateur est authentifié
   * @returns true si l'utilisateur est authentifié, false sinon
   */
  isAuthenticated(): Observable<boolean> {
    return this.isAuthenticatedSubject.asObservable();
  }

  /**
   * Vérifie le statut d'authentification au démarrage
   */
  private checkAuthStatus(): void {
    const token = localStorage.getItem('token');
    if (token) {
      this.isAuthenticatedSubject.next(true);
    }
  }

  /**
   * Connecte l'utilisateur
   * @param credentials Les identifiants de connexion
   * @returns Un Observable contenant la réponse du serveur
   */
  login(credentials: LoginCredentials): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, credentials)
      .pipe(
        tap((response: AuthResponse) => {
          if (response && response.accessToken) {
            this.setToken(response.accessToken);
            // Stocker aussi le refreshToken
            if (response.refreshToken) {
              localStorage.setItem('refreshToken', response.refreshToken);
            }
            this.isAuthenticatedSubject.next(true);
          }
        }),
        catchError(this.handleError)
      );
  }

  /**
   * Déconnecte l'utilisateur
   * Supprime le token et les données utilisateur du localStorage
   * Redirige vers la page de connexion
   */
  logout(): void {
    console.log('Début de la déconnexion...');
    
    try {
      // Supprimer le token et les données utilisateur
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem('refreshToken');
      console.log('Tokens supprimés du localStorage');
      
      // Mettre à jour les observables
      this.isAuthenticatedSubject.next(false);
      this.currentUserSubject.next(null);
      console.log('Observables mis à jour');
      
      // Rediriger vers la page de connexion
      console.log('Redirection vers la page de connexion...');
      this.router.navigate(['/login']);
      console.log('Déconnexion terminée avec succès');
    } catch (error) {
      console.error('Erreur lors de la déconnexion:', error);
      // En cas d'erreur, forcer la redirection
      this.router.navigate(['/login']);
    }
  }

  /**
   * Récupère le token d'authentification
   * @returns Le token JWT ou null s'il n'existe pas
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Récupère l'utilisateur courant
   * @returns L'utilisateur courant ou null s'il n'est pas connecté
   */
  getUser(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Vérifie si l'utilisateur a un rôle spécifique
   * @param role Le rôle à vérifier
   * @returns true si l'utilisateur a le rôle, false sinon
   */
  hasRole(role: string): boolean {
    const user = this.getUser();
    return user?.roles.some(r => r.nom === role) || false;
  }

  /**
   * Change le mot de passe lors de la première connexion avec token JWT
   * @param data Les données du formulaire (currentPassword, newPassword, confirmPassword)
   * @returns Un Observable contenant la réponse du serveur
   */
  firstPasswordChange(data: { currentPassword: string; newPassword: string; confirmPassword: string }) {
    // Récupérer le token du localStorage
    const token = this.getToken();
    
    if (!token) {
      return throwError(() => new Error('Token d\'authentification non trouvé'));
    }
    
    // Créer les headers avec le token
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    console.log('Envoi de la requête firstPasswordChange avec le corps:', JSON.stringify(data, null, 2));
    console.log('Headers:', headers);

    // Faire la requête vers l'endpoint
    return this.http.post<AuthResponse>(
      `${this.apiUrl}/auth/first-password-change`,
      data,
      { headers }
    ).pipe(
      tap({
        next: (response) => {
          console.log('Réponse du serveur firstPasswordChange:', JSON.stringify(response, null, 2));
          if (response.accessToken && response.utilisateur.passwordChangedAt) {
            // On met à jour le token stocké
            this.setToken(response.accessToken);
            // On met à jour le statut d'authentification
            this.isAuthenticatedSubject.next(true);
            // On met à jour l'utilisateur courant
            this.currentUserSubject.next(response.utilisateur);
          }
        },
        error: (error) => {
          console.error('Erreur firstPasswordChange détaillée:', {
            status: error.status,
            statusText: error.statusText,
            error: error.error,
            message: error.message,
            data: error.error?.data,
            headers: error.headers,
            url: error.url,
            response: error.error?.response,
            validationErrors: error.error?.validationErrors,
            details: error.error?.details,
            errorText: error.error?.error,
            timestamp: error.error?.timestamp,
            path: error.error?.path,
            fullError: JSON.stringify(error, null, 2)
          });
          
          if (error.error?.data) {
            return throwError(() => ({
              status: error.status,
              message: error.error.message || 'Erreur de validation',
              data: error.error.data,
              fullError: error.error
            }));
          }
          
          return throwError(() => ({
            status: error.status,
            message: error.error?.message || 'Une erreur est survenue lors du changement de mot de passe',
            data: error.error?.data,
            fullError: error.error
          }));
        }
      })
    );
  }

  /**
   * Change le mot de passe lors de la première connexion
   * @param password Le nouveau mot de passe
   * @returns Un Observable contenant la réponse du serveur
   */
  changeFirstPassword(password: string): Observable<AuthResponse> {
    // On envoie tous les champs requis par le backend
    const requestBody = {
      currentPassword: password, // Pour la première connexion, on utilise le même mot de passe
      newPassword: password,
      confirmPassword: password
    };
    
    console.log('Envoi de la requête avec le corps:', JSON.stringify(requestBody, null, 2));
    
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/first-password-change`, requestBody).pipe(
      tap({
        next: (response) => {
          console.log('Réponse du serveur:', JSON.stringify(response, null, 2));
          if (response.accessToken && response.utilisateur.passwordChangedAt) {
            // On met à jour le token stocké
            this.setToken(response.accessToken);
            // On met à jour le statut d'authentification
            this.isAuthenticatedSubject.next(true);
            // On met à jour l'utilisateur courant
            this.currentUserSubject.next(response.utilisateur);
          }
        },
        error: (error) => {
          console.error('Erreur détaillée:', {
            status: error.status,
            statusText: error.statusText,
            error: error.error,
            message: error.message,
            data: error.error?.data,
            headers: error.headers,
            url: error.url,
            response: error.error?.response,
            validationErrors: error.error?.validationErrors,
            details: error.error?.details,
            errorText: error.error?.error,
            timestamp: error.error?.timestamp,
            path: error.error?.path,
            fullError: JSON.stringify(error, null, 2)
          });
          
          if (error.error?.data) {
            return throwError(() => ({
              status: error.status,
              message: error.error.message || 'Erreur de validation',
              data: error.error.data,
              fullError: error.error
            }));
          }
          
          return throwError(() => ({
            status: error.status,
            message: error.error?.message || 'Une erreur est survenue lors du changement de mot de passe',
            data: error.error?.data,
            fullError: error.error
          }));
        }
      })
    );
  }

  changePassword(oldPassword: string, newPassword: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/change-password`, {
      oldPassword,
      newPassword
    }).pipe(
      catchError(this.handleError)
    );
  }

  private setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Une erreur est survenue';
    
    if (error.error instanceof ErrorEvent) {
      // Erreur côté client
      errorMessage = `Erreur: ${error.error.message}`;
    } else {
      // Erreur côté serveur
      if (error.status === 400) {
        errorMessage = error.error.message || 'Données invalides';
      } else if (error.status === 401) {
        errorMessage = 'Identifiants invalides';
      } else if (error.status === 403) {
        errorMessage = 'Accès non autorisé';
      } else if (error.status === 404) {
        errorMessage = 'Ressource non trouvée';
      } else if (error.status === 500) {
        errorMessage = 'Erreur serveur';
      }
    }
    
    console.error('Erreur détaillée:', error);
    return throwError(() => new Error(errorMessage));
  }

  /**
   * Démarre l'animation de connexion
   */
  startConnecting(): void {
    this.isConnectingSubject.next(true);
  }

  /**
   * Termine l'animation de connexion
   */
  stopConnecting(): void {
    this.isConnectingSubject.next(false);
  }

  /**
   * Vérifie si une animation de connexion est en cours
   */
  isConnecting(): Observable<boolean> {
    return this.isConnectingSubject.asObservable();
  }

  /**
   * Demande la réinitialisation du mot de passe (reset-request)
   * @param email L'email de l'utilisateur
   */
  requestPasswordReset(email: string) {
    return this.http.post<any>(`${this.apiUrl}/auth/reset-request`, { email });
  }

  /**
   * Réinitialise le mot de passe avec un token (reset-form)
   */
  resetPasswordWithToken(token: string, newPassword: string, confirmPassword: string) {
    return this.http.post<any>(`${this.apiUrl}/auth/reset-password`, {
      token,
      newPassword,
      confirmPassword
    });
  }
} 