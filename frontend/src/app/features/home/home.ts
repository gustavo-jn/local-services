import { afterNextRender, Component, DestroyRef, DOCUMENT, ElementRef, inject, signal, viewChild } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { erroApi } from '../../core/api.service';

@Component({
  imports: [RouterLink],
  selector: 'app-home',
  styleUrl: './home.css',
  templateUrl: './home.html',
  host: {
    class: 'home-page',
    '[class.js-enabled]': 'revealEnabled()',
    '(document:keydown.escape)': 'closeMenu(true)',
    '(document:click)': 'closeMenuOutside($event)',
  },
})
export class Home {
  readonly auth = inject(AuthService);
  readonly saindo = signal(false);
  readonly erroConta = signal('');
  sair(): void {
    if(this.saindo()) return;
    this.saindo.set(true); this.erroConta.set('');
    this.auth.sair().subscribe({
      next:()=>this.saindo.set(false),
      error:erro=>{ this.saindo.set(false); this.erroConta.set(erroApi(erro)); },
    });
  }
  private readonly document = inject(DOCUMENT);
  private readonly element = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly destroyRef = inject(DestroyRef);
  private readonly menuButton = viewChild<ElementRef<HTMLButtonElement>>('menuButton');
  private readonly cleanup: Array<() => void> = [];
  private readonly images = ['manutencao', 'pintura', 'pedreiro', 'instalacoes', 'jardinagem'];
  private window: Window | null = null;
  private photoTimer: number | undefined;
  private stepTimer: number | undefined;
  private photoVisible = false;
  private stepsVisible = false;
  private photoHovered = false;
  private stepHovered = false;
  private animationObserverAvailable = false;
  private reducedMotion = false;

  protected readonly menuOpen = signal(false);
  protected readonly activeImage = signal('manutencao');
  protected readonly activeStep = signal(0);
  protected readonly activeSection = signal('');
  protected readonly headerScrolled = signal(false);
  protected readonly scrollProgress = signal(0);
  protected readonly revealEnabled = signal(false);

  constructor() {
    afterNextRender(() => this.initializeBrowserEffects());
    this.destroyRef.onDestroy(() => {
      this.stopTimers();
      this.cleanup.forEach(dispose => dispose());
    });
  }

  protected toggleMenu(): void {
    this.menuOpen.update(open => !open);
  }

  protected closeMenu(returnFocus = false): void {
    if (!this.menuOpen()) return;
    this.menuOpen.set(false);
    if (returnFocus) this.menuButton()?.nativeElement.focus();
  }

  protected closeMenuOutside(event: MouseEvent): void {
    const target = event.target as Node | null;
    if (target && !this.element.nativeElement.querySelector('.site-header')?.contains(target)) {
      this.closeMenu();
    }
  }

  protected previewService(key: string): void {
    this.photoHovered = true;
    this.activeImage.set(key);
    this.syncTimers();
  }

  protected endServicePreview(): void {
    this.photoHovered = false;
    this.syncTimers();
  }

  protected previewStep(index: number): void {
    this.stepHovered = true;
    this.activeStep.set(index);
    this.syncTimers();
  }

  protected endStepPreview(): void {
    this.stepHovered = false;
    this.syncTimers();
  }

  private initializeBrowserEffects(): void {
    const win = this.document.defaultView;
    if (!win) return;
    this.window = win;
    const host = this.element.nativeElement;

    const updateScroll = () => {
      const top = win.scrollY || this.document.documentElement.scrollTop;
      const height = this.document.documentElement.scrollHeight - win.innerHeight;
      this.headerScrolled.set(top > 12);
      this.scrollProgress.set(height > 0 ? Math.min(100, Math.max(0, top / height * 100)) : 0);
    };
    win.addEventListener('scroll', updateScroll, { passive: true });
    this.cleanup.push(() => win.removeEventListener('scroll', updateScroll));
    updateScroll();

    if (!win.matchMedia) return;
    const mobile = win.matchMedia('(max-width: 820px)');
    const reduced = win.matchMedia('(prefers-reduced-motion: reduce)');
    const changeViewport = () => {
      if (!mobile.matches) this.closeMenu();
      updateScroll();
    };
    const changeMotion = () => {
      this.reducedMotion = reduced.matches;
      if (reduced.matches) {
        this.revealEnabled.set(false);
      }
      this.syncTimers();
    };
    mobile.addEventListener('change', changeViewport);
    reduced.addEventListener('change', changeMotion);
    this.cleanup.push(() => mobile.removeEventListener('change', changeViewport));
    this.cleanup.push(() => reduced.removeEventListener('change', changeMotion));
    changeMotion();

    const visibility = () => this.syncTimers();
    this.document.addEventListener('visibilitychange', visibility);
    this.cleanup.push(() => this.document.removeEventListener('visibilitychange', visibility));

    if (typeof IntersectionObserver === 'undefined') return;
    this.animationObserverAvailable = true;

    const revealElements = host.querySelectorAll<HTMLElement>('[data-reveal]');
    if (!reduced.matches) {
      this.revealEnabled.set(true);
      revealElements.forEach(element => {
        const delay = Number(element.dataset['revealDelay'] || 0);
        element.style.setProperty('--reveal-delay', `${Math.min(500, Math.max(0, delay))}ms`);
      });
      const revealObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-revealed');
            observer.unobserve(entry.target);
          }
        });
      }, { rootMargin: '0px 0px -8% 0px', threshold: 0.12 });
      revealElements.forEach(element => revealObserver.observe(element));
      this.cleanup.push(() => revealObserver.disconnect());
    }

    const cycleObserver = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.target.id === 'services-photo-stack') this.photoVisible = entry.isIntersecting;
        if (entry.target.id === 'steps-timeline') this.stepsVisible = entry.isIntersecting;
      });
      this.syncTimers();
    }, { threshold: 0.2 });
    ['services-photo-stack', 'steps-timeline'].forEach(id => {
      const target = host.querySelector(`#${id}`);
      if (target) cycleObserver.observe(target);
    });
    this.cleanup.push(() => cycleObserver.disconnect());

    const navObserver = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) this.activeSection.set(entry.target.id);
      });
    }, { rootMargin: '-25% 0px -60% 0px', threshold: 0 });
    ['servicos', 'para-quem', 'como-comecar'].forEach(id => {
      const section = host.querySelector(`#${id}`);
      if (section) navObserver.observe(section);
    });
    this.cleanup.push(() => navObserver.disconnect());
  }

  private syncTimers(): void {
    this.stopTimers();
    if (!this.window || !this.animationObserverAvailable || this.reducedMotion || this.document.hidden) return;
    if (this.photoVisible && !this.photoHovered) {
      this.photoTimer = this.window.setInterval(() => {
        const next = (this.images.indexOf(this.activeImage()) + 1) % this.images.length;
        this.activeImage.set(this.images[next]);
      }, 4200);
    }
    if (this.stepsVisible && !this.stepHovered) {
      this.stepTimer = this.window.setInterval(() => this.activeStep.update(step => (step + 1) % 3), 2800);
    }
  }

  private stopTimers(): void {
    if (this.window) {
      this.window.clearInterval(this.photoTimer);
      this.window.clearInterval(this.stepTimer);
    }
    this.photoTimer = undefined;
    this.stepTimer = undefined;
  }
}
