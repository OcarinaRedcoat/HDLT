CREATE DATABASE db;
CREATE USER 'hdlt'@'localhost' IDENTIFIED BY 'hdlt';
GRANT ALL PRIVILEGES ON db.* TO 'hdlt'@'localhost';
USE db;

CREATE TABLE users(username varchar(30) PRIMARY KEY, age varchar(30) NOT NULL);
INSERT INTO users VALUES ('ANDRE', '21');