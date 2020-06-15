# All the command below are specific for HBase please change as needed for other clients/databases

CHECK_IF_TABLE_EXISTS="select * from m$sys_tables where name = upper('usertable');"

TRUNCATE_TABLE="truncate table usertable;"

CREATE_TABLE="create table usertable (power_substation_key varchar(64), sensor_key varchar(64), datetime timestamp, sensor_value varchar(20), "

CHECK_STATS_DB="status 'simple'"

#COUNT_ROWS_IN_TABLE="count 'usertable', INTERVAL=>1000000"

COUNT_ROWS_IN_TABLE="hbase org.apache.hadoop.hbase.mapreduce.RowCounter usertable"

SUT_TABLE_PATH="/hbase/data/default/usertable/*/.regioninfo"

ROW_COUNT="ROWS="

SUT_SHELL="hbase shell"

IOT_DATA_TABLE="usertable"

SUT_PARAMETERS="columnfamily=cf"
