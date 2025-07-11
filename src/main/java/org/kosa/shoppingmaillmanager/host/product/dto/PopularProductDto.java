package org.kosa.shoppingmaillmanager.host.product.dto;

import lombok.Data;

@Data
public class PopularProductDto {
    private Integer productId;
    private String name;
    private String mainImage; // 썸네일 URL
    private Integer productSalesCount;
}