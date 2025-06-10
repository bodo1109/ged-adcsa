/**
 * Service d'authentification
 * 
 * Ce service gère tout ce qui concerne l'authentification des utilisateurs :
 * - Connexion des utilisateurs
 * - Déconnexion
 * - Vérification de l'état de connexion
 * - Stockage du token d'authentification
 * - Récupération des informations de l'utilisateur connecté
 * 
 * Il utilise le localStorage pour persister les informations de connexion
 * entre les rechargements de page.
 */

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';

// Définition de ce à quoi ressemble un utilisateur dans notre application
// On utilise cette interface partout où on manipule des données utilisateur
export interface User {
  id: number;          // Identifiant unique de l'utilisateur
  username: string;    // Son nom d'utilisateur pour se connecter
  email: string;       // Son email
  roles: string[];     // Ses droits d'accès (ex: ADMIN, USER, etc.)
}

// Interface définissant la structure des identifiants de connexion
export interface LoginCredentials {
  username: string;
  password: string;
}

// Quand le serveur répond à une tentative de connexion, il nous renvoie :
// - Un token (notre clé d'accès)
// - Les infos de l'utilisateur
export interface AuthResponse {
  token: string;   // Le token JWT qui servira de clé d'accès
  user: User;      // Les infos de l'utilisateur connecté
}

@Injectable({
  providedIn: 'root'  // Ce service est disponible partout dans l'application
})
export class AuthService {
  // Les clés qu'on utilise pour stocker les infos dans le navigateur
  // On les met en readonly pour éviter de les modifier par erreur
  private readonly TOKEN_KEY = 'auth_token';  // Où on stocke le token
  private readonly USER_KEY = 'user_data';    // Où on stocke les infos utilisateur
  
  // URL de l'API d'authentification
  private apiUrl = `${environment.apiUrl}/auth`;
  
  // Un système qui permet à tous les composants de savoir qui est connecté
  // C'est comme un haut-parleur qui annonce les changements d'état
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  // Les composants peuvent s'abonner à ce haut-parleur
  public currentUser$ = this.currentUserSubject.asObservable();

  // Sujet qui émet l'état de connexion de l'utilisateur
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  
  // Observable public de l'état de connexion
  isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(
    private http: HttpClient,  // Pour faire des requêtes au serveur
    private router: Router     // Pour naviguer entre les pages
  ) {
    // Au démarrage de l'application, on vérifie si quelqu'un était déjà connecté
    // C'est utile si l'utilisateur rafraîchit la page
    const user = this.getUser();
    if (user) {
      this.currentUserSubject.next(user);  // On annonce qui est connecté
    }
  }

  /**
   * Vérifie si un token est présent dans le localStorage
   * @returns true si un token existe, false sinon
   */
  private hasToken(): boolean {
    return !!localStorage.getItem('token');
  }

  /**
   * Récupère le token d'authentification
   * @returns Le token stocké ou null s'il n'existe pas
   */
  getToken(): string | null {
    return localStorage.getItem('token');
  }

  /**
   * Vérifie si l'utilisateur est actuellement connecté
   * @returns true si l'utilisateur est connecté, false sinon
   */
  isAuthenticated(): boolean {
    return this.hasToken();
  }

  /**
   * Connecte un utilisateur
   * @param credentials Les identifiants de connexion
   * @returns Un Observable qui émet la réponse du serveur
   */
  login(credentials: LoginCredentials): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      // Quand on reçoit la réponse, on stocke le token
      tap(response => {
        localStorage.setItem('token', response.token);
        this.isAuthenticatedSubject.next(true);
        this.setUser(response.user);
        this.currentUserSubject.next(response.user);
      })
    );
  }

  /**
   * Déconnecte l'utilisateur
   * Supprime le token et met à jour l'état de connexion
   */
  logout(): void {
    localStorage.removeItem('token');
    this.isAuthenticatedSubject.next(false);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  /**
   * Récupère les informations de l'utilisateur connecté
   * @returns Un Observable qui émet les informations de l'utilisateur
   */
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Récupère les infos de l'utilisateur connecté
   * 
   * On va chercher les infos dans le stockage local
   * et on les convertit en objet User
   */
  getUser(): User | null {
    const userStr = localStorage.getItem(this.USER_KEY);
    return userStr ? JSON.parse(userStr) : null;
  }

  /**
   * Stocke les infos de l'utilisateur
   * 
   * On convertit l'objet User en chaîne de caractères
   * pour pouvoir le stocker dans le navigateur
   */
  private setUser(user: User): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  /**
   * Vérifie si l'utilisateur a un rôle spécifique
   * 
   * Par exemple, on peut vérifier si quelqu'un est admin
   * en appelant hasRole('ADMIN')
   * 
   * @param role - Le rôle à vérifier (ex: 'ADMIN', 'USER')
   */
  hasRole(role: string): boolean {
    const user = this.getUser();
    return user?.roles?.includes(role) || false;
  }

  /**
   * Vérifie si l'utilisateur a au moins un des rôles demandés
   * 
   * Utile quand une page peut être accessible par plusieurs types d'utilisateurs
   * Par exemple, une page accessible aux admins ET aux modérateurs
   * 
   * @param roles - La liste des rôles acceptés
   */
  hasAnyRole(roles: string[]): boolean {
    return roles.some(role => this.hasRole(role));
  }
} 