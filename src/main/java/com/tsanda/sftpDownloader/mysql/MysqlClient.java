package com.tsanda.sftpDownloader.mysql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class MysqlClient {

  private static final Logger logger = LogManager.getLogger(MysqlClient.class);

  private String url;
  private String userName;
  private String password;
  private Connection connection = null;

  public MysqlClient(String url, String userName, String password) {
    this.url = url;
    this.userName = userName;
    this.password = password;
  }

  public void connect() {
    logger.debug("Connecting database...");
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      connection = DriverManager.getConnection(url, userName, password);
      logger.debug("Successful connection.");
    } catch (ClassNotFoundException e) {
      logger.warn("Connect to database failed." + e);
    } catch (SQLException e) {
      logger.warn("Connect to database failed." + e);
    }
  }

  public void save(String fileName, Timestamp timestamp) {
    try {
      Statement statement = connection.createStatement();
      statement.executeUpdate(
          "INSERT INTO info(file_name, download_date) VALUES ('"
              + fileName
              + "', '"
              + timestamp
              + "')");
    } catch (SQLException e) {
      logger.warn("Execute sql error" + e);
    }
  }

  public void show() {
    System.out.println(" --- List of downloaded files --- ");
    System.out.println("--------------------------------------");
    try {
      Statement statement = connection.createStatement();
      ResultSet resultSet = statement.executeQuery("SELECT * FROM info");
      while (resultSet.next()) {
        System.out.print(resultSet.getInt("id") + " | ");
        System.out.print(resultSet.getString("file_name") + " | ");
        System.out.println(resultSet.getTimestamp("download_date"));
      }
      System.out.println("--------------------------------------");
    } catch (SQLException e) {
      logger.warn("Execute sql error" + e);
    }
  }

  public void closeConnect() {
    try {
      if (connection != null) {
        connection.close();
        logger.debug("Connection to database closed.");
      }
    } catch (SQLException e) {
      logger.warn("Error close connection to database." + e);
    }
  }
}
