INSERT INTO accounts (account_number, user_id, account_type_id, balance, currency, insurance_benefit, has_premium_benefits)
VALUES
    ('MDL4T9XKQ8P3Z2', 1, (SELECT id FROM account_types WHERE type_name='STANDARD_CHECKING'), 4500.00, 'LEI', 0, FALSE),
    ('MDN7R2FV5W9Y6H', 1, (SELECT id FROM account_types WHERE type_name='PREMIUM_SAVINGS'), 2300.50, 'EUR', 50, TRUE);