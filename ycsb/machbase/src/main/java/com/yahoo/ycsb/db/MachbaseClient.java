/**
 * Copyright (c) 2013 - 2016 YCSB contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */

package com.yahoo.ycsb.db;

import com.machbase.jdbc.*;

import java.sql.*;
import com.yahoo.ycsb.*;

import java.util.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.io.*;

/**
 * A class that wraps the MachbaseClient to allow it to be interfaced with YCSB.
 * This class extends {@link DB} and implements the database interface used by YCSB client.
 */
public class MachbaseClient extends DB {
  public static final String URL_PROPERTY = "machbase.url";
  public static final String USER_PROPERTY = "machbase.user";
  public static final String HOST_PROPERTY = "machbase.host";
  public static final String PORT_PROPERTY = "machbase.port";
  public static final String BATCH_SIZE_PROPERTY = "machbase.batchsize";
  public static final String PASSWORD_PROPERTY = "machbase.password";
  public static final String DEBUG_PROPERTY = "machbase.debug";

  public static final String PRIMARY_KEY_NAME = "tagid";
  public static final String TIMESTAMP_NAME = "time";

  private boolean checkFutures;
  private String designDoc;
  private String viewName;
  private String tableName;
  private int    errorCheckCount = 100;

  private Properties props;
  private boolean  debug = false;
  private int  batchSize = 20000;
  private long numRowsInBatch = 0;
  private long insertTimestamp = 0;

  private Connection conn = null;
  private MachPreparedStatement prepareStmt = null;
  private MachPreparedStatement prepareScanStmt = null;

  private Connection        appendConn = null;
  private MachStatement     appendStmt = null;
  private ResultSet         appendResultSet = null;
  private ResultSetMetaData appendRsmd = null;

  private String            debugQueryStr = null;

  private Properties buildProperties(String propertiesFromString, String entrySeparator) throws IOException {
      Properties properties = new Properties();
      properties.load(new StringReader(propertiesFromString.replaceAll(entrySeparator, "\n")));
      return properties;
  }

  private void PrintDebug(String aHeader,  String aMsg)
  {
      if (this.debug)
      {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd H:m:s.S");
          String sCurrMilliSec = "[" + Thread.currentThread().getId() + "]" + "[" + aHeader + ":";
          sCurrMilliSec += sdf.format(System.currentTimeMillis()) + "] ";
          System.out.println(sCurrMilliSec + aMsg);
      }
  }

  public void connectServer() {
    Properties sProps = getProperties();
    String sPropString = sProps.getProperty("machbase", "");
    Properties sProps2 = null;
    try {
        sProps2 = buildProperties(sPropString, ",");
    } catch (Exception e) {
      System.err.println("Properties Exception : " + e.getMessage());
      e.printStackTrace(System.out);
    }

    String host = sProps2.getProperty(HOST_PROPERTY, "localhost");
    String port = sProps2.getProperty(PORT_PROPERTY, "5656");
    String url = sProps2.getProperty(URL_PROPERTY, "jdbc:machbase://" +host+ ":" +port+ "/mydb");

    String batchSizeStr = sProps2.getProperty(BATCH_SIZE_PROPERTY, "20000");
    if (batchSizeStr != null) {
        try {
            this.batchSize = Integer.parseInt(batchSizeStr);
        } catch (NumberFormatException nfe) {
            System.err.println("Invalid " + BATCH_SIZE_PROPERTY + " specified: " + batchSizeStr);
            nfe.printStackTrace(System.out);
        }
    } else {
        this.batchSize = 20000;
    }

    String debugStr = sProps2.getProperty(DEBUG_PROPERTY, "0");
    if (debugStr != null) {
        try {
            int debugInt = Integer.parseInt(debugStr);
            if (debugInt == 0) {
                this.debug = false;
            } else {
                this.debug = true;
            }
        } catch (NumberFormatException nfe) {
            System.err.println("Invalid " + DEBUG_PROPERTY+ " specified: " + debugStr);
            nfe.printStackTrace(System.out);
        }
    } else {
        this.debug = false;
    }

    PrintDebug("INIT", "Batchsize is " + this.batchSize);

    Properties systemProperties = System.getProperties();
    System.setProperties(systemProperties);

    try {
      Class.forName("com.machbase.jdbc.driver");
      conn = java.sql.DriverManager.getConnection(url, sProps2);

      // append connection
      appendConn = java.sql.DriverManager.getConnection(url, sProps2);
      appendStmt = (MachStatement)appendConn.createStatement();
      appendResultSet = appendStmt.executeAppendOpen("TAG", 5000);
      appendRsmd = appendResultSet.getMetaData();

      MachAppendCallback cb = new MachAppendCallback() {
          @Override
          public void onAppendError(long aErrNo, String aErrMsg, String aRowMsg) {
              System.out.format("Append Error : [%05d - %s]\n%s\n", aErrNo, aErrMsg, aRowMsg);
          }
      };

      appendStmt.executeSetAppendErrorCallback(cb);

    } catch (ClassNotFoundException ex) {
      System.err.println("ClassNotFoundException : unable to load machbase jdbc driver class");
      ex.printStackTrace(System.out);
    } catch (Exception e) {
      System.err.println("Exception : " + e.getMessage());
      e.printStackTrace(System.out);
    }
  }

