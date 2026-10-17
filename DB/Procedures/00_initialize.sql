-- Execute this SQL script once before installing the business procedures.
CREATE DATABASE IF NOT EXISTS esun CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE esun;
DELIMITER $$
DROP PROCEDURE IF EXISTS sp_initialize_database$$
CREATE PROCEDURE sp_initialize_database()
BEGIN
CREATE TABLE IF NOT EXISTS users (
  user_id VARCHAR(20) COLLATE utf8mb4_bin PRIMARY KEY,
  user_name VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  account VARCHAR(30) NOT NULL,
  password_hash VARCHAR(255) NOT NULL
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS products (
  no BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_name VARCHAR(200) NOT NULL,
  price DECIMAL(18,2) NOT NULL CHECK(price > 0 AND price <= 1000000000),
  fee_rate DECIMAL(10,6) NOT NULL CHECK(fee_rate >= 0 AND fee_rate <= 1)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS like_list (
  sn BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id VARCHAR(20) COLLATE utf8mb4_bin NOT NULL,
  product_no BIGINT NOT NULL,
  purchase_quantity INT NOT NULL CHECK(purchase_quantity BETWEEN 1 AND 1000000),
  account VARCHAR(30) NOT NULL,
  total_fee DECIMAL(18,2) NOT NULL CHECK(total_fee >= 0),
  total_amount DECIMAL(18,2) NOT NULL CHECK(total_amount > 0),
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  FOREIGN KEY (product_no) REFERENCES products(no),
  INDEX idx_like_user_sn(user_id,sn)
) ENGINE=InnoDB;
-- No FK to like_list: retain events after a favorite is deleted.
CREATE TABLE IF NOT EXISTS like_events (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id VARCHAR(20) COLLATE utf8mb4_bin NOT NULL,
  like_sn BIGINT NOT NULL,
  action VARCHAR(10) NOT NULL CHECK(action IN ('CREATE','UPDATE','DELETE')),
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  INDEX idx_event_user(user_id,created_at)
) ENGINE=InnoDB;

-- Separate sequence preserves existing member IDs and their foreign keys.
CREATE TABLE IF NOT EXISTS member_id_sequence (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY
) ENGINE=InnoDB;

-- Illustrative products, no real quotes or investment recommendations.
INSERT INTO products(no, product_name, price, fee_rate) VALUES
 (1,'全球永續股票基金',1250.00,0.015),
 (2,'優質企業債券基金',980.50,0.008),
 (3,'台灣成長股票基金',1680.00,0.012),
 (4,'美元短期債券基金',1050.00,0.005),
 (5,'全球多元資產基金',1120.00,0.010),
 (6,'亞洲科技股票基金',2150.75,0.018)
ON DUPLICATE KEY UPDATE no=VALUES(no);
-- Members are created via registration; no default credentials.

END$$
DELIMITER ;
CALL sp_initialize_database();
