package org.kosa.shoppingmaillmanager.host.product.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class ProductUpdateDto {
    private Long categoryId;
    private String name;
    private Integer price;
    private Integer salePrice;
    private Integer stock;
    private String productStatus;
    private String productShortDescription;
    private String productDescription;
    private MultipartFile mainImage; // 없으면 기존 이미지 그대로 유지

    private List<ProductOptionDto> options;
}
