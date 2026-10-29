import { createElement } from './dom';

const HOVER_CARD_ID = 'hover-card';
const SCREEN_MARGIN = 12;

let hoverBox: HTMLDivElement | undefined;

function getHoverBox(): HTMLDivElement {
  if (!hoverBox) {
    hoverBox = createElement('div', 'hover-card card shadow p-3 small');
    hoverBox.id = HOVER_CARD_ID;
    hoverBox.setAttribute('role', 'tooltip');
    hoverBox.hidden = true;
    document.body.append(hoverBox);
  }
  return hoverBox;
}

function placeNear(x: number, y: number): void {
  const box = getHoverBox();
  const left = Math.min(x + SCREEN_MARGIN, window.innerWidth - box.offsetWidth - SCREEN_MARGIN);
  const fitsBelow = y + SCREEN_MARGIN + box.offsetHeight <= window.innerHeight;
  const top = fitsBelow ? y + SCREEN_MARGIN : y - box.offsetHeight - SCREEN_MARGIN;
  box.style.left = `${Math.max(SCREEN_MARGIN, left)}px`;
  box.style.top = `${Math.max(SCREEN_MARGIN, top)}px`;
}

function showNear(x: number, y: number, content: HTMLElement): void {
  const box = getHoverBox();
  box.replaceChildren(content);
  box.hidden = false;
  placeNear(x, y);
}

export function hideHoverCard(): void {
  if (hoverBox) {
    hoverBox.hidden = true;
  }
}

export function attachHoverCard(target: HTMLElement | SVGElement, describe: () => HTMLElement): void {
  target.setAttribute('tabindex', '0');
  target.setAttribute('aria-describedby', HOVER_CARD_ID);

  target.addEventListener('mouseenter', (event) => {
    const { clientX, clientY } = event as MouseEvent;
    showNear(clientX, clientY, describe());
  });
  target.addEventListener('mousemove', (event) => {
    const { clientX, clientY } = event as MouseEvent;
    placeNear(clientX, clientY);
  });
  target.addEventListener('focus', () => {
    const { left, bottom } = target.getBoundingClientRect();
    showNear(left, bottom, describe());
  });
  target.addEventListener('mouseleave', hideHoverCard);
  target.addEventListener('blur', hideHoverCard);
}
