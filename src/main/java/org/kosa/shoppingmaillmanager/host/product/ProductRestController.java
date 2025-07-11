package org.kosa.shoppingmaillmanager.host.product;


import java.util.List;
import java.util.Map;

import org.kosa.shoppingmaillmanager.host.product.dto.LowStockProductSummaryDto;
import org.kosa.shoppingmaillmanager.host.product.dto.PopularProductDto;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductRequestDto;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductSearchCondition;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductSimpleDTO;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductStatusDto;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductUpdateDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/products")
public class ProductRestController {

    private final ProductService productService;

    // 상품 리스트 조회
    @GetMapping
    public ResponseEntity<ProductListResponse> getProductList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdDate") String sort
    ) {
        String userId = (String) request.getAttribute("userId");
        log.info("🔑 userId from JWT: {}", userId);

        ProductSearchCondition cond = new ProductSearchCondition();
        cond.setPage(page);
        cond.setSize(size);
        cond.setStatus(status);
        cond.setCategoryId(categoryId);
        cond.setKeyword(keyword);
        cond.setSort(sort);

        ProductListResponse response = productService.getProductList(userId, cond);
        return ResponseEntity.ok(response);
    }

    // 진열 여부 변경 (userId 포함)
    @PostMapping("/display-yn")
    public ResponseEntity<?> updateDisplayYn(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        String userId = (String) request.getAttribute("userId");
        Integer productId = (Integer) body.get("productId");
        String displayYn = (String) body.get("displayYn");

        if (productId == null || displayYn == null) {
            return ResponseEntity.badRequest().body("productId와 displayYn은 필수입니다.");
        }

        productService.updateDisplayYn(userId, productId, displayYn);
        return ResponseEntity.ok().build();
    }

    // 상품 상세 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ProductSimpleDTO> getProductDetail(
        @PathVariable Integer productId,
        HttpServletRequest request
    ) {
        String userId = (String) request.getAttribute("userId");
        ProductSimpleDTO dto = productService.getProductDetail(userId, productId);
        return ResponseEntity.ok(dto);
    }

    // 상품 필드 개별 수정
    @PatchMapping("/{productId}")
    public ResponseEntity<?> updateProductField(
        @PathVariable Integer productId,
        @RequestBody Map<String,Object> updates,
        HttpServletRequest request
    ) {
        String userId = (String) request.getAttribute("userId");
        productService.updateProductField(userId, productId, updates);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping
    public ResponseEntity<?> registerProduct(
        @RequestPart("product") ProductRequestDto productDto,
        @RequestPart("mainImage") MultipartFile mainImage,
        HttpServletRequest request
    ) {
        String userId = (String) request.getAttribute("userId");
        log.info("📦 상품 등록 요청 by userId: {}", userId);

        // 필드 유효성 검사 (프론트에도 있지만 백엔드에도 최소 검증)
        if (mainImage == null || mainImage.isEmpty()) {
            return ResponseEntity.badRequest().body("대표 이미지는 필수입니다.");
        }

        try {
            productService.registerProduct(userId, productDto, mainImage);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("❌ 상품 등록 실패", e);
            return ResponseEntity.internalServerError().body("상품 등록 중 오류가 발생했습니다.");
        }
    }
    
    @PostMapping("/{productId}/edit")
    public ResponseEntity<?> updateProduct(
            @PathVariable("productId") Integer productId,
            @RequestPart("product") ProductUpdateDto dto,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest request
    ) {
        try {
            String userId = (String) request.getAttribute("userId");

            // 이미지가 따로 넘어왔으면 DTO에 넣어줌
            if (dto.getMainImage() == null && mainImage != null) {
                dto.setMainImage(mainImage);
            }

            productService.updateProduct(userId, productId, dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @GetMapping("/dashboard/sold-out")
    public ResponseEntity<LowStockProductSummaryDto> getLowStockProducts(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        log.info("📦 품절 임박 상품 요청 by userId: {}", userId);

        LowStockProductSummaryDto result = productService.getLowStockProducts(userId);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/dashboard/popular")
    public ResponseEntity<List<PopularProductDto>> getPopularProducts(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        log.info("🔥 인기 상품 요청 by userId: {}", userId);

        List<PopularProductDto> result = productService.getPopularProducts(userId);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/dashboard/product-status")
    public ResponseEntity<ProductStatusDto> getProductStatus(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        log.info("📦 상품 상태 요청 by userId: {}", userId);

        ProductStatusDto result = productService.getProductStatus(userId);
        return ResponseEntity.ok(result);
    }
}

