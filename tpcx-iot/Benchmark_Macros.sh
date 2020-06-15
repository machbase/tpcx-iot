# All the command below are specific for HBase please change as needed for other clients/databases

CHECK_IF_TABLE_EXISTS="select_table.sql"

TRUNCATE_TABLE="truncate_table.sql"

CREATE_TABLE="create_table.sql"

CHECK_STATS_DB="machadmin -e | grep server"

COUNT_ROWS_IN_TABLE="select count(*) from tag"

SUT_TABLE_PATH="/home/interp/work/nfx/machbase_home/dbs"

ROW_COUNT="ROWS="

DB_HOST="localhost"

DB_PORT="23000"

SUT_SHELL="xargs machsql -s $DB_HOST -u SYS -p MANAGER -P $DB_PORT -i -f"

IOT_DATA_TABLE="TAG"

DB_BATCHSIZE=20000

SUT_PARAMETERS="machbase=machbase.host=$DB_HOST,machbase.port=$DB_PORT,machbase.batchsize=$DB_BATCHSIZE,machbase.debug=0"

COORDINATOR_HOME="/home/interp/work/nfx/test/nodes/coordinator1"