  @Override
  public void init() throws DBException {
    try {
      connectServer();
    } catch (Exception e) {
      throw e;
    }
  }

  /**
   * Shutdown the client.
   */
  @Override
  public void cleanup() {
    try {
      if (prepareScanStmt != null) {
        prepareScanStmt.close();
        prepareScanStmt = null;
      }
      if (prepareStmt != null) {
        prepareStmt.close();
        prepareStmt = null;
      }
      if (appendStmt != null) {
          appendStmt.executeAppendClose();
          appendStmt = null;
      }
      if (appendConn != null) {
        appendConn.close();
        appendConn = null;
      }
      if (conn != null) {
        conn.close();
        conn = null;
      }
    } catch (SQLException e) {
      System.err.println("cleanup SQLException : " + e.getMessage());
    }
  }

  /* scan for ycsb test */
  public Status read(final String table, final String key, final Set<String> fields,
                     final HashMap<String, ByteIterator> result) {
    return Status.OK;
  }

  private static String joinFields(final Set<String> fields) {
    if (fields == null || fields.isEmpty()) {
      return "*";
    }

    StringBuilder builder = new StringBuilder();
    for (String f : fields) {
      builder.append("`").append(f).append("`").append(",");
    }

    String toReturn = builder.toString();
    return toReturn.substring(0, toReturn.length() - 1);
  }

  /* scan for ycsb test */
  @Override
  public Status scan(final String table, final String startkey, final int recordcount, final Set<String> fields,
                     final Vector<HashMap<String, ByteIterator>> result) {
    String scanSpecQuery = "SELECT " + joinFields(fields) + " FROM " + table + " WHERE " + PRIMARY_KEY_NAME
        + " = " + startkey.toString();
    long startTime = System.currentTimeMillis();

    try {
      if (conn == null) {
        connectServer();
      }

      prepareScanStmt = (MachPreparedStatement)conn.prepareStatement(scanSpecQuery);
      MachResultSet rs = (MachResultSet)prepareScanStmt.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      HashMap<String, ByteIterator> tuple = new HashMap<String, ByteIterator>(rs.getRowSize());
      for (int i = 0; i < rsmd.getColumnCount(); i++) {
        tuple.put(rsmd.getColumnName(i), new StringByteIterator(rs.getString(i)));
        result.add(tuple);
      }

      rs.close();
      prepareScanStmt.close();
      prepareScanStmt = null;

      if (result.size() == 0)
      {
          PrintDebug("SCAN()", "==========================================");
          PrintDebug("SCAN()", "scan() failed!!");
          PrintDebug("SCAN()", scanSpecQuery + "");
          PrintDebug("SCAN()", "==========================================");
      }
      else
      {
          long endTime = System.currentTimeMillis();
          PrintDebug("SCAN(v,v)", "Elapsed milliseconds : " + (endTime - startTime));
      }
    } catch (Exception e) {
      System.out.println(e.toString());
      System.out.println("Error while running query: start key = " + startkey + " count = " + recordcount);
      e.printStackTrace(System.out);
      return Status.ERROR;
    }
    return Status.OK;
  }

