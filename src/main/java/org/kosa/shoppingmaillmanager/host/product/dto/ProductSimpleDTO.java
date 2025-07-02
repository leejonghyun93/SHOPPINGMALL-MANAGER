package org.kosa.shoppingmaillmanager.host.product.dto;
import java.util.List;

import lombok.Data;

@Data
public class ProductSimpleDTO {
	
    private Integer productId;
    private String name;
    private Integer price;
    private Integer salePrice;
    private String productStatus;
    private String displayYn;
    private Integer stock;
    private Integer productSalesCount;
    private Integer productReviewCount;
    private String mainImage;
    private String createdDate;
    private String updatedDate;
    private Integer viewCount;
    private Integer categoryId;
    private String productShortDescription;
    private String productDescription;
    
    private List<ProductOptionDto> options;

    private String mainCategoryName;
    private String subCategoryName;
}