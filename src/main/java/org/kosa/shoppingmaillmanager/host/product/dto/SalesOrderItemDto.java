package org.kosa.shoppingmaillmanager.host.product.dto;

import lombok.Data;

@Data
public class SalesOrderItemDto {
    // ✅ 결과 데이터용
    private String orderDate;              // 주문일시 (문자열로 받기)
    private String orderId;                // 주문번호
    private String productName;            // 상품명
    private int quantity;                  // 수량
    private String paymentMethodName;      // 결제 수단명
    private int totalPrice;                // 결제 금액

    // ✅ 검색 조건용
    private String startDate;              // 시작 날짜 (yyyy-MM-dd)
    private String endDate;                // 종료 날짜 (yyyy-MM-dd)
    private String productKeyword;         // 상품명 검색어
    private String paymentMethodKeyword;   // 결제 수단 검색어
}
