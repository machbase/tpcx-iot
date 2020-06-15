#!/bin/bash
source ./Benchmark_Macros.sh
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#  @author
#
#

curl -u Administrator:nbv12345 http://30.1.1.101:8091/pools/default/buckets/usertable/stats > test.json
var1=$( cat "test.json" | jq '.op.samples.ep_queue_size[0]' )
echo $var1
var2=$( cat "test.json" | jq '.op.samples.ep_flusher_todo[0]' )
echo $var2
var3=$( cat "test.json" | jq '.op.samples.ep_diskqueue_items[0]' )
echo $var3
var4=$( cat "test.json" | jq '.op.samples.vb_active_queue_size[0]' )
echo $var4
var5=$( cat "test.json" | jq '.op.samples.vb_replica_queue_size[0]' )
echo $var5
var6=$( cat "test.json" | jq '.op.samples.ep_dcp_replica_items_remaining[0]' )
echo $var6
var7=$( cat "test.json" | jq '.op.samples.curr_items[0]' )
num_rows=0
#if  [ $var1 -eq "0" ] && [ $var2 -eq "0" ] && [ $var3 -eq "0" ] && [ $var4 -eq "0" ] && [ $var5 -eq "0" ] && [ $var5 -eq "0" ] && [ $var6 -eq "0" ];  then
 num_rows=$var7
#fi
echo $num_rows 
`export num_rows=$num_rows`
