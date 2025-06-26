package org.kosa.shoppingmaillmanager.host.product;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @Column(name = "PRODUCT_ID", length = 50)
    private String productId;

    @Column(name = "CATEGORY_ID", length = 50, nullable = false)
    private String categoryId;

    @Column(name = "NAME", length = 200, nullable = false)
    private String name;

    @Column(name = "PRICE", nullable = false)
    private Integer price;

    @Column(name = "SALE_PRICE")
    private Integer salePrice;

    @Column(name = "PRODUCT_DESCRIPTION", columnDefinition = "TEXT")
    private String productDescription;

    @Column(name = "PRODUCT_SHORT_DESCRIPTION", length = 500)
    private String productShortDescription;

    @Column(name = "PRODUCT_STATUS", length = 20, nullable = false)
    private String productStatus;

    @Column(name = "PRODUCT_SALES_COUNT")
    private Integer productSalesCount;

    @Column(name = "PRODUCT_RATING", precision = 3, scale = 2)
    private BigDecimal productRating;

    @Column(name = "PRODUCT_REVIEW_COUNT")
    private Integer productReviewCount;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;

    @Column(name = "START_DATE")
    private LocalDateTime startDate;

    @Column(name = "END_DATE")
    private LocalDateTime endDate;

    @Column(name = "MAIN_IMAGE", length = 500)
    private String mainImage;

    @Column(name = "VIEW_COUNT")
    private Integer viewCount;

    @Column(name = "STOCK")
    private Integer stock;
    
    @Column(name = "DISPLAY_YN", length = 1)
    private String displayYn;
}