  @Override
  public Status update(final String table, final String key, final HashMap<String, ByteIterator> values) {
    return Status.OK;
  }

  @Override
  public Status insert(final String table, final String key, final HashMap<String, ByteIterator> values) {

    ArrayList<Object> sBuf = new ArrayList<Object>();

    try {
        int count = 1;
        String tempKey = "";
        String[] split = key.split(":");


        // for insert
        //if (!table.equals(tableName) || prepareStmt == null) {
        //    Iterator iterator = values.keySet().iterator();
        //    String keys = PRIMARY_KEY_NAME+","+TIMESTAMP_NAME+",value";
        //    String fieldsQues = "?,?,?";

        //    for (int i = 0; iterator.hasNext(); i++){
        //        keys += "," + (String)iterator.next();
        //        fieldsQues += ",?";
        //    }

        //    String insertQuery = "INSERT INTO TAG (" + keys + ") values (" + fieldsQues + ")";
        //    if (appendConn == null) {
        //        connectServer();
        //        // TODO appendOpen
        //    }
        //    prepareStmt = (MachPreparedStatement)conn.prepareStatement(insertQuery);
        //    tableName = table;
        //}

        /*
         * split[0]: filter
         * split[1]: clientFilter
         * split[2]: timestamp
         */

        //// tagid column = <filter>:<clientFilter>
        //prepareStmt.setString(count, split[0] + ":" + split[1]);
        //count++;
        //prepareStmt.setDate(count, new java.sql.Date(Long.parseLong(split[2])));
        //count++;
        //// unused values column. necessary for tag table
        //prepareStmt.setDouble(count, 0.0);
        //count++;

        sBuf.add(split[0] + ":" + split[1]);
        sBuf.add(Long.parseLong(split[2]) * 1000000); // split[2] is millisecond timestamp
        sBuf.add(0.0);

        for (Map.Entry<String, ByteIterator> entry : values.entrySet()) {
            tempKey = entry.getKey().toString();
            //prepareStmt.setString(count, entry.getValue().toString());
            sBuf.add(entry.getValue().toString());
            //count++;
        }

        numRowsInBatch++;
        appendStmt.executeAppendData(appendRsmd, sBuf);

        if (numRowsInBatch % this.batchSize == 0)
        {
            long start = System.currentTimeMillis();
            appendStmt.executeAppendFlush();
            PrintDebug("INSERT", "Elapsed milliseconds : " + (start - this.insertTimestamp));
            insertTimestamp = start;
        }

    } catch (Exception e) {
      System.out.println(e.toString());
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      String sCurrMilliSec = "[" + Thread.currentThread().getId() + "]" + "[INSERT:" + sdf.format(System.currentTimeMillis()) + "] ";
      System.out.print(sCurrMilliSec + "Error on APPEND DATA! (sBuf:" + sBuf + ")\n");
      System.out.println("Could not insert value for table: "+table+" key:" +key);
      e.printStackTrace(System.out);
      return Status.ERROR;
    }

    return Status.OK;
  }

  @Override
  public Status delete(final String table, final String key) {
    return Status.OK;
  }

  public String longToDateString(long milliSec) {
    Timestamp ts = new Timestamp(milliSec);
    DateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    return simple.format(new Date(ts.getTime()));
  }

