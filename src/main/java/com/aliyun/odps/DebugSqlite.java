package com.aliyun.odps;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteOpenMode;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class DebugSqlite {

  public static void main(String[] args) throws ClassNotFoundException {
    new DebugSqlite().testOne();
  }

  public void testOne() throws ClassNotFoundException {
    Class.forName("org.sqlite.JDBC");
    Connection connection = null;
    try {
      SQLiteConfig config = new SQLiteConfig();

//            config.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
//            config.setOpenMode(SQLiteOpenMode.READONLY);
//            config.setTransactionMode(SQLiteConfig.TransactionMode.IMMEDIATE);
//            config.setLockingMode(SQLiteConfig.LockingMode.NORMAL);
//            config.enableFullSync(false);

      config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);
      config.setOpenMode(SQLiteOpenMode.FULLMUTEX);
      config.setTransactionMode("DEFERRED");

      System.out.println(config.toProperties().toString());

      printMemoryUsage();

      connection = DriverManager.getConnection("jdbc:sqlite:/tmp/sample.db", config.toProperties());
      Statement statement = connection.createStatement();
      statement.setQueryTimeout(30); // set timeout to 30 sec.
      statement.executeUpdate("drop table if exists person");
      statement.executeUpdate("create table person (id integer, name string)");
      statement.executeUpdate("insert into person values(1, 'leo')");
      statement.executeUpdate("insert into person values(2, 'yui')");
      ResultSet rs = statement.executeQuery("select * from person");
      while (rs.next()) {
        // read the result set
        System.out.println("name = " + rs.getString("name"));
        System.out.println("id = " + rs.getInt("id"));
      }
    } catch (Exception e) {
      Arrays.stream(e.getStackTrace()).sequential().forEach(System.out::println);
      // if the error message is "out of memory",
      // it probably means no database file is found
      System.err.println("MESSAGE:" + e.getMessage());
    } finally {
      try {
        if (connection != null)
          connection.close();
      } catch (SQLException e) {
        // connection close failed.
        System.err.println(e.getMessage());
      }
    }
  }

  public static void printMemoryUsage() {
    System.out.println("Stack:" + Thread.currentThread().getStackTrace().length);

    RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    List<String> arguments = runtimeMxBean.getInputArguments();
    System.out.println("JVM arguments: " + arguments);

    MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
    System.out.print("Initial heap: " + heapMemoryUsage.getInit() / (1024 * 1024) + " MB");
    System.out.print(" | Used heap: " + heapMemoryUsage.getUsed() / (1024 * 1024) + " MB");
    System.out.print(" | Max heap: " + heapMemoryUsage.getMax() / (1024 * 1024) + " MB");
    System.out.println(" | Committed heap: " + heapMemoryUsage.getCommitted() / (1024 * 1024) + " MB");
  }
}