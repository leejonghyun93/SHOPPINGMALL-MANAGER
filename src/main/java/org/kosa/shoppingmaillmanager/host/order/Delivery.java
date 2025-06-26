package org.kosa.shoppingmaillmanager.host.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Delivery {
	private String deliveryId;           // 배송 ID (PK)
    private String orderId;              // 주문 ID (FK)
    private String deliveryCompany;      // 배송 업체
    private String deliveryStatus;       // 배송 상태
    private String deliveryTracking;     // 송장 번호
    private String deliveryComplete;       // 배송 완료일

    private String deliveryRecipient;    // 수령인 이름
    private String deliveryRecipientPhone; // 수령인 전화번호
    private String deliveryZipcode;      // 우편번호
    private String deliveryAddress;      // 상세 주소
    private String deliveryMemo;         // 배송 메모

    private String createdDate;            // 생성일
    private String updatedDate;            // 수정일
}
