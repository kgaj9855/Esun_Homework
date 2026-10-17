-- Run once before starting the updated application. Existing member IDs are preserved.
USE esun;
CREATE TABLE IF NOT EXISTS member_id_sequence (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY
) ENGINE=InnoDB;
SOURCE DB/Procedures/01_user_product.sql;
