--drop table tag;
---- create tagdata table tag (power_substation_key varchar(64) primary key, time datetime basetime, value double summarized, sensor_value varchar(20), sensor_unit varchar(34), padding varchar(995));
--CREATE TAGDATA TABLE TAG (
--    tagid VARCHAR(255) primary key,
--    time datetime basetime,
--    value double summarized,
--    FIELD0 VARCHAR(1024)
--);
delete from tag;
