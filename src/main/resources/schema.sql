-- =============================================================
--  Bee Pollen & Plant Management System — MySQL Schema
-- =============================================================

CREATE DATABASE IF NOT EXISTS bee_pollen_db;
USE bee_pollen_db;

-- -------------------------------------------------
--  1. users
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)     UNIQUE NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    email       VARCHAR(100)    UNIQUE NOT NULL,
    full_name   VARCHAR(100)    NOT NULL,
    role        ENUM('ADMIN', 'RESEARCHER', 'BEEKEEPER', 'STUDENT', 'FARMER')
                                NOT NULL DEFAULT 'STUDENT',
    enabled     BOOLEAN         DEFAULT TRUE,
    created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------
--  2. plants
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS plants (
    id                BIGINT          AUTO_INCREMENT PRIMARY KEY,
    common_name       VARCHAR(100)    NOT NULL,
    scientific_name   VARCHAR(150)    UNIQUE NOT NULL,
    family            VARCHAR(100),
    genus             VARCHAR(100),
    flowering_season  VARCHAR(50),
    region            VARCHAR(100),
    description       TEXT,
    image_url         VARCHAR(500),
    created_at        TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------
--  3. pollens
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS pollens (
    id                       BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name                     VARCHAR(100)    NOT NULL,
    shape                    VARCHAR(50),
    size_micron              DOUBLE,
    surface_characteristic   VARCHAR(200),
    microscope_image         VARCHAR(500),
    created_at               TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------
--  4. plant_pollen (join table)
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS plant_pollen (
    plant_id    BIGINT  NOT NULL,
    pollen_id   BIGINT  NOT NULL,
    PRIMARY KEY (plant_id, pollen_id),

    CONSTRAINT fk_plant_pollen_plant
        FOREIGN KEY (plant_id)  REFERENCES plants(id)   ON DELETE CASCADE,

    CONSTRAINT fk_plant_pollen_pollen
        FOREIGN KEY (pollen_id) REFERENCES pollens(id)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------
--  5. bee_colonies
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS bee_colonies (
    id                    BIGINT          AUTO_INCREMENT PRIMARY KEY,
    colony_code           VARCHAR(50)     UNIQUE NOT NULL,
    bee_species           VARCHAR(100)    NOT NULL,
    latitude              DOUBLE,
    longitude             DOUBLE,
    health_status         ENUM('HEALTHY', 'WEAK', 'CRITICAL', 'DEAD')
                                          DEFAULT 'HEALTHY',
    estimated_population  INT,
    created_at            TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------
--  6. collection_tracking
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS collection_tracking (
    id                BIGINT      AUTO_INCREMENT PRIMARY KEY,
    colony_id         BIGINT      NOT NULL,
    pollen_id         BIGINT      NOT NULL,
    collected_weight  DOUBLE      NOT NULL,
    collection_date   DATE        NOT NULL,
    note              TEXT,
    created_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_collection_colony
        FOREIGN KEY (colony_id)  REFERENCES bee_colonies(id) ON DELETE CASCADE,

    CONSTRAINT fk_collection_pollen
        FOREIGN KEY (pollen_id)  REFERENCES pollens(id)      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
--  Indexes
-- =============================================================
CREATE INDEX idx_plants_common_name ON plants(common_name);
CREATE INDEX idx_plants_family      ON plants(family);
CREATE INDEX idx_pollens_name       ON pollens(name);
CREATE INDEX idx_bee_colonies_code  ON bee_colonies(colony_code);
CREATE INDEX idx_collection_date    ON collection_tracking(collection_date);
