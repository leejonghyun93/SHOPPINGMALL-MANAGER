package org.kosa.shoppingmaillmanager.host.order;

import java.util.List;

import lombok.Data;
import lombok.Getter;

@Data
public class OrderDetailDTO {
	// tb_order 테이블
	private String order_id; // 주문 id
	private String user_id;  // 주문자 id
	private String order_date; // 주문일시
	private List<String> order_status; // 주문상태
	private String phone; // 주문자 전화번호
	private String email; // 주문자 이메일
	private String recipient_name; // 수령인 이름
	private String recipient_phone; // 수령인 전화번호
	private String order_zipcode; // 주문 우편번호
	private String order_address_detail; // 주문 상세주소
	private String delivery_memo; // 배송 메모
	private String order_memo; // 주문관리 메모 
	private List<String>payment_method; // 결제방식
	
	private int total_price; // 총 주문상품 가격
	private int delivery_fee; // 총 배송비
	private int discount_amount; // 총 할인 금액
	private int original_total_price; // 할인 전 총 금액
	private int final_payment_amount; // 총 금액
	
	// tb_order_item 테이블
	// 하나의 주문 번호에 여러 개의 주문 상품이 존재하기 때문에 List로 선언
	private List<OrderItem> orderItems;
	
	// tb_member 테이블
	private String user_name; // 주문자명 tb_member : name 컬럼
}
