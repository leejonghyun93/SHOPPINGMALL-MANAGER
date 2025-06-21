package org.kosa.shoppingmaillmanager.host.product;

import java.util.List;
import java.util.Map;

public class ProductListResponse {
    private List<ProductDTO> content;
    private long totalElements;
    private Map<String, Long> statusCounts;

    public ProductListResponse(List<ProductDTO> content, long totalElements, Map<String, Long> statusCounts) {
        this.content = content;
        this.totalElements = totalElements;
        this.statusCounts = statusCounts;
    }

    public List<ProductDTO> getContent() {
        return content;
    }

    public void setContent(List<ProductDTO> content) {
        this.content = content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public Map<String, Long> getStatusCounts() {
        return statusCounts;
    }

    public void setStatusCounts(Map<String, Long> statusCounts) {
        this.statusCounts = statusCounts;
    }
}
