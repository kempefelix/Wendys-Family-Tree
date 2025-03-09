CREATE TABLE IF NOT EXISTS owner
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  email VARCHAR(255),
  description VARCHAR(4095)
);



CREATE TABLE IF NOT EXISTS horse
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(4095),
  date_of_birth DATE NOT NULL,
  sex ENUM('MALE', 'FEMALE') NOT NULL,
  image VARCHAR(255),
  owner_id BIGINT,
  parent_female_id BIGINT,
  parent_male_id BIGINT
);