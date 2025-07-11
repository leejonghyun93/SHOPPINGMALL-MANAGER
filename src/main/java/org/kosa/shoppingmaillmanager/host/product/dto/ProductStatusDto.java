package org.kosa.shoppingmaillmanager.host.product.dto;

import lombok.Data;

@Data
public class ProductStatusDto {
    private int onSale;
    private int offSale;
    private int outOfStock;
}
