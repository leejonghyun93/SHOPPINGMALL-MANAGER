package org.kosa.shoppingmaillmanager.host.product.dto;

import lombok.Data;

// 옵션 정보 DTO
@Data
public class OptionDTO {
    private String optionName;
    private Integer salePrice;
    private Integer stock;
    private String status;
}
