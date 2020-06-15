# All the command below are specific for HBase please change as needed for other clients/databases
# list all buckets instead
CHECK_IF_TABLE_EXISTS="select name from system:keyspaces;"

TRUNCATE_TABLE="delete from usertable;"

CREATE_TABLE="bucket-create -c  -c 30.1.1.101 -u Administrator -p nbv1234 --bucket usertable --bucket-type couchbase --bucket-size 153134"

CHECK_STATS_DB="server-info  -c 30.1.1.101 -u Administrator -p nbv1234"

#total amount of documents is listed in server-info, in "InterestingStats" section
COUNT_ROWS_IN_TABLE="server-info  -c 30.1.1.101 -u Administrator -p nbv1234"

#data and index locations are listed in server-info as well, in "storage" section
SUT_TABLE_PATH="server-info  -c 30.1.1.101 -u Administrator -p nbv1234"

# how this is different from COUNT_ROWS_IN_TABLE? 
ROW_COUNT=

SUT_SHELL="/opt/couchbase/bin/cbq -e=http://30.1.1.101:8093 -u=Administrator -p=nbv12345"

IOT_DATA_TABLE="usertable"

# I don't think we need any specific parameters
SUT_PARAMETERS="couchbase.host=30.1.1.102:couchbase.bucket=usertable:couchbase.password=nbv12345"

#SUT_PARAMETERS="couchbase.host=30.1.1.101"
SLEEP_TIME=50
# Additional command, will be requred for test execution against Couchbase - create a bucket user with admin role
#CREATE_USER="user-manage -c  -c 30.1.1.101 -u Administrator -p nbv1234 --set --rbac-username usertable --rbac-password nbv12345 --rbac-name usertable --roles admin --auth-domain local"

