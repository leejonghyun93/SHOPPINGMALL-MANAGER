package org.kosa.shoppingmaillmanager.host.product.entity;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUCT_ID")
    private Integer productId;

    @Column(name = "NAME", length = 200, nullable = false)
    private String name;

    @Column(name = "PRICE", nullable = false)
    private Integer price;

    @Column(name = "SALE_PRICE", nullable = false)
    private Integer salePrice;

    @Column(name = "PRODUCT_DESCRIPTION", columnDefinition = "TEXT")
    private String productDescription;

    @Column(name = "PRODUCT_SHORT_DESCRIPTION", length = 500)
    private String productShortDescription;

    @Column(name = "PRODUCT_STATUS", length = 20, nullable = false)
    private String productStatus;

    @Column(name = "PRODUCT_RATING", precision = 3, scale = 2)
    private BigDecimal productRating = BigDecimal.ZERO;

    @Column(name = "PRODUCT_REVIEW_COUNT")
    private Integer productReviewCount = 0;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;

    @Column(name = "MAIN_IMAGE", length = 500)
    private String mainImage;

    @Column(name = "VIEW_COUNT")
    private Integer viewCount = 0;

    @Column(name = "STOCK")
    private Integer stock = 0;

    @Column(name = "HOST_ID", nullable = false)
    private Long hostId;

    @Column(name = "CATEGORY_ID", nullable = false)
    private Integer categoryId;

    @Column(name = "DISPLAY_YN", length = 1, nullable = false)
    private String displayYn = "Y";

}
