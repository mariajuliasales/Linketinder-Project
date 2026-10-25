import { attachHoverCard } from './hoverCard';

export type BarChartEntry = [label: string, value: number];

export interface BarChartOptions {
  ariaLabel: string;
  unit: string;
  describeEntry: (entry: BarChartEntry) => HTMLElement;
}

const SVG_NAMESPACE = 'http://www.w3.org/2000/svg';
const LAYOUT = {
  width: 660,
  maxWidth: '720px',
  rowHeight: 34,
  labelWidth: 170,
  labelGap: 8,
  maxBarWidth: 440,
  barHeight: 22,
  barTopOffset: 4,
  textBaseline: 18,
  valueGap: 6,
};

function svgElement<K extends keyof SVGElementTagNameMap>(
  tag: K,
  attributes: Record<string, string | number> = {},
): SVGElementTagNameMap[K] {
  const element = document.createElementNS(SVG_NAMESPACE, tag);
  Object.entries(attributes).forEach(([name, value]) => element.setAttribute(name, String(value)));
  return element;
}

function barRow([label, value]: BarChartEntry, index: number, maxValue: number, unit: string): SVGGElement {
  const y = index * LAYOUT.rowHeight;
  const barWidth = (value / maxValue) * LAYOUT.maxBarWidth;

  const row = svgElement('g', { class: 'chart-row', 'aria-label': `${label}: ${value} ${unit}` });
  const hoverArea = svgElement('rect', { y, width: LAYOUT.width, height: LAYOUT.rowHeight, fill: 'transparent' });
  const labelText = svgElement('text', {
    class: 'chart-text', x: LAYOUT.labelWidth - LAYOUT.labelGap, y: y + LAYOUT.textBaseline, 'text-anchor': 'end',
  });
  labelText.textContent = label;
  const bar = svgElement('rect', {
    class: 'chart-bar', x: LAYOUT.labelWidth, y: y + LAYOUT.barTopOffset, width: barWidth, height: LAYOUT.barHeight, rx: 4,
  });
  const valueText = svgElement('text', {
    class: 'chart-text', x: LAYOUT.labelWidth + barWidth + LAYOUT.valueGap, y: y + LAYOUT.textBaseline,
  });
  valueText.textContent = String(value);

  row.append(hoverArea, labelText, bar, valueText);
  return row;
}

export function barChart(entries: BarChartEntry[], { ariaLabel, unit, describeEntry }: BarChartOptions): SVGSVGElement {
  const maxValue = Math.max(1, ...entries.map(([, value]) => value));
  const svg = svgElement('svg', {
    viewBox: `0 0 ${LAYOUT.width} ${entries.length * LAYOUT.rowHeight}`, role: 'img', 'aria-label': ariaLabel,
  });
  svg.style.maxWidth = LAYOUT.maxWidth;
  svg.style.width = '100%';

  entries.forEach((entry, index) => {
    const row = barRow(entry, index, maxValue, unit);
    attachHoverCard(row, () => describeEntry(entry));
    svg.append(row);
  });
  return svg;
}
