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

echo "$COUNT_ROWS_IN_TABLE" > count_rows_in_table.sql
num_rows=$(machsql -u SYS -p MANAGER -s $DB_HOST -P $DB_PORT -f count_rows_in_table.sql -i | grep -e '^[0-9]\+' | awk '{print $1}')
`export num_rows=$num_rows`
