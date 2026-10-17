package com.Testesun.Testesunbank.DTO;

import jakarta.validation.constraints.*;

// 接收前端傳來的資料，註解負責檢查必填與格式。
public class RegisterRequest {
    @NotBlank @Size(max = 100)
    private String userName;

    @NotBlank @Email @Size(max = 255)
    private String email;

    @NotBlank @Pattern(regexp = "[0-9]{10,30}", message = "帳號須為 10–30 位數字")
    private String account;

    @NotBlank @Size(min = 10, max = 72)
    private String password;

    public RegisterRequest() {
    }

    public RegisterRequest(String userName, String email, String account, String password) {
        this.userName = userName;
        this.email = email;
        this.account = account;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
