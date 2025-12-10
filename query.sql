CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE user_role AS ENUM ('admin', 'user');

CREATE TABLE users (
    user_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name      VARCHAR(150) NOT NULL,
    email          VARCHAR(150) UNIQUE NOT NULL,
    password       TEXT NOT NULL,
    status         BOOLEAN DEFAULT TRUE,
    role           user_role DEFAULT 'user',
    profile_image  TEXT,
    created_at     TIMESTAMP DEFAULT NOW(),
    updated_at     TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);

CREATE TABLE place (
    place_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    place_name     VARCHAR(255) NOT NULL,
    description    TEXT,
    main_image     TEXT,
    lat            DECIMAL(10, 7),
    longitude      DECIMAL(10, 7),
    created_at     TIMESTAMP DEFAULT NOW(),
    updated_at     TIMESTAMP DEFAULT NOW()
);

CREATE TABLE additional_image (
    image_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    image_url      TEXT NOT NULL
);

CREATE TABLE image_of_place (
    ip_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    place_id       UUID NOT NULL REFERENCES place(place_id) ON DELETE CASCADE,
    image_id       UUID NOT NULL REFERENCES additional_image(image_id) ON DELETE CASCADE
);

CREATE INDEX idx_image_of_place_place ON image_of_place(place_id);
CREATE INDEX idx_image_of_place_image ON image_of_place(image_id);

CREATE TABLE favorite (
    fav_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    place_id       UUID NOT NULL REFERENCES place(place_id) ON DELETE CASCADE,
    created_at     TIMESTAMP DEFAULT NOW(),
    updated_at     TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, place_id)
);

CREATE INDEX idx_favorite_user ON favorite(user_id);
CREATE INDEX idx_favorite_place ON favorite(place_id);

CREATE TABLE search_history (
    search_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    place_id       UUID NOT NULL REFERENCES place(place_id) ON DELETE CASCADE,
    created_at     TIMESTAMP DEFAULT NOW(),
    updated_at     TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_search_history_user ON search_history(user_id);
CREATE INDEX idx_search_history_place ON search_history(place_id);
CREATE INDEX idx_search_history_user_updated ON search_history(user_id, updated_at DESC);

CREATE TYPE activity_action AS ENUM ('PLACE_CREATED', 'PLACE_UPDATED', 'PLACE_DELETED', 'IMAGE_UPLOADED', 'IMAGE_DELETED', 'USER_REGISTERED', 'USER_ENABLED', 'USER_DISABLED', 'USER_ROLE_UPDATED');
CREATE TYPE entity_type AS ENUM ('PLACE', 'USER', 'IMAGE');

CREATE TABLE activity_log (
    activity_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    action           activity_action NOT NULL,
    entity_type      entity_type NOT NULL,
    entity_id        UUID,
    entity_name      VARCHAR(255),
    user_id          UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    created_at       TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_activity_log_created_at ON activity_log(created_at DESC);
CREATE INDEX idx_activity_log_user ON activity_log(user_id);
CREATE INDEX idx_activity_log_entity ON activity_log(entity_type, entity_id);

CREATE TABLE review (
    review_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    place_id        UUID NOT NULL REFERENCES place(place_id) ON DELETE CASCADE,
    rating          INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment         TEXT,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, place_id)
);

CREATE INDEX idx_review_user ON review(user_id);
CREATE INDEX idx_review_place ON review(place_id);
CREATE INDEX idx_review_place_created ON review(place_id, created_at DESC);
CREATE INDEX idx_review_rating ON review(place_id, rating);

INSERT INTO users (user_id, full_name, email, password, role, status) VALUES (gen_random_uuid(), 'TheGoat', 'tirachlo34@gmail.com', '$2a$10$Ntt5rQer0eCuZuOVEiCYVOpalvc.pKyrblpVGz30nvfB9q6o8GbWS', 'admin', true);