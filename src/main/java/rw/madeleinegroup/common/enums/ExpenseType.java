package rw.madeleinegroup.common.enums;

/**
 * Expense classification persisted in MySQL column {@code expenses.type}.
 * Use SCREAMING_SNAKE_CASE; API/JSON field remains {@code category} on {@link rw.madeleinegroup.entity.Expense}.
 */
public enum ExpenseType {
    SALAIRE,
    MATERIEL,
    LOCATION,
    /** Legacy / French DB rows (rent); prefer {@link #LOCATION} for new API payloads. */
    LOYER,
    AUTRES
}
