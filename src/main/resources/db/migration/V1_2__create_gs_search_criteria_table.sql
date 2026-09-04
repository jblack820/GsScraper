CREATE TABLE IF NOT EXISTS public.search_keyword (
   id BIGSERIAL PRIMARY KEY,
   keyword TEXT NOT NULL,
   marketplace VARCHAR(50) NOT NULL,
   created_at TIMESTAMP DEFAULT now(),
   UNIQUE (keyword, marketplace)
    );

INSERT INTO public.search_keyword (keyword, marketplace) VALUES
                                                             ('vandenberg', 'GSFANATIC'),
                                                             ('charvel', 'GSFANATIC'),
                                                             ('js1200', 'GSFANATIC'),
                                                             ('jackson&soloist', 'GSFANATIC'),
                                                             ('jvm210', 'GSFANATIC'),
                                                             ('jvm205', 'GSFANATIC'),
                                                             ('rockman', 'GSFANATIC'),
                                                             ('dispatch', 'GSFANATIC'),
                                                             ('pedal&steel', 'GSFANATIC'),
                                                             ('steel&gitár', 'GSFANATIC'),
                                                             ('manouche', 'GSFANATIC'),
                                                             ('gypsy', 'GSFANATIC'),
                                                             ('jackson&professional', 'GSFANATIC'),
                                                             ('fender&player', 'GSFANATIC'),
                                                             ('axe&ultra', 'GSFANATIC'),
                                                             ('digitech&2101', 'GSFANATIC'),
                                                             ('dobro', 'GSFANATIC'),
                                                             ('MOOER&GE200', 'GSFANATIC'),
                                                             ('Ibanez&1200', 'GSFANATIC'),
                                                             ('Ibanez&1200CA', 'GSFANATIC'),
                                                             ('JS1200CA', 'GSFANATIC'),
                                                             ('EMG&81', 'GSFANATIC'),
                                                             ('Roland&JC', 'GSFANATIC'),
                                                             ('JC120', 'GSFANATIC'),
                                                             ('Richwood', 'GSFANATIC'),
                                                             ('Tonex', 'GSFANATIC'),
                                                             ('EMG81', 'GSFANATIC'),
                                                             ('gm-800', 'GSFANATIC'),
                                                             ('sy-1000', 'GSFANATIC')
    ON CONFLICT (keyword, marketplace) DO NOTHING;

INSERT INTO public.search_keyword (keyword, marketplace)
VALUES ('samsung 990 PRO', 'HARDVERAPRO') ON CONFLICT (keyword, marketplace) DO NOTHING;