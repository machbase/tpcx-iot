-- CREATE TAGDATA TABLE TAG (
--     tagid VARCHAR(255) primary key,
--     time datetime basetime,
--     value double summarized,
--     FIELD0 VARCHAR(1024)
-- );
CREATE TAGDATA TABLE TAG (
    tagid VARCHAR(255) primary key,
    time datetime basetime,
    value double summarized,
    FIELD0 VARCHAR(1024)
);
