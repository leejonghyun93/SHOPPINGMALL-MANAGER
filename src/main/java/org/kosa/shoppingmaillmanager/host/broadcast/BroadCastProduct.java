package org.kosa.shoppingmaillmanager.host.broadcast;

import org.kosa.shoppingmaillmanager.host.product.dto.ProductSimpleDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BroadCastProduct {
	private int broadcast_product_id;       // 방송-상품 연결 고유 ID
	private int broadcast_id;               // 방송 ID
	private int product_id;                 // 상품 ID
	private int display_order;              // 상품 표시 순서
	private boolean is_featured;            // 메인 상품 여부
	private int special_price;              // 방송 특가 (NULL이면 원래 가격)
	private String created_at;              // 생성일시
	private String updated_at;              // 수정일시
	
	private ProductSimpleDTO product;
}
