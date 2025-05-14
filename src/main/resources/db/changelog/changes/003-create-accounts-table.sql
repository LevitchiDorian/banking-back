CREATE TABLE accounts (
                          account_number VARCHAR(50) PRIMARY KEY,
                          user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                          account_type_id BIGINT REFERENCES account_types(id) ON DELETE CASCADE,
                          balance DECIMAL(15, 2) DEFAULT 0,
                          currency VARCHAR(3) NOT NULL, -- LEI, EUR, USD etc.
                          insurance_benefit DECIMAL(15, 2) DEFAULT 0, -- InsuranceDecorator
                          has_premium_benefits BOOLEAN DEFAULT FALSE -- PremiumBenefitsDecorator
);