CREATE SCHEMA IF NOT EXISTS `tech`; --Remove this line if you have created `tech` schema
CREATE TABLE IF NOT EXISTS `tech`.`users` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `firstname` VARCHAR(45) NULL,
  `lastname` VARCHAR(45) NULL,
  `username` VARCHAR(45) NOT NULL,
  `password` VARCHAR(45) NOT NULL,
  `salt` VARCHAR(45) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB