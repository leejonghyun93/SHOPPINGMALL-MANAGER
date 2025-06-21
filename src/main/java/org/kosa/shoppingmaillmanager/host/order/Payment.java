package org.kosa.shoppingmaillmanager.host.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {
	private String paymentId;           // 결제 ID (PK)
    private String orderId;             // 주문 ID (FK)
    private String invoicePoId;         // 송장 PO ID

    private int paymentAmount;          // 결제 금액
    private String paymentStatus;       // 결제 상태
    private String paymentMethod;       // 결제 방법 (CARD, KAKAO 등)

    private String bankName;            // 은행명
    private String cardName;            // 카드명
    private int paymentSecondAmount;    // 두 번째 결제 금액 (ex: 부분결제 시)

    private String paymentPcName;       // 결제한 PC 이름
    private String paymentCashName;     // 현금 결제 수단명
    private String paymentApprovalNo;   // 결제 승인 번호
    private int paymentInstallment;     // 할부 개월

    private String createdDate;           // 생성일
    private String updatedDate;           // 수정일
}
