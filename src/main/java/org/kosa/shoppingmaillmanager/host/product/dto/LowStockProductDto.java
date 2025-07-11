package org.kosa.shoppingmaillmanager.host.product.dto;

import lombok.Data;

@Data
public class LowStockProductDto {
    private String productId;
    private String name;
    private int stock;
}
