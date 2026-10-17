package com.Testesun.Testesunbank.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// 接收前端傳來的資料，註解負責檢查必填與格式。
public class CreateLikeListRequest {
    // 收藏的商品編號，必須填寫且大於 0。
    @NotNull(message = "請選擇商品")
    @Positive(message = "商品編號必須大於 0")
    private Long productNo;

    // 購買數量，允許範圍為 1 到 1000000。
    @NotNull(message = "請輸入購買數量")
    @Min(value = 1, message = "購買數量至少為 1")
    @Max(value = 1000000, message = "購買數量不得超過 1000000")
    private Integer purchaseQuantity;

    // Spring 接收前端 JSON 時，先建立物件，再透過 setter 填入資料。
    public CreateLikeListRequest() {
    }

    // Java 程式或測試也可以直接帶入商品編號與數量建立物件。
    public CreateLikeListRequest(Long productNo, Integer purchaseQuantity) {
        this.productNo = productNo;
        this.purchaseQuantity = purchaseQuantity;
    }

    public Long getProductNo() {
        return productNo;
    }

    public void setProductNo(Long productNo) {
        this.productNo = productNo;
    }

    public Integer getPurchaseQuantity() {
        return purchaseQuantity;
    }

    public void setPurchaseQuantity(Integer purchaseQuantity) {
        this.purchaseQuantity = purchaseQuantity;
    }
}
