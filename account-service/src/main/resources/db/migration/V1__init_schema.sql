CREATE TABLE IF NOT EXISTS account (
    id VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    balance DECIMAL(38,2) NOT NULL,
    currency VARCHAR(255) NOT NULL,
    customer_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_date DATETIME(6) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    last_modified_date DATETIME(6),
    last_modified_by VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY uk_account_customer_id (customer_id),
    UNIQUE KEY uk_account_email (email)
);

CREATE TABLE IF NOT EXISTS operation (
    id VARCHAR(255) NOT NULL,
    date_time DATETIME(6) NOT NULL,
    amount DECIMAL(38,2) NOT NULL,
    type TINYINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    account_id VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_operation_account FOREIGN KEY (account_id) REFERENCES account(id)
);

CREATE TABLE IF NOT EXISTS counter (
    id BIGINT AUTO_INCREMENT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS account_email_customer_id (
    account_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    customer_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (account_id),
    UNIQUE KEY uk_aeci_email (email),
    UNIQUE KEY uk_aeci_customer_id (customer_id)
);

CREATE TABLE IF NOT EXISTS token_entry (
    processor_name VARCHAR(255) NOT NULL,
    segment INT NOT NULL,
    token BLOB,
    token_type VARCHAR(255),
    timestamp VARCHAR(255),
    owner VARCHAR(255),
    PRIMARY KEY (processor_name, segment)
);
