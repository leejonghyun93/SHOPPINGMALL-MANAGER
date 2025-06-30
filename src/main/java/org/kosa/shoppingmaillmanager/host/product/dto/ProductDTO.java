package org.kosa.shoppingmaillmanager.host.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.kosa.shoppingmaillmanager.host.product.entity.Product;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Integer productId;
    private Integer categoryId;
    private String name;
    private Integer price;
    private Integer salePrice;
    private String productDescription;
    private String productShortDescription;
    private String productStatus;
    private BigDecimal productRating;
    private Integer productReviewCount;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String mainImage;
    private Integer viewCount;
    private Integer stock;
    private String displayYn;

    // 추가: 대/중/소분류명
    private String mainCategoryName; // 대분류명
    private String midCategoryName;  // 중분류명
    private String subCategoryName;  // 소분류명

    // 엔티티 → DTO 변환 메서드
    public static ProductDTO fromEntity(Product product) {
        if (product == null) return null;
        ProductDTO dto = new ProductDTO(
            product.getProductId(),
            product.getCategoryId(),
            product.getName(),
            product.getPrice(),
            product.getSalePrice(),
            product.getProductDescription(),
            product.getProductShortDescription(),
            product.getProductStatus(),
            product.getProductRating(),
            product.getProductReviewCount(),
            product.getCreatedDate(),
            product.getUpdatedDate(),
            product.getMainImage(),
            product.getViewCount(),
            product.getStock(),
            product.getDisplayYn(),
            null, // mainCategoryName (추후 쿼리/매퍼에서 세팅)
            null, // midCategoryName
            null  // subCategoryName
        );
        return dto;
    }

}
}
