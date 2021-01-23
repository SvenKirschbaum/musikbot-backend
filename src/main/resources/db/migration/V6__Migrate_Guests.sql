/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;

create table guest
(
    id         bigint auto_increment
        primary key,
    identifier varchar(255) not null,
    token      varchar(255) null,
    constraint UK_jfsb0nflxyw8btb4lmq2j7g9w
        unique (identifier),
    constraint UK_m809bq1y0r18s1y6xrcvisghl
        unique (token)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

INSERT INTO guest (identifier)
SELECT DISTINCT guest_author
FROM song
WHERE guest_author IS NOT NULL;

ALTER TABLE song
    ADD tmp bigint(20);

UPDATE song s
    LEFT JOIN guest g ON s.guest_author = g.identifier
SET s.tmp = g.id
WHERE s.guest_author IS NOT NULL;

ALTER TABLE song
    DROP COLUMN guest_author;

ALTER TABLE song
    RENAME COLUMN tmp TO guest_author;

ALTER TABLE song
    ADD constraint guest_author
        foreign key (guest_author) references guest (id)