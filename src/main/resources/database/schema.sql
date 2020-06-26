DROP DATABASE IF EXISTS downloader_db;
CREATE DATABASE downloader_db;

DROP TABLE IF EXISTS `downloader_db`.`info`;
CREATE TABLE `downloader_db`.`info` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `file_name` VARCHAR(50) NOT NULL,
  `download_date` DATETIME,
  PRIMARY KEY(`id`)
);