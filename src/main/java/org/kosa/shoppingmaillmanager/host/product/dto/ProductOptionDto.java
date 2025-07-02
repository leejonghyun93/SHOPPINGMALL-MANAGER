package org.kosa.shoppingmaillmanager.host.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 옵션 정보 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOptionDto {
    private String optionName;
    private Integer salePrice;
    private Integer stock;
    private String status;
}