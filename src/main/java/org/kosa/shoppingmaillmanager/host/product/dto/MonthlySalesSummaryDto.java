package org.kosa.shoppingmaillmanager.host.product.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlySalesSummaryDto {
	private int totalOrders;
    private int totalAmount;
    private int averageAmount;   // 평균 결제금액
}
