-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server Version:               10.5.4-MariaDB-1:10.5.4+maria~focal - mariadb.org binary distribution
-- Server Betriebssystem:        debian-linux-gnu
-- HeidiSQL Version:             11.0.0.5919
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;

-- Exportiere Struktur von Tabelle musikbot-dev.locked_song
CREATE TABLE IF NOT EXISTS `locked_song`
(
    `id`    bigint(20) NOT NULL AUTO_INCREMENT,
    `title` varchar(255) DEFAULT NULL,
    `url`   varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 62
  DEFAULT CHARSET = utf8mb4;

-- Daten Export vom Benutzer nicht ausgewählt

-- Exportiere Struktur von Tabelle musikbot-dev.setting
CREATE TABLE IF NOT EXISTS `setting`
(
    `name`  varchar(100) NOT NULL,
    `value` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- Daten Export vom Benutzer nicht ausgewählt

-- Exportiere Struktur von Tabelle musikbot-dev.song
CREATE TABLE IF NOT EXISTS `song`
(
    `id`           bigint(20)   NOT NULL AUTO_INCREMENT,
    `duration`     int(11)      NOT NULL,
    `guest_author` varchar(255) DEFAULT NULL,
    `inserted_at`  datetime(6)  NOT NULL,
    `link`         varchar(255) NOT NULL,
    `played`       bit(1)       NOT NULL,
    `played_at`    datetime(6)  DEFAULT NULL,
    `skipped`      bit(1)       NOT NULL,
    `sort`         bigint(20)   DEFAULT NULL,
    `title`        varchar(255) NOT NULL,
    `user_author`  bigint(20)   DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `user_author` (`user_author`),
    CONSTRAINT `user_author` FOREIGN KEY (`user_author`) REFERENCES `user` (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 153891
  DEFAULT CHARSET = utf8mb4;

-- Daten Export vom Benutzer nicht ausgewählt

-- Exportiere Struktur von Tabelle musikbot-dev.SPRING_SESSION
CREATE TABLE IF NOT EXISTS `SPRING_SESSION`
(
    `PRIMARY_ID`            char(36) CHARACTER SET latin2 NOT NULL,
    `SESSION_ID`            char(36) CHARACTER SET latin2 NOT NULL,
    `CREATION_TIME`         bigint(20)                    NOT NULL,
    `LAST_ACCESS_TIME`      bigint(20)                    NOT NULL,
    `MAX_INACTIVE_INTERVAL` int(11)                       NOT NULL,
    `EXPIRY_TIME`           bigint(20)                    NOT NULL,
    `PRINCIPAL_NAME`        varchar(100) DEFAULT NULL,
    PRIMARY KEY (`PRIMARY_ID`),
    UNIQUE KEY `SPRING_SESSION_IX1` (`SESSION_ID`),
    KEY `SPRING_SESSION_IX2` (`EXPIRY_TIME`),
    KEY `SPRING_SESSION_IX3` (`PRINCIPAL_NAME`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC;

-- Daten Export vom Benutzer nicht ausgewählt

-- Exportiere Struktur von Tabelle musikbot-dev.SPRING_SESSION_ATTRIBUTES
CREATE TABLE IF NOT EXISTS `SPRING_SESSION_ATTRIBUTES`
(
    `SESSION_PRIMARY_ID` char(36) CHARACTER SET latin2     NOT NULL,
    `ATTRIBUTE_NAME`     varchar(200) CHARACTER SET latin2 NOT NULL,
    `ATTRIBUTE_BYTES`    blob                              NOT NULL,
    PRIMARY KEY (`SESSION_PRIMARY_ID`, `ATTRIBUTE_NAME`),
    CONSTRAINT `SPRING_SESSION_ATTRIBUTES_FK` FOREIGN KEY (`SESSION_PRIMARY_ID`) REFERENCES `SPRING_SESSION` (`PRIMARY_ID`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC;

-- Daten Export vom Benutzer nicht ausgewählt

-- Exportiere Struktur von Tabelle musikbot-dev.token
CREATE TABLE IF NOT EXISTS `token`
(
    `id`       bigint(20) NOT NULL AUTO_INCREMENT,
    `created`  datetime(6)  DEFAULT NULL,
    `token`    varchar(255) DEFAULT NULL,
    `owner_id` bigint(20) NOT NULL,
    `external` tinyint(1) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `owner_id` (`owner_id`),
    CONSTRAINT `owner_id` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 117
  DEFAULT CHARSET = utf8mb4;

-- Daten Export vom Benutzer nicht ausgewählt

-- Exportiere Struktur von Tabelle musikbot-dev.user
CREATE TABLE IF NOT EXISTS `user`
(
    `id`       bigint(20) NOT NULL AUTO_INCREMENT,
    `admin`    bit(1)     NOT NULL,
    `email`    varchar(255) DEFAULT NULL,
    `name`     varchar(255) DEFAULT NULL,
    `password` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `name` (`name`),
    UNIQUE KEY `email` (`email`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 79
  DEFAULT CHARSET = utf8mb4;

-- Daten Export vom Benutzer nicht ausgewählt

/*!40101 SET SQL_MODE = IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS = IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
