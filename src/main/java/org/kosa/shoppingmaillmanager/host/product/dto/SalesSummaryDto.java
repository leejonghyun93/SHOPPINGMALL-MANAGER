package org.kosa.shoppingmaillmanager.host.product.dto;

import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesSummaryDto {

    // 공통
    private String type; // "daily" 또는 "monthly"
    private String label; // "2025-07-08" 또는 "2025-07" 같은 날짜 라벨
    private int totalSales; // 해당 일 또는 월의 매출 총액

    // 요약용
    private Integer totalAmount;       // 총 매출액 (최근 30일/12개월)
    private Integer averageAmount;     // 일 평균 또는 월 평균
    private String highestLabel;       // 최고 매출일 or 월
    private Integer highestAmount;     // 최고 매출액
}
