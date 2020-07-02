package com.tsanda.sftpDownloader.sftp;

import com.jcraft.jsch.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Vector;

public class SFTPClient {

  private static final Logger logger = LogManager.getLogger(SFTPClient.class);

  private String host;
  private String userName;
  private String password;
  private int port;

  private Session session = null;
  private ChannelSftp channelSftp = null;

  public SFTPClient(String host, String userName, String password, int port) {
    this.host = host;
    this.userName = userName;
    this.password = password;
    this.port = port;
  }

  public void connect() throws JSchException {
    logger.debug("Connecting...");
    JSch jSch = new JSch();
    session = jSch.getSession(userName, host, port);
    session.setPassword(password);
    session.setConfig("StrictHostKeyChecking", "no");
    session.connect();
    Channel channel = session.openChannel("sftp");
    channel.connect();
    channelSftp = (ChannelSftp) channel;
    logger.debug("Successful connection.");
  }

  public Vector getListFiles(String path) {
    try {
      Vector listFiles = channelSftp.ls(path);
      return listFiles;
    } catch (SftpException e) {
      logger.warn("Something went wrong: " + e.getMessage());
    }
    return null;
  }

  public void download(String source, String destination) {
    try {
      channelSftp.get(source, destination);
    } catch (SftpException e) {
      logger.warn("Something went wrong: " + e);
    }
  }

  public void disconnect() {
    if (session != null) {
      session.disconnect();
      logger.debug("Disconnected.");
    } else {
      logger.debug("No connection.");
    }
  }
}
