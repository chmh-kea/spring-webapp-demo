CREATE TABLE IF NOT EXISTS users (
  `id` INT NOT NULL AUTO_INCREMENT,
  `firstname` VARCHAR(45) NULL,
  `lastname` VARCHAR(45) NULL,
  `username` VARCHAR(45) NOT NULL,
  `password` VARCHAR(45) NOT NULL,
  `salt` VARCHAR(45) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;
INSERT INTO users (firstname, lastname, username, password, salt) VALUES ('Jens', 'Jensen','jens1', 'notsecret', null);
INSERT INTO users (firstname, lastname, username, password, salt) VALUES ('Felix', 'Felixen','felix2', 'notsecret', null);
INSERT INTO users (firstname, lastname, username, password, salt) VALUES ('Marie', 'Mariesen','marie3', 'notsecret', null);