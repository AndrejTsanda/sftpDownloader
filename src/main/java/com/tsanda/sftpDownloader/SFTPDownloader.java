package com.tsanda.sftpDownloader;

import com.tsanda.sftpDownloader.mysql.MysqlClient;
import com.tsanda.sftpDownloader.sftp.SFTPClient;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

public class SFTPDownloader {

  private static final Logger logger = LogManager.getLogger(SFTPDownloader.class);

  public static void main(String[] args) {
    //
    String addressConfigFile = args[0];

    System.out.println("--- SFTP DOWNLOADER ---");

    File file = new File(addressConfigFile);
    Map<String, String> mapConfig = new HashMap<String, String>();
    try {
      Scanner scanner = new Scanner(file);
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] dataConfig = line.split("=");
        mapConfig.put(dataConfig[0], dataConfig[1]);
      }
    } catch (FileNotFoundException e) {
      System.out.println("Config File not Found");
    }

    SFTPClient sftpClient =
        new SFTPClient(
            mapConfig.get("sftp_host"),
            mapConfig.get("sftp_user"),
            mapConfig.get("sftp_password"),
            Integer.parseInt(mapConfig.get("sftp_port")));
    sftpClient.connect();
    MysqlClient mysqlClient =
        new MysqlClient(
            mapConfig.get("sql_database") + "?serverTimezone=UTC", // serverTimezone=UTC",
            mapConfig.get("sql_user"),
            mapConfig.get("sql_password"));
    mysqlClient.connect();

    logger.debug("Downloading...");
    Vector listFiles = sftpClient.getListFiles(mapConfig.get("sftp_remote_dir"));
    for (Object files : listFiles) {
      LsEntry lsEntry = (LsEntry) files;
      sftpClient.download(
          mapConfig.get("sftp_remote_dir") + lsEntry.getFilename(),
          mapConfig.get("local_dir") + lsEntry.getFilename());
      mysqlClient.save(lsEntry.getFilename(), new Timestamp(System.currentTimeMillis()));
    }
    logger.debug("Download is done.");

    mysqlClient.show();

    mysqlClient.closeConnect();
    sftpClient.disconnect();
  }
}
