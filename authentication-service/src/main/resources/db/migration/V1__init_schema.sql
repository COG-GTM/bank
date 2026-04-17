CREATE TABLE IF NOT EXISTS role (
    id BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    create_by VARCHAR(255),
    created_date DATETIME(6),
    last_modified_by VARCHAR(255),
    last_modified_date DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_name (name)
);

CREATE TABLE IF NOT EXISTS `user` (
    id VARCHAR(255) NOT NULL,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    place_of_birth VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    nationality VARCHAR(255) NOT NULL,
    gender VARCHAR(255) NOT NULL,
    cin VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    enabled BIT(1) NOT NULL,
    password_need_to_be_modified BIT(1) NOT NULL,
    last_login DATETIME(6),
    create_by VARCHAR(255),
    created_date DATETIME(6),
    last_modified_by VARCHAR(255),
    last_modified_date DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_cin (cin),
    UNIQUE KEY uk_user_email (email),
    UNIQUE KEY uk_user_username (username)
);

CREATE TABLE IF NOT EXISTS verification (
    id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    code VARCHAR(255) NOT NULL,
    expiry_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_verification_email (email)
);

CREATE TABLE IF NOT EXISTS user_role (
    user_id VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES `user`(id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(id)
);
