package org.kosa.shoppingmaillmanager.host.product.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDto {
    private Long categoryId;
    private String name;
    private Integer price;
    private Integer salePrice;
    private Integer stock;
    private String productStatus;
    private String productShortDescription;
    private String productDescription;
    private List<ProductOptionDto> options;
    // + MultipartFile mainImage
}