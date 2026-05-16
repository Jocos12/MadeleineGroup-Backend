package rw.madeleinegroup.common.enums;

/**
 * Workflow:
 * <ul>
 *   <li>MANAGER expenses: {@link #PENDING_APPROVAL} → {@link #FIRST_APPROVED} → {@link #PAID}</li>
 *   <li>CEO/ADMIN high amount: {@link #PENDING_FIRST_APPROVAL} → {@link #FIRST_APPROVED} → {@link #PAID}</li>
 *   <li>CEO/ADMIN low amount: {@link #PAID} immediately</li>
 * </ul>
 * Legacy rows may have null status (treated as PAID for totals).
 */
public enum ExpenseStatus {
    PENDING_APPROVAL,
    PENDING_FIRST_APPROVAL,
    FIRST_APPROVED,
    PAID,
    REJECTED
}
