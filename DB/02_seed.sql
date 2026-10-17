USE esun;
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
