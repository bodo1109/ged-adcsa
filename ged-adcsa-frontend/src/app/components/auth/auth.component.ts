import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { FooterComponent } from '../footer/footer.component';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, RouterOutlet, FooterComponent],
  template: `
    <div class="page-wrapper">
      <main class="main-content">
        <router-outlet></router-outlet>
      </main>
      <footer class="footer-wrapper">
        <app-footer></app-footer>
      </footer>
    </div>
  `,
  styles: [`
    .page-wrapper {
      display: flex;
      flex-direction: column;
      min-height: 100vh;
      background-color: #f5f5f5;
    }

    .main-content {
      flex: 1 0 auto;
      padding: 20px;
      display: flex;
      flex-direction: column;
    }

    .footer-wrapper {
      flex-shrink: 0;
      width: 100%;
    }
  `]
})
export class AuthComponent {} 