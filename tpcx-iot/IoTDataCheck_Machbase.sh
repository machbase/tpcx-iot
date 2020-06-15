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
#  @author Chinmayi Narasimhadevara
#
#
#   [Machbase's DataCheck Guide]
#
#   Here is `machcoordinatoradmin --cluster-status` example.
#   We want to get minimum replication number of each group.
#
#   In this case, each group has 2 normal warehouses. so the final result should be 2.
#   If one of those warehouses goes to 'unknown' or 'scrapped', 
#   then replication factor may be decreased.
#
#   +-------------+-----------------+-----------------+-----------------+--------------+
#   |  Node Type  |    Node Name    |   Group Name    |   Group State   |     State    |
#   +-------------+-----------------+-----------------+-----------------+--------------+
#   | coordinator | localhost:23110 | Coordinator     | normal          | primary      |
#   | coordinator | localhost:23120 | Coordinator     | normal          | normal       |
#   | deployer    | localhost:23210 | Deployer        | normal          | normal       |
#   | deployer    | localhost:23220 | Deployer        | normal          | normal       |
#   | broker      | localhost:23310 | Broker          | normal          | leader       |
#   | warehouse   | localhost:23410 | group1          | normal          | normal       |
#   | warehouse   | localhost:23420 | group1          | normal          | normal       |
#   | warehouse   | localhost:23510 | group2          | normal          | normal       |
#   | warehouse   | localhost:23520 | group2          | normal          | normal       |
#   +-------------+-----------------+-----------------+-----------------+--------------+

machcoordinatoradmin --cluster-status | grep warehouse | awk -F'|' '{print $4","$6}' | tr -d ' ' | awk -F, '{if ($2 == "normal") count[$1]++;} END {for (j in count) print count[j]}' | sort -n | head -n 1
