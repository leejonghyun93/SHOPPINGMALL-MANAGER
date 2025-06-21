package org.kosa.shoppingmaillmanager.host.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItem {
	private String order_item_id;    // 주문 상품 ID (PK)
    private String order_id;        // 주문 ID (FK)
    private String product_id;      // 상품 ID (FK)
    private String name;           // 상품명
    private int quantity;          // 수량
    private String status;         // 상태 (예: 결제완료, 배송중, 취소 등)
    private int total_price;        // 총 가격
    private int delivery_fee;       // 배송비
    private String image_url;       // 이미지 URL

    private String created_date;      // 생성일
    private String updated_date;      // 수정일
    
    private String point_earned; // 적립 포인트
}
