CREATE TABLE IF NOT EXISTS customer (
    id VARCHAR(255) NOT NULL,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    place_of_birth VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    nationality VARCHAR(255) NOT NULL,
    gender VARCHAR(255) NOT NULL,
    cin VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_date DATETIME(6) NOT NULL,
    created_by VARCHAR(255),
    last_modified_date DATETIME(6),
    last_modified_by VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY uk_customer_cin (cin),
    UNIQUE KEY uk_customer_email (email)
);
