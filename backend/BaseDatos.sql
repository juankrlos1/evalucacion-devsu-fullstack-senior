-- BaseDatos.sql
-- Script loaded by Docker Compose in a single shared database: bp_banking_db.
-- It creates one schema per microservice and preloads data.

CREATE SCHEMA IF NOT EXISTS customer_service;

CREATE TABLE IF NOT EXISTS customer_service.customers (
  customer_id VARCHAR(60) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  gender VARCHAR(255),
  age INTEGER,
  identification VARCHAR(255) NOT NULL UNIQUE,
  address VARCHAR(255) NOT NULL,
  phone VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'USER',
  active BOOLEAN NOT NULL
);

INSERT INTO customer_service.customers (
  customer_id,
  name,
  gender,
  age,
  identification,
  address,
  phone,
  password,
  role,
  active
) VALUES
  (
    'CLI-ADMIN',
    'Juan Carlos De La Cruz',
    'Male',
    30,
    'ADMIN',
    'Quito',
    '0990000000',
    '1234',
    'ADMIN',
    TRUE
  ),
  (
    'CLI-001',
    'Jose Lema',
    'Male',
    32,
    '0102030405',
    'Otavalo sn y principal',
    '098254785',
    '1234',
    'USER',
    TRUE
  ),
  (
    'CLI-002',
    'Marianela Montalvo',
    'Female',
    30,
    '0102030406',
    'Amazonas y NNUU',
    '097548965',
    '5678',
    'USER',
    TRUE
  ),
  (
    'CLI-003',
    'Juan Osorio',
    'Male',
    28,
    '0102030407',
    '13 junio y Equinoccial',
    '098874587',
    '1245',
    'USER',
    TRUE
  )
ON CONFLICT (customer_id) DO NOTHING;

CREATE SCHEMA IF NOT EXISTS account_service;

CREATE TABLE IF NOT EXISTS account_service.accounts (
  account_number VARCHAR(50) PRIMARY KEY,
  account_type VARCHAR(50) NOT NULL,
  initial_balance NUMERIC(19, 2) NOT NULL,
  available_balance NUMERIC(19, 2) NOT NULL,
  active BOOLEAN NOT NULL,
  customer_id VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS account_service.movements (
  movement_id VARCHAR(36) PRIMARY KEY,
  account_number VARCHAR(50) NOT NULL,
  date TIMESTAMP NOT NULL,
  movement_type VARCHAR(50) NOT NULL,
  movement_value NUMERIC(19, 2) NOT NULL,
  balance NUMERIC(19, 2) NOT NULL,
  CONSTRAINT fk_movements_accounts
    FOREIGN KEY (account_number)
    REFERENCES account_service.accounts (account_number)
);

CREATE INDEX IF NOT EXISTS idx_accounts_customer_id
  ON account_service.accounts (customer_id);
CREATE INDEX IF NOT EXISTS idx_movements_account_number_date
  ON account_service.movements (account_number, date);

INSERT INTO account_service.accounts (
  account_number,
  account_type,
  initial_balance,
  available_balance,
  active,
  customer_id
) VALUES
  ('478758', 'Savings', 2000.00, 1425.00, TRUE, 'CLI-001'),
  ('225487', 'Checking', 100.00, 700.00, TRUE, 'CLI-002'),
  ('495878', 'Savings', 0.00, 150.00, TRUE, 'CLI-003'),
  ('496825', 'Savings', 540.00, 0.00, TRUE, 'CLI-002'),
  ('585545', 'Checking', 1000.00, 1000.00, TRUE, 'CLI-001')
ON CONFLICT (account_number) DO NOTHING;

INSERT INTO account_service.movements (
  movement_id,
  account_number,
  date,
  movement_type,
  movement_value,
  balance
) VALUES
  (
    '00000000-0000-0000-0000-000000000001',
    '478758',
    '2022-02-08 09:00:00',
    'Withdrawal',
    -575.00,
    1425.00
  ),
  (
    '00000000-0000-0000-0000-000000000002',
    '225487',
    '2022-02-10 09:00:00',
    'Deposit',
    600.00,
    700.00
  ),
  (
    '00000000-0000-0000-0000-000000000003',
    '495878',
    '2022-02-08 10:00:00',
    'Deposit',
    150.00,
    150.00
  ),
  (
    '00000000-0000-0000-0000-000000000004',
    '496825',
    '2022-02-08 11:00:00',
    'Withdrawal',
    -540.00,
    0.00
  )
ON CONFLICT (movement_id) DO NOTHING;
