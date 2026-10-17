package com.Testesun.Testesunbank.common;

import org.springframework.http.HttpStatus;

// 業務檢查失敗時，帶著 HTTP 狀態碼與訊息交給統一錯誤處理。
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
