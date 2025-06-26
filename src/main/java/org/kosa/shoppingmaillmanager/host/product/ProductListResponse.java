package org.kosa.shoppingmaillmanager.host.product;

import java.util.List;
import java.util.Map;

import org.kosa.shoppingmaillmanager.host.product.dto.ProductDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponse {
    private List<ProductDTO> content;
    private long totalElements;
    private Map<String, Long> statusCounts;
}
