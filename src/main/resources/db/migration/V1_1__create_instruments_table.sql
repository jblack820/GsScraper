CREATE TABLE IF NOT EXISTS public.instruments (
                             id BIGSERIAL PRIMARY KEY,
                             url TEXT NOT NULL,
                             date DATE NOT NULL,
                             title TEXT NOT NULL,
                             price TEXT,
                             titlePictureURL TEXT
);