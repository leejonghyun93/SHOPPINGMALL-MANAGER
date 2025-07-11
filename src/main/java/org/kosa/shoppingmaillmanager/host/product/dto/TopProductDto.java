package org.kosa.shoppingmaillmanager.host.product.dto;

import lombok.Data;

@Data
public class TopProductDto {
    private int productId;
    private String productName;
    
    // 판매 수량 기준
    private int totalQuantity;

    // 매출 금액 기준
    private int totalSales;
}
