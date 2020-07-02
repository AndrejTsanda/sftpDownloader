package com.tsanda.sftpDownloader;

import com.jcraft.jsch.JSchException;
import com.tsanda.sftpDownloader.mysql.MysqlClient;
import com.tsanda.sftpDownloader.sftp.SFTPClient;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class SFTPDownloader {

  private static final Logger logger = LogManager.getLogger(SFTPDownloader.class);

  public static void main(String[] args) {
    //
    String addressConfigFile = args[0];

    System.out.println("--- SFTP DOWNLOADER ---");
    Properties properties = new Properties();

    try {
      InputStream inputStream = new FileInputStream(addressConfigFile);
      properties.load(inputStream);

      SFTPClient sftpClient =
          new SFTPClient(
              properties.getProperty("sftp_host"),
              properties.getProperty("sftp_user"),
              properties.getProperty("sftp_password"),
              Integer.parseInt(properties.getProperty("sftp_port")));
      sftpClient.connect();

      MysqlClient mysqlClient =
          new MysqlClient(
              properties.getProperty("sql_database"),
              properties.getProperty("sql_user"),
              properties.getProperty("sql_password"));
      mysqlClient.connect();

      logger.debug("Downloading...");
      Vector listFiles = sftpClient.getListFiles(properties.getProperty("sftp_remote_dir"));
      File localDir = new File(properties.getProperty("local_dir"));
      if (listFiles != null && localDir.isDirectory()) {
        for (Object files : listFiles) {
          LsEntry lsEntry = (LsEntry) files;
          if (!lsEntry.getAttrs().isDir()) {
            sftpClient.download(
                properties.getProperty("sftp_remote_dir") + lsEntry.getFilename(),
                properties.getProperty("local_dir") + lsEntry.getFilename());
            mysqlClient.save(lsEntry.getFilename(), new Timestamp(System.currentTimeMillis()));
          }
        }
        logger.debug("Download is done.");
        mysqlClient.show();
      } else {
        if (!localDir.isDirectory()) {
          logger.warn("Local directory not found: " + localDir.toString());
        }
        logger.warn("Failed to download.");
      }

      mysqlClient.closeConnect();
      sftpClient.disconnect();
    } catch (FileNotFoundException e) {
      logger.warn(e);
    } catch (IOException e) {
      logger.warn(e);
    } catch (JSchException e) {
      logger.warn("Failed to connection sftp: " + e.getMessage());
    } catch (SQLException e) {
      logger.warn("Failed to connection database: " + e.getMessage());
    }
  }
}
