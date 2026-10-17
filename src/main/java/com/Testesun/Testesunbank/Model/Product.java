package com.Testesun.Testesunbank.Model;

import java.math.BigDecimal;

// 對應資料庫查詢結果，使用一般 Java 類別儲存資料。
public class Product {
    private long no;

    private String productName;

    private BigDecimal price;

    private BigDecimal feeRate;

    public Product() {
    }

    public Product(long no, String productName, BigDecimal price, BigDecimal feeRate) {
        this.no = no;
        this.productName = productName;
        this.price = price;
        this.feeRate = feeRate;
    }

    public long getNo() {
        return no;
    }

    public void setNo(long no) {
        this.no = no;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(BigDecimal feeRate) {
        this.feeRate = feeRate;
    }
}
