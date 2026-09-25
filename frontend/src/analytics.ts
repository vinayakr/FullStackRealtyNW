declare global {
  interface Window {
    dataLayer?: unknown[]
    gtag?: (...args: unknown[]) => void
  }
}

export function trackLeadConversion() {
  window.gtag?.('event', 'conversion', {
    send_to: 'AW-18473319962/zIBWCIPAuYQdEJr84ehE',
    value: 1.0,
    currency: 'USD',
  })
}
