USE esun;
DELIMITER $$
DROP PROCEDURE IF EXISTS sp_get_like_list_by_user$$
CREATE PROCEDURE sp_get_like_list_by_user(IN p_user VARCHAR(20))
BEGIN
 SELECT l.*,p.product_name,p.price,p.fee_rate,u.email
 FROM like_list l JOIN products p ON p.no=l.product_no JOIN users u ON u.user_id=l.user_id
 WHERE l.user_id=p_user ORDER BY l.sn DESC;
END$$
DROP PROCEDURE IF EXISTS sp_get_like_list_item$$
CREATE PROCEDURE sp_get_like_list_item(IN p_user VARCHAR(20),IN p_sn BIGINT)
BEGIN
 SELECT l.*,p.product_name,p.price,p.fee_rate,u.email
 FROM like_list l JOIN products p ON p.no=l.product_no JOIN users u ON u.user_id=l.user_id
 WHERE l.user_id=p_user AND l.sn=p_sn;
END$$
DROP PROCEDURE IF EXISTS sp_create_like_list$$
CREATE PROCEDURE sp_create_like_list(IN p_user VARCHAR(20),IN p_product BIGINT,IN p_quantity INT)
BEGIN
 DECLARE v_price DECIMAL(18,2) DEFAULT NULL;
 DECLARE v_rate DECIMAL(10,6);
 DECLARE v_account VARCHAR(30) DEFAULT NULL;
 DECLARE v_fee DECIMAL(18,2);
 DECLARE v_sn BIGINT;
 IF p_quantity IS NULL OR p_quantity NOT BETWEEN 1 AND 1000000 THEN
  SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO=30002,MESSAGE_TEXT='購買數量須介於 1 至 1000000';
 END IF;
 SELECT price,fee_rate INTO v_price,v_rate FROM products WHERE no=p_product FOR SHARE;
 SELECT account INTO v_account FROM users WHERE user_id=p_user FOR SHARE;
 IF v_price IS NULL OR v_account IS NULL THEN
  SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO=30001,MESSAGE_TEXT='找不到商品或會員';
 END IF;
 SET v_fee=ROUND(v_price*p_quantity*v_rate,2);
 INSERT INTO like_list(user_id,product_no,purchase_quantity,account,total_fee,total_amount)
 VALUES(p_user,p_product,p_quantity,v_account,v_fee,v_price*p_quantity+v_fee);
 SET v_sn=LAST_INSERT_ID();
 CALL sp_get_like_list_item(p_user,v_sn);
END$$
DROP PROCEDURE IF EXISTS sp_update_like_list$$
CREATE PROCEDURE sp_update_like_list(IN p_user VARCHAR(20),IN p_sn BIGINT,IN p_product BIGINT,IN p_quantity INT)
BEGIN
 DECLARE v_owner VARCHAR(20) DEFAULT NULL;
 DECLARE v_price DECIMAL(18,2) DEFAULT NULL;
 DECLARE v_rate DECIMAL(10,6);
 DECLARE v_account VARCHAR(30);
 DECLARE v_fee DECIMAL(18,2);
 IF p_quantity IS NULL OR p_quantity NOT BETWEEN 1 AND 1000000 THEN
  SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO=30002,MESSAGE_TEXT='購買數量須介於 1 至 1000000';
 END IF;
 SELECT user_id INTO v_owner FROM like_list WHERE sn=p_sn AND user_id=p_user FOR UPDATE;
 IF v_owner IS NULL THEN
  SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO=30001,MESSAGE_TEXT='找不到喜好紀錄';
 END IF;
 SELECT price,fee_rate INTO v_price,v_rate FROM products WHERE no=p_product FOR SHARE;
 IF v_price IS NULL THEN
  SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO=30001,MESSAGE_TEXT='找不到商品';
 END IF;
 SELECT account INTO v_account FROM users WHERE user_id=p_user FOR SHARE;
 SET v_fee=ROUND(v_price*p_quantity*v_rate,2);
 UPDATE like_list SET product_no=p_product,purchase_quantity=p_quantity,account=v_account,
 total_fee=v_fee,total_amount=v_price*p_quantity+v_fee WHERE sn=p_sn AND user_id=p_user;
 CALL sp_get_like_list_item(p_user,p_sn);
END$$
DROP PROCEDURE IF EXISTS sp_delete_like_list$$
CREATE PROCEDURE sp_delete_like_list(IN p_user VARCHAR(20),IN p_sn BIGINT)
BEGIN
 DELETE FROM like_list WHERE sn=p_sn AND user_id=p_user;
 IF ROW_COUNT()=0 THEN
  SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO=30001,MESSAGE_TEXT='找不到喜好紀錄';
 END IF;
END$$
DROP PROCEDURE IF EXISTS sp_record_like_event$$
CREATE PROCEDURE sp_record_like_event(IN p_user VARCHAR(20),IN p_sn BIGINT,IN p_action VARCHAR(10))
BEGIN
 INSERT INTO like_events(user_id,like_sn,action) VALUES(p_user,p_sn,p_action);
END$$
DELIMITER ;
