# 金融商品喜好系統

## 啟動方式

1. 安裝 Java 21、Maven 3.9+、MySQL 8.0.16+，並啟動 MySQL。
2. 預設資料庫帳密為 `root` / `123456`；若不同，請修改 `src/main/resources/application.properties` 的預設值。
3. 在專案根目錄執行：

   ```bash
   mvn spring-boot:run
   ```

首次啟動需連網下載依賴，會自動建置前端、建立資料庫及資料表。

開啟 http://localhost:8080/#/register，註冊後使用 Email 與密碼登入。
