-- Remove duplicated customer email from account table
-- Accounts should reference customers only by customerId
ALTER TABLE account DROP COLUMN IF EXISTS email;

-- Remove email from account_email_customer_id validation table
ALTER TABLE account_email_customer_id DROP INDEX IF EXISTS UK_email;
ALTER TABLE account_email_customer_id DROP COLUMN IF EXISTS email;
