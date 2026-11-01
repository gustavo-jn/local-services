import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Home } from './home';
import { vi } from 'vitest';

class ObserverStub {
  static instances: ObserverStub[] = [];
  readonly observe = vi.fn();
  readonly unobserve = vi.fn();
  readonly disconnect = vi.fn();

  constructor(private readonly callback: IntersectionObserverCallback) {
    ObserverStub.instances.push(this);
  }

  intersect(target: Element, visible = true): void {
    this.callback([{ target, isIntersecting: visible } as IntersectionObserverEntry], this as unknown as IntersectionObserver);
  }
}

function mediaQuery(matches: boolean): MediaQueryList {
  return Object.assign(new EventTarget(), { matches }) as MediaQueryList;
}

describe('Home', () => {
  let component: Home;
  let fixture: ComponentFixture<Home>;
  let mobile: MediaQueryList;
  let reduced: MediaQueryList;
  let originalHidden: PropertyDescriptor | undefined;

  beforeEach(async () => {
    ObserverStub.instances = [];
    mobile = mediaQuery(true);
    reduced = mediaQuery(false);
    vi.stubGlobal('matchMedia', vi.fn((query: string) => query.includes('prefers-reduced-motion') ? reduced : mobile));
    vi.stubGlobal('IntersectionObserver', ObserverStub);
    originalHidden = Object.getOwnPropertyDescriptor(document, 'hidden');
    Object.defineProperty(document, 'hidden', { configurable: true, value: false });
    await TestBed.configureTestingModule({
      imports: [Home],
    }).compileComponents();

    fixture = TestBed.createComponent(Home);
    component = fixture.componentInstance;
    await fixture.whenStable();
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture.destroy();
    vi.useRealTimers();
    vi.unstubAllGlobals();
    if (originalHidden) Object.defineProperty(document, 'hidden', originalHidden);
    else delete (document as unknown as Record<string, unknown>)['hidden'];
  });

  it('renders the supplied home with one title, local images and no standalone script', () => {
    expect(component).toBeTruthy();
    const host: HTMLElement = fixture.nativeElement;
    expect(host.querySelectorAll('h1')).toHaveLength(1);
    expect(host.querySelector('#hero-title')?.textContent).toContain('Serviços presenciais.');
    expect(host.querySelectorAll('.services-stack-img')).toHaveLength(4);
    expect([...host.querySelectorAll('img')].every(image => image.getAttribute('src')?.startsWith('/images/home/'))).toBe(true);
    expect(host.querySelector('script')).toBeNull();
  });

  it('opens the menu and closes it with Escape, returning focus', () => {
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('#menu-toggle');
    // jsdom não aplica o viewport móvel às media queries de CSS.
    button.style.display = 'inline-flex';
    button.click();
    fixture.detectChanges();
    expect(button.getAttribute('aria-expanded')).toBe('true');
    expect(fixture.nativeElement.querySelector('#primary-navigation').classList.contains('is-open')).toBe(true);
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    fixture.detectChanges();
    expect(button.getAttribute('aria-expanded')).toBe('false');
    expect(document.activeElement === button).toBe(true);
  });

  it('closes the mobile menu on an internal link, an outside click and a desktop viewport', () => {
    const host: HTMLElement = fixture.nativeElement;
    const button: HTMLButtonElement = host.querySelector('#menu-toggle')!;
    button.click();
    host.querySelector<HTMLAnchorElement>('.nav-link')!.click();
    fixture.detectChanges();
    expect(button.getAttribute('aria-expanded')).toBe('false');
    button.click();
    host.querySelector('main')!.click();
    fixture.detectChanges();
    expect(button.getAttribute('aria-expanded')).toBe('false');
    button.click();
    Object.assign(mobile, { matches: false });
    mobile.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(button.getAttribute('aria-expanded')).toBe('false');
  });

  it('does not navigate to registration or login before those pages exist', () => {
    const host: HTMLElement = fixture.nativeElement;
    expect(host.querySelector('a[href="/cadastro"], a[href="/login"]')).toBeNull();
    const actions = host.querySelectorAll<HTMLButtonElement>('button[title*="indisponível"]');
    expect(actions.length).toBeGreaterThan(0);
    expect([...actions].every(button => button.disabled)).toBe(true);
  });

  it('updates the photo on hover without treating services as search filters', () => {
    const host: HTMLElement = fixture.nativeElement;
    host.querySelector('[data-service-target="pintura"]')!.dispatchEvent(new MouseEvent('mouseenter'));
    fixture.detectChanges();
    expect(host.querySelector('.services-stack-img.is-active')?.getAttribute('data-service-img')).toBe('pintura');
    expect(host.querySelector('.services-stack-img.is-active')?.getAttribute('aria-hidden')).toBe('false');
    expect(host.querySelector('.services-cards-grid button, .services-cards-grid input')).toBeNull();
  });

  it('pauses motion and respects a change to reduced motion', () => {
    vi.useFakeTimers();
    const host: HTMLElement = fixture.nativeElement;
    const cycles = ObserverStub.instances.find(observer => observer.observe.mock.calls.some(([target]) => target.id === 'services-photo-stack'))!;
    cycles.intersect(host.querySelector('#services-photo-stack')!);
    cycles.intersect(host.querySelector('#steps-timeline')!);
    vi.advanceTimersByTime(4200);
    fixture.detectChanges();
    expect(host.querySelector('.services-stack-img.is-active')?.getAttribute('data-service-img')).toBe('pintura');
    host.querySelector<HTMLButtonElement>('.motion-toggle')!.click();
    fixture.detectChanges();
    expect(host.querySelector('.motion-toggle')?.getAttribute('aria-pressed')).toBe('true');
    vi.advanceTimersByTime(8400);
    fixture.detectChanges();
    expect(host.querySelector('.services-stack-img.is-active')?.getAttribute('data-service-img')).toBe('pintura');
    host.querySelector<HTMLButtonElement>('.motion-toggle')!.click();
    Object.assign(reduced, { matches: true });
    reduced.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(host.querySelector<HTMLButtonElement>('.motion-toggle')!.disabled).toBe(true);
    expect(host.classList.contains('js-enabled')).toBe(false);
  });

  it('disconnects observers and stops animation intervals when destroyed', () => {
    vi.useFakeTimers();
    const host: HTMLElement = fixture.nativeElement;
    const cycles = ObserverStub.instances.find(observer => observer.observe.mock.calls.some(([target]) => target.id === 'services-photo-stack'))!;
    cycles.intersect(host.querySelector('#services-photo-stack')!);
    cycles.intersect(host.querySelector('#steps-timeline')!);
    const clearInterval = vi.spyOn(window, 'clearInterval');
    fixture.destroy();
    expect(ObserverStub.instances.every(observer => observer.disconnect.mock.calls.length > 0)).toBe(true);
    expect(clearInterval.mock.calls.filter(([id]) => id !== undefined)).toHaveLength(2);
    clearInterval.mockRestore();
  });
});
