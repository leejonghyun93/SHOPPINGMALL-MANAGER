package org.kosa.shoppingmaillmanager.host.order;

import org.kosa.shoppingmaillmanager.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {
		private User user;
	
	 	private String order_id;               // 주문 ID
	    private String user_id;                // 사용자 ID
	    private String order_date;               // 주문일
	    private String order_status;           // 주문 상태

	    private String phone;                  // 전화번호
	    private String email;                  // 이메일

	    private String recipient_name;         // 수령인
	    private String recipient_phone;        // 수령인 전화번호
	    private String order_zipcode;          // 주문 우편번호
	    private String order_address_detail;   // 주문 상세주소
	    private String delivery_memo;          // 배송 메모

	    private int total_price;               // 총 금액
	    private int delivery_fee;              // 배송비
	    private int discount_amount;           // 할인 금액
	    private int used_point;                // 사용 포인트
	    private String payment_method;         // 결제 방법
	    private int saved_point;               // 적립 포인트
	    private String payment_method_name;    // 결제 수단명

	    private String shipping_date;            // 배송일
	    private String estimated_date;           // 예상 도착일
	    private String tracking_number;        // 추적 번호
	    private String delivery_company;       // 배송 업체

	    private String created_date;             // 생성일
	    private String updated_date;             // 수정일
	    
	    // Member 테이블에서 가져올 필드
	    private String name; // user.java : name (주문자 이름)
}
