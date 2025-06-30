package org.kosa.shoppingmaillmanager.host.order;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListDTO {
    private String order_id;       // 주문 ID
    private String user_id;        // 사용자 ID
    private String order_date;     // 주문일
    private String order_status;   // 주문 상태
    private int total_price;       // 총 금액
    
    // Member 테이블에서 불러올 정보
    private String user_name;           // 주문자 이름 (m.name)
    
    // Order_Item 테이블에서 불러올 정보
    private List<OrderItemDTO> orderItems;
}
