-- Expense classification: column is `category` (not `type`). Widen to VARCHAR for ExpenseType enum names.
ALTER TABLE expenses MODIFY COLUMN category VARCHAR(64) NULL;
