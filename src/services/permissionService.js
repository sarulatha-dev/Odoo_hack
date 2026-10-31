/**
 * Permission Service – centralised role & action checks.
 *
 * The UI uses this helper to decide which buttons / pages to render.
 * It mirrors the role matrix described in the specification.
 *
 * NOTE: This is purely a front‑end convenience. Real authorization
 * must be enforced on the Spring Boot backend.
 */

export const Roles = {
  ADMIN: 'admin',
  STAFF: 'staff',
  CUSTOMER: 'customer',
};

// Map of role → module → allowed actions
const ACL = {
  [Roles.ADMIN]: {
    quotations: [
      'view',
      'create',
      'edit',
      'delete',
      'send',
      'accept',
      'reject',
      'print',
    ],
  },
  [Roles.STAFF]: {
    quotations: ['view', 'create', 'edit', 'delete', 'send', 'print'],
  },
  [Roles.CUSTOMER]: {
    quotations: ['view'],
  },
};

/**
 * Checks whether a role is allowed to perform a specific action on a module.
 *
 * @param {string} role   - one of Roles (admin, staff, customer)
 * @param {string} module - e.g. 'quotations'
 * @param {string} action - e.g. 'edit'
 * @returns {boolean}
 */
export function canAccess(role, module, action) {
  if (!role || !module || !action) return false;
  const modulePermissions = ACL[role]?.[module] ?? [];
  return modulePermissions.includes(action);
}
