package org.kosa.shoppingmaillmanager.host.product.dto;

import lombok.Data;

@Data
public class ProductSearchCondition {
    private int page;
    private int size;
    private String status;
    private Integer categoryId;
    private String keyword;
    private String sort;
}