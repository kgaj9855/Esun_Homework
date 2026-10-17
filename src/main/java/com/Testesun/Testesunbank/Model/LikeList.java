package com.Testesun.Testesunbank.Model;

import java.math.BigDecimal;

// 對應資料庫查詢結果，使用一般 Java 類別儲存資料。
public class LikeList {
    private long sn;

    private long productNo;

    private String productName;

    private BigDecimal price;

    private BigDecimal feeRate;

    private int purchaseQuantity;

    private String account;

    private BigDecimal totalFee;

    private BigDecimal totalAmount;

    private String email;

    public LikeList() {
    }

    public LikeList(long sn, long productNo, String productName, BigDecimal price, BigDecimal feeRate, int purchaseQuantity, String account, BigDecimal totalFee, BigDecimal totalAmount, String email) {
        this.sn = sn;
        this.productNo = productNo;
        this.productName = productName;
        this.price = price;
        this.feeRate = feeRate;
        this.purchaseQuantity = purchaseQuantity;
        this.account = account;
        this.totalFee = totalFee;
        this.totalAmount = totalAmount;
        this.email = email;
    }

    public long getSn() {
        return sn;
    }

    public void setSn(long sn) {
        this.sn = sn;
    }

    public long getProductNo() {
        return productNo;
    }

    public void setProductNo(long productNo) {
        this.productNo = productNo;
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

    public int getPurchaseQuantity() {
        return purchaseQuantity;
    }

    public void setPurchaseQuantity(int purchaseQuantity) {
        this.purchaseQuantity = purchaseQuantity;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
