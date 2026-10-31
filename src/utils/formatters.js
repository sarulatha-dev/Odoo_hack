const numberFormatter = new Intl.NumberFormat('en-IN')

const currencySymbols = {
  INR: '₹',
  USD: '$',
  SGD: 'S$',
  EUR: '€',
  GBP: '£',
  AED: 'AED ',
}

export function formatNumber(value) {
  return numberFormatter.format(value)
}

export function formatINR(value) {
  return `₹${numberFormatter.format(value)}`
}

export function formatINRCompact(value) {
  if (value >= 10000000) {
    return `₹${(value / 10000000).toFixed(1)}Cr`
  }

  if (value >= 100000) {
    return `₹${(value / 100000).toFixed(1)}L`
  }

  if (value >= 1000) {
    return `₹${(value / 1000).toFixed(0)}K`
  }

  return formatINR(value)
}

export function formatCurrency(value, currency) {
  const symbol = currencySymbols[currency] ?? ''
  return `${symbol}${numberFormatter.format(value)}`
}

export function formatDate(value) {
  const date = typeof value === 'string' ? new Date(`${value}T00:00:00`) : value

  return new Intl.DateTimeFormat('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(date)
}

export function formatLongDate(date = new Date()) {
  return new Intl.DateTimeFormat('en-IN', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(date)
}
// ---------- Quotation calculation helpers ----------

/**
 * Calculate subtotal (sum of unitPrice * quantity for each line item).
 * @param {Array<{unitPrice:number, quantity:number}>} items
 * @returns {number}
 */
export function calcSubtotal(items = []) {
  return items.reduce((sum, item) => sum + (item.unitPrice ?? 0) * (item.quantity ?? 0), 0);
}

/**
 * Calculate GST based on subtotal and configuration.
 * Supports two modes:
 *   - 'cgst_sgst': 50% CGST + 50% SGST of the gstRate
 *   - 'igst': full GST as gstRate
 * @param {number} subtotal
 * @param {string} gstType - 'cgst_sgst' | 'igst'
 * @param {number} gstRate - e.g., 0.18 for 18%
 * @returns {{cgst:number, sgst:number, igst:number, totalGST:number}}
 */
export function calcGST(subtotal, gstType = 'cgst_sgst', gstRate = 0.18) {
  const totalGST = subtotal * gstRate;
  if (gstType === 'igst') {
    return { cgst: 0, sgst: 0, igst: totalGST, totalGST };
  }
  // default CGST + SGST split equally
  const half = totalGST / 2;
  return { cgst: half, sgst: half, igst: 0, totalGST };
}

/**
 * Calculate the grand total after applying discount and GST.
 * @param {number} subtotal
 * @param {number} discount - absolute discount amount (not percentage)
 * @param {{totalGST:number}} gstObj
 * @returns {number}
 */
export function calcGrandTotal(subtotal, discount = 0, gstObj = { totalGST: 0 }) {
  const discounted = Math.max(subtotal - discount, 0);
  return discounted + (gstObj.totalGST ?? 0);
}
