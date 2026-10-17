package com.Testesun.Testesunbank.config;

import com.zaxxer.hikari.HikariDataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

@Configuration
public class DatabaseConfig {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource dataSource(DataSourceProperties properties,
            @Value("${app.database.initialize:true}") boolean initialize) throws Exception {
        HikariDataSource source = properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        // 測試或管理員已建好資料庫時，可以關閉自動初始化。
        if (!initialize) {
            return source;
        }
        // MySQL 驅動會在第一次連線時建立不存在的資料庫。
        String url = source.getJdbcUrl();
        if (!url.startsWith("jdbc:mysql:")) {
            source.close();
            throw new IllegalStateException("自動初始化僅支援 MySQL；其他資料庫請設定 DB_INITIALIZE=false");
        }
        source.addDataSourceProperty("createDatabaseIfNotExist", "true");
        try (Connection connection = source.getConnection()) {
            // 依序建表，再建立會員／商品與收藏的程序。
            executeProcedureScript(connection, "00_initialize.sql");
            executeProcedureScript(connection, "01_user_product.sql");
            executeProcedureScript(connection, "02_like_list.sql");
            log.info("資料庫初始化完成：已檢查資料表、展示商品及 Stored Procedures");
            return source;
        } catch (Exception ex) {
            source.close();
            throw new IllegalStateException("資料庫初始化失敗，停止啟動；請確認 MySQL 服務、帳密與建庫／建表／建立程序權限", ex);
        }
    }

    // 程序內也有分號，因此須依 DELIMITER 切換整段 SQL 的結束符號。
    static void executeProcedureScript(Connection connection, String file) throws Exception {
        String sql = new ClassPathResource("db/procedures/" + file).getContentAsString(StandardCharsets.UTF_8);
        String separator = ";";
        StringBuilder block = new StringBuilder();
        for (String line : sql.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            // 使用 application.properties 的資料庫，略過手動腳本指定的 esun。
            if (trimmed.startsWith("CREATE DATABASE ") || trimmed.startsWith("USE ")) {
                continue;
            }
            if (trimmed.startsWith("DELIMITER ")) {
                executeBlock(connection, block.toString(), separator);
                block.setLength(0);
                separator = trimmed.substring("DELIMITER ".length()).trim();
            } else {
                block.append(line).append('\n');
            }
        }
        executeBlock(connection, block.toString(), separator);
    }

    private static void executeBlock(Connection connection, String sql, String separator) {
        if (sql.isBlank()) {
            return;
        }
        ScriptUtils.executeSqlScript(connection,
            new EncodedResource(new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8),
            false, false, "--", separator, "/*", "*/");
    }
}
