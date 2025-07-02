package org.kosa.shoppingmaillmanager.host.product;

import java.util.List;
import java.util.Map;

import org.kosa.shoppingmaillmanager.host.product.dto.ProductSimpleDTO;

import lombok.Data;



@Data
public class ProductListResponse {
    private List<ProductSimpleDTO> content;
    private int totalElements;
    private Map<String, Long> statusCounts;
    
    public ProductListResponse(List<ProductSimpleDTO> content, long totalElements, Map<String, Long> statusCounts) {
        this.content = content;
        this.totalElements = (int) totalElements; // 또는 캐스팅 처리
        this.statusCounts = statusCounts;
    }
}