  @Override
  public Status scan(String table,
                     String filter, // Sensor Name,
                     String clientFilter, // Client-Driver Id
                     String timestamp, // Record time stamp
                     Set<String> fields, // All fields are retrieved
                     long runStartTime,
                     Vector<HashMap<String, ByteIterator>> result1, // Data Structure for results from query 1
                     Vector<HashMap<String, ByteIterator>> result2) // Data Structure for results from query 2
  {
      long oldTimeStamp;
      // long timeGap = 1800000L;
      // long randTime = 0;

      long start = System.currentTimeMillis();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

      long longTimestamp = Long.valueOf(timestamp);

      Status s1 = scanHelper(table, filter, clientFilter, longTimestamp, fields, result1);
      String s1QueryStr = new String(this.debugQueryStr);

      if (runStartTime > 0L) {
          long time = longTimestamp - runStartTime;
          oldTimeStamp = longTimestamp - time;
      } else {
          oldTimeStamp = longTimestamp - 1800000L;
      } 
      long timestampVal = oldTimeStamp + (long)(Math.random() * (longTimestamp - 10000L - oldTimeStamp));

      Status s2 = scanHelper(table, filter, clientFilter, timestampVal, fields, result2);

      if (s1.isOk() && s2.isOk()) {
          double val = 0.0D;
          int i = 0;

          if (result1.size() == 0)
          {
              PrintDebug("SCAN(v,v)", "==============================================");
              PrintDebug("SCAN(v,v)", "scan(result1, result2)'s result1 does not have any result!");
              PrintDebug("SCAN(v,v)", "Query { " + s1QueryStr + " }");
              PrintDebug("SCAN(v,v)", "- timestamp            : "+timestamp);
              PrintDebug("SCAN(v,v)", "- Date(timestamp)      : "+sdf.format(Long.parseLong(timestamp)));
              PrintDebug("SCAN(v,v)", "- oldTimeStamp         : "+oldTimeStamp);
              PrintDebug("SCAN(v,v)", "- runStartTime         : "+runStartTime);
              PrintDebug("SCAN(v,v)", "result1 error! ================================");
          }

          long end = System.currentTimeMillis();
          PrintDebug("SCAN(v,v)", "Elapsed milliseconds : " + (end - start));

          return Status.OK;
      }
      else 
      {
          PrintDebug("SCAN(v,v)", "one of results got an error! ============================");
          return Status.ERROR;
      }
  }

  private Status scanHelper(String table, String filter, String clientFilter, long timestamp,
                            Set<String> fields, Vector<HashMap<String, ByteIterator>> result) {
    StringBuffer key = new StringBuffer();
    java.sql.Date startDate = new java.sql.Date(timestamp);
    java.sql.Date endDate = new java.sql.Date(timestamp + 5000L);
    int i = 0;
    int j = 0;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    key.append(clientFilter);
    key.append(":");
    key.append(filter);

    try {
      if (conn == null) {
        connectServer();
      }

      this.debugQueryStr  = "SELECT " + joinFields(fields) + " FROM TAG WHERE ";
      this.debugQueryStr += PRIMARY_KEY_NAME + " = '" + key.toString() + "' and " + TIMESTAMP_NAME;
      this.debugQueryStr += " between TO_DATE('" + sdf.format(timestamp) + "') and TO_DATE('" + sdf.format(timestamp + 5000L) + "');";

      prepareScanStmt = (MachPreparedStatement)conn.prepareStatement("SELECT " + joinFields(fields)
                                                                     + " FROM TAG WHERE " + PRIMARY_KEY_NAME + " = ? and " + TIMESTAMP_NAME
                                                                     + " between ? and ?");
      prepareScanStmt.setString(1, key.toString());
      prepareScanStmt.setDate(2, startDate);
      prepareScanStmt.setDate(3, endDate);

      MachResultSet rs = (MachResultSet)prepareScanStmt.executeQuery();
      MachResultSetMetaData rsmd = (MachResultSetMetaData)rs.getMetaData();

      for (i = 0; rs.next(); i++) {
        HashMap<String, ByteIterator> tuple = new HashMap<String, ByteIterator>();
        for (j = 0; j < rsmd.getColumnCount(); j++) {
          tuple.put(rsmd.getColumnName(j + 1).toLowerCase(), new StringByteIterator(rs.getString(j + 1)));
          result.add(tuple);
        }
      }

      rs.cleanRowSet();
      rs.close();
      prepareScanStmt.close();
      prepareScanStmt = null;
    } catch (Exception e) {
      System.out.println(e.toString());
      System.out.println("Error while running query: start key = " + key + " time = " + longToDateString(timestamp - 5000L) + " ~ "
                         + longToDateString(timestamp));
      return Status.ERROR;
    }
    return Status.OK;
  }
}
