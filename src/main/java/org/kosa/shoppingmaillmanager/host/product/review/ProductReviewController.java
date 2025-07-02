package org.kosa.shoppingmaillmanager.host.product.review;

import java.util.List;

import org.kosa.shoppingmaillmanager.host.product.dto.ProductReviewDTO;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductReviewDetailDTO;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductReviewDisplayDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/products/{productId}/reviews")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService productReviewService;

    // 1. 후기 상세 조회 (판매자는 공개 여부와 관계없이 조회 가능)
    @GetMapping("/{reviewId}")
    public ResponseEntity<ProductReviewDetailDTO> getReviewDetail(
            @PathVariable int productId,
            @PathVariable String reviewId,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        ProductReviewDetailDTO detail = productReviewService.getReviewDetail(userId, productId, reviewId);
        if (detail == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(detail);
    }

    // 2. 공개 여부 변경 (displayYn)
    @PutMapping("/{reviewId}/display-yn")
    public ResponseEntity<Void> updateDisplayYn(
            @PathVariable int productId,
            @PathVariable String reviewId,
            @RequestBody ProductReviewDisplayDTO dto,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        productReviewService.updateDisplayYn(userId, productId, reviewId, dto.getDisplayYn());
        return ResponseEntity.ok().build();
    }

    // 3. 후기 목록 조회 (판매자는 공개/비공개 모두 조회 가능)
    @GetMapping
    public ResponseEntity<List<ProductReviewDTO>> getReviewList(
            @PathVariable int productId,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        List<ProductReviewDTO> reviews = productReviewService.getReviewList(userId, productId);
        return ResponseEntity.ok(reviews);
    }
}
