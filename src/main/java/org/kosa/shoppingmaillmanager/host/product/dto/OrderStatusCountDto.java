package org.kosa.shoppingmaillmanager.host.product.dto;

import lombok.Data;

@Data
public class OrderStatusCountDto {
    private int paid;         // 결제 완료
    private int preparing;    // 배송 준비중
    private int delivering;   // 배송중
    private int cancelled;    // 주문 취소
    private int returnRequested;  // 반품 요청
    private int exchangeRequested;  // 교환 요청
}