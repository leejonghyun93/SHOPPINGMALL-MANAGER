package org.kosa.shoppingmaillmanager.host.product;

import java.util.Map;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/products")
public class ProductRestController {

    private final ProductService productService;

    // 상품 목록 조회
    @GetMapping
    public ResponseEntity<ProductListResponse> getAllProducts(
            Pageable pageable,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Page<ProductDTO> page = productService.getAllProducts(pageable, status, categoryId, keyword);
        Map<String, Long> statusCounts = productService.getProductStatusCounts();

        ProductListResponse response = new ProductListResponse(
                page.getContent(),
                page.getTotalElements(),
                statusCounts
        );
        return ResponseEntity.ok(response);
    }

    // 진열여부(Y/N) 토글 API
    @PostMapping("/display-yn")
    public ResponseEntity<?> updateDisplayYn(@RequestBody DisplayYnRequest request) {
        productService.updateDisplayYn(request.getProductId(), request.getDisplayYn());
        return ResponseEntity.ok().build();
    }

    // 진열여부 변경 요청 DTO
    @Data
    public static class DisplayYnRequest {
        private String productId;
        private String displayYn; // Y or N
    }

    // 상품 목록 응답 DTO
    @Data
    @AllArgsConstructor
    public static class ProductListResponse {
        private List<ProductDTO> content;
        private long totalElements;
        private Map<String, Long> statusCounts;
    }
}
