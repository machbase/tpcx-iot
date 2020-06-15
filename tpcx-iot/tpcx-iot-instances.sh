#!/bin/bash
# Usage: ./tpcx-iot-instances.sh 1000 4 100

# Usage check

counter=1
recordCount=$1
numInstances=$2
totalThreadCount=$3
start=0
operationCount=$((recordCount / numInstances))  # Improve this to be total of record count
echo "intance Operation: $operationCount"
threadCount=$((totalThreadCount / numInstances))
echo "instance Tread: $threadCount"

while [ $counter -le $numInstances ]
do

echo $counter

cat << EOF | tee ./tpc_iot_instance${counter}_workload
insertstart=$start
insertcount=$operationCount
recordcount=$recordCount
operationcount=$operationCount
workload=com.yahoo.ycsb.workloads.CoreWorkload
readallfields=true
readproportion=0.0
updateproportion=0.0
# scanproportion=0
insertproportion=1
threadcount=$threadCount
requestdistribution=uniform
EOF

echo "./tpcx-iot/bin/tpcx-iot load basic -P ./tpc_iot_instance${counter}_workload -s > /dev/shm/large$counter.dat"
nohup ./tpcx-iot/bin/tpcx-iot load basic -P ./tpc_iot_instance${counter}_workload -s > /dev/shm/large$counter.dat &

#echo "./tpcx-iot/bin/tpcx-iot run hbase12 -P ./tpc_iot_instance${counter}_workload -p columnfamily=cf -s > /dev/shm/hbase$counter.dat"
#nohup ./tpcx-iot/bin/tpcx-iot run hbase12 -P ./tpc_iot_instance${counter}_workload -p columnfamily=cf -s > /dev/shm/hbase$counter.dat &

start=$((operationCount * counter))

((counter++))

done
echo All done

