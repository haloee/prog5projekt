ALTER TABLE listing
    CHANGE `condition` book_condition VARCHAR(20) NOT NULL,
    CHANGE `type` listing_type   VARCHAR(20) NOT NULL;
