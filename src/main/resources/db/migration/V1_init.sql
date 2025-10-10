CREATE TABLE app_user (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          password VARCHAR(255) NOT NULL,
                          display_name VARCHAR(100) NOT NULL,
                          role VARCHAR(20) NOT NULL DEFAULT 'USER',
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE book (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      title VARCHAR(255) NOT NULL,
                      author VARCHAR(255) NOT NULL,
                      isbn VARCHAR(20),
                      published_year INT,
                      created_by BIGINT,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      CONSTRAINT fk_book_user FOREIGN KEY (created_by) REFERENCES app_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE listing (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         owner_id BIGINT NOT NULL,
                         book_id BIGINT NOT NULL,
                         `condition` VARCHAR(20) NOT NULL,     -- NEW/LIKE_NEW/GOOD/USED
                         `type` VARCHAR(20) NOT NULL,          -- EXCHANGE/GIVEAWAY/SELL
                         price_huf INT,
                         status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE/RESERVED/CLOSED
                         note TEXT,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT fk_listing_owner FOREIGN KEY (owner_id) REFERENCES app_user(id),
                         CONSTRAINT fk_listing_book FOREIGN KEY (book_id) REFERENCES book(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_listing_status ON listing(status);
CREATE INDEX idx_listing_book ON listing(book_id);
