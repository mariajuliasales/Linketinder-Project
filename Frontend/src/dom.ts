export function el<K extends keyof HTMLElementTagNameMap>(tag: K, className = '', text?: string): HTMLElementTagNameMap[K] {
  const element = document.createElement(tag);
  
  if (className){
  element.className = className;
  }
  if (text !== undefined) {
    element.textContent = text;
  }

  return element;
}

export function badge(text: string, color = 'text-bg-secondary'): HTMLSpanElement {
  return el('span', `badge ${color} me-1`, text);
}


const hoverBox = el('div', 'hover-card card shadow p-3 small');
hoverBox.setAttribute('role', 'tooltip');
hoverBox.id = 'hover-card';
hoverBox.hidden = true;
document.body.append(hoverBox);

function placeHoverCard(x: number, y: number): void {
  const margin = 12;
  const left = Math.min(x + margin, window.innerWidth - hoverBox.offsetWidth - margin);
  const top = y + margin + hoverBox.offsetHeight > window.innerHeight ? y - hoverBox.offsetHeight - margin : y + margin;
  hoverBox.style.left = `${Math.max(margin, left)}px`;
  hoverBox.style.top = `${Math.max(margin, top)}px`;
}

export function hideHoverCard(): void {
  hoverBox.hidden = true;
}

export function hoverCard(target: HTMLElement | SVGElement, content: () => HTMLElement): void {
  target.setAttribute('tabindex', '0');
  target.setAttribute('aria-describedby', 'hover-card');

  const show = (x: number, y: number) => {
    hoverBox.replaceChildren(content());
    hoverBox.hidden = false;
    placeHoverCard(x, y);
  };

  target.addEventListener('mouseenter', (event) => show((event as MouseEvent).clientX, (event as MouseEvent).clientY));
  target.addEventListener('mousemove', (event) => placeHoverCard((event as MouseEvent).clientX, (event as MouseEvent).clientY));
  target.addEventListener('mouseleave', hideHoverCard);
  target.addEventListener('focus', () => {
    const rect = target.getBoundingClientRect();
    show(rect.left, rect.bottom);
  });
  target.addEventListener('blur', hideHoverCard);
}

export function lockedInfo(text: string): HTMLParagraphElement {
  return el('p', 'text-body-secondary mb-0 mt-2 border-top pt-2', `🔒 ${text}`);
}
