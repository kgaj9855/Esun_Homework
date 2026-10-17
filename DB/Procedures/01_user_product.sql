USE esun;
DELIMITER $$
DROP PROCEDURE IF EXISTS sp_register_user$$
CREATE PROCEDURE sp_register_user(IN p_name VARCHAR(100),
 IN p_email VARCHAR(255), IN p_account VARCHAR(30), IN p_hash VARCHAR(255))
BEGIN
 DECLARE p_id VARCHAR(20);
 member_id: LOOP
   INSERT INTO member_id_sequence VALUES (NULL);
   SET p_id = CAST(LAST_INSERT_ID() AS CHAR);
   IF NOT EXISTS (SELECT 1 FROM users WHERE user_id=p_id) THEN
     LEAVE member_id;
   END IF;
 END LOOP;
 INSERT INTO users(user_id,user_name,email,account,password_hash)
 VALUES(p_id,p_name,LOWER(TRIM(p_email)),p_account,p_hash);
 SELECT user_id,user_name,email,account,password_hash FROM users WHERE user_id=p_id;
END$$
DROP PROCEDURE IF EXISTS sp_get_user_by_email$$
CREATE PROCEDURE sp_get_user_by_email(IN p_email VARCHAR(255))
BEGIN
 SELECT user_id,user_name,email,account,password_hash FROM users WHERE email=p_email;
END$$
DROP PROCEDURE IF EXISTS sp_get_products$$
CREATE PROCEDURE sp_get_products()
BEGIN
 SELECT no,product_name,price,fee_rate FROM products ORDER BY no;
END$$
DELIMITER ;
