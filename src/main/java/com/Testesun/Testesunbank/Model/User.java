package com.Testesun.Testesunbank.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;

// 對應資料庫查詢結果，使用一般 Java 類別儲存資料。
public class User {
    private String userId;

    private String userName;

    private String email;

    private String account;

    @JsonIgnore
    private String passwordHash;

    public User() {
    }

    public User(String userId, String userName, String email, String account, String passwordHash) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.account = account;
        this.passwordHash = passwordHash;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @JsonIgnore
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
