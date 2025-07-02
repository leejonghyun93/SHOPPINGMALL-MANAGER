package org.kosa.shoppingmaillmanager.host.product.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductOption {
    private Integer optionId;
    private Integer productId;
    private String optionName;
    private Integer salePrice;
    private Integer stock;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
