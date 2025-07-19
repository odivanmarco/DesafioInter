CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    document VARCHAR(14) NOT NULL UNIQUE,
    user_type VARCHAR(2) NOT NULL
);


CREATE TABLE wallets (
    id VARCHAR(36) PRIMARY KEY,
    balance_brl DECIMAL(19, 2) NOT NULL,
    balance_usd DECIMAL(19, 2) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE remittances (
    id VARCHAR(36) PRIMARY KEY,
    sender_id VARCHAR(36) NOT NULL,
    receiver_id VARCHAR(36) NOT NULL,
    amount_brl DECIMAL(19, 2) NOT NULL,
    amount_usd DECIMAL(19, 2) NOT NULL,
    exchange_rate DECIMAL(10, 4) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_remittance_sender FOREIGN KEY (sender_id) REFERENCES users(id),
    CONSTRAINT fk_remittance_receiver FOREIGN KEY (receiver_id) REFERENCES users(id)
);
