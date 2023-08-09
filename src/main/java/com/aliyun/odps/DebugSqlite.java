package com.aliyun.odps;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteOpenMode;

public class DebugSqlite {
  public void TestOne() throws ClassNotFoundException {
    Class.forName("org.sqlite.JDBC");
    Connection connection = null;
    try {
      SQLiteConfig config = new SQLiteConfig();
      config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);
      config.setOpenMode(SQLiteOpenMode.FULLMUTEX);
      config.setTransactionMode("DEFERRED");
      System.out.println(config.toProperties().toString());
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
    } catch (SQLException e) {
      // if the error message is "out of memory",
      // it probably means no database file is found
      System.err.println(e.getMessage());
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
}