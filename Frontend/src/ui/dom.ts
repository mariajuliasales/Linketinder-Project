export function createElement<K extends keyof HTMLElementTagNameMap>(
  tag: K,
  className = '',
  text?: string,
): HTMLElementTagNameMap[K] {
  const element = document.createElement(tag);
  if (className) {
    element.className = className;
  }
  if (text !== undefined) {
    element.textContent = text;
  }
  return element;
}

export function requireElement<T extends Element>(root: ParentNode, selector: string): T {
  const element = root.querySelector<T>(selector);
  if (!element) {
    throw new Error(`Elemento não encontrado: ${selector}`);
  }
  return element;
}

export function elementFromTemplate(template: string): HTMLDivElement {
  const container = createElement('div');
  container.innerHTML = template;
  return container;
}

export interface CardClasses {
  card?: string;
  body?: string;
}

export function card(content: Node[], classes: CardClasses = {}): HTMLDivElement {
  const body = createElement('div', `card-body ${classes.body ?? ''}`.trim());
  body.append(...content);
  const wrapper = createElement('div', `card shadow-sm ${classes.card ?? ''}`.trim());
  wrapper.append(body);
  return wrapper;
}
