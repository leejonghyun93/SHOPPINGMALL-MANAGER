package org.kosa.shoppingmaillmanager.host.product;

import java.util.Map;

import org.kosa.shoppingmaillmanager.host.product.dto.ProductCreateRequest;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/products")
public class ProductRestController {

    private final ProductService productService;

    // 상품 목록 조회 (MyBatis 전용, page/size 파라미터)
    @GetMapping
    public ResponseEntity<ProductListResponse> getAllProducts(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        page = Math.max(1, page); // 1 미만이면 1로 보정
        ProductListResponse response = productService.getAllProducts(page, size, status, categoryId, keyword);
        return ResponseEntity.ok(response);
    }

    // 진열여부(Y/N) 토글 API
    @PostMapping("/display-yn")
    public ResponseEntity<?> updateDisplayYn(@RequestBody DisplayYnRequest request) {
        productService.updateDisplayYn(request.getProductId(), request.getDisplayYn());
        return ResponseEntity.ok().build();
    }

    // 진열여부 변경 요청 DTO (내부 static class)
    @Data
    public static class DisplayYnRequest {
        private Integer productId;
        private String displayYn; // Y or N
    }

    // 상품 상세 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Integer productId) {
        ProductDTO product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    // 상품 필드 부분 수정(PATCH)
    @PatchMapping("/{productId}")
    public ResponseEntity<Void> updateProductField(
        @PathVariable Integer productId,
        @RequestBody Map<String, Object> updates
    ) {
        productService.updateProductField(productId, updates);
        return ResponseEntity.ok().build();
    }

    // 상품 등록 API (대표이미지, 옵션, 카테고리, 상세설명 등 포함)
    // - @PostMapping(consumes = "multipart/form-data")로 파일 업로드 지원
    // - @ModelAttribute로 DTO와 파일을 한 번에 받음
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createProduct(
            @ModelAttribute ProductCreateRequest request // 파일+일반 필드 동시 수신
    ) {
        productService.createProduct(request);
        return ResponseEntity.ok().build();
    }
}
