CREATE TABLE card (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    list_id BIGINT NOT NULL REFERENCES list (id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    position DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_card_list_id ON card (list_id);
