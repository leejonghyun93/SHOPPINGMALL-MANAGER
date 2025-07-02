package org.kosa.shoppingmaillmanager.host.product.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Product {
    private Integer productId;
    private String name;
    private Integer price;
    private Integer salePrice;
    private String productDescription;
    private String productShortDescription;
    private String productStatus;
    private Double productRating;
    private Integer productReviewCount;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String mainImage;
    private Integer viewCount;
    private Integer stock;
    private String hostId;
    private Long categoryId;
    private String displayYn;
}
