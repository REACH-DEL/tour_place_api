CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE user_role AS ENUM ('admin', 'user');

CREATE TABLE users (
    user_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name      VARCHAR(150) NOT NULL,
    email          VARCHAR(150) UNIQUE NOT NULL,
    password       TEXT NOT NULL,
    status         BOOLEAN DEFAULT TRUE,
    role           user_role DEFAULT 'user',
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

INSERT INTO user VALUE(full_name='TheGoat', email='tirachlo34@gmail.com', password='$2a$10$Ntt5rQer0eCuZuOVEiCYVOpalvc.pKyrblpVGz30nvfB9q6o8GbWS', role='admin', status=true);