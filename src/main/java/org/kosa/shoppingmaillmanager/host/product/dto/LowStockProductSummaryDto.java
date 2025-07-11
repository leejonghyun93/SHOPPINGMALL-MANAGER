package org.kosa.shoppingmaillmanager.host.product.dto;

import java.util.List;

import lombok.Data;

@Data
public class LowStockProductSummaryDto {
    private int totalCount;
    private List<LowStockProductDto> products;
}
