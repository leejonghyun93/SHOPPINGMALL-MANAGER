package org.kosa.shoppingmaillmanager.host.product.review;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/products/{productId}/reviews")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService reviewService;

    @GetMapping
    public List<ProductReviewDTO> getReviews(@PathVariable String productId) {
        return reviewService.getReviewsByProductId(productId);
    }

    @GetMapping("/{reviewId}")
    public ProductReviewDTO getReview(@PathVariable String productId, @PathVariable String reviewId) {
        // 필요 시 productId와 reviewId 일치 여부 검증 가능
        return reviewService.getReviewById(reviewId);
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<Void> updateReviewDisplayYn(
        @PathVariable String productId,
        @PathVariable String reviewId,
        @RequestBody ProductReviewDTO dto) {
        reviewService.updateReviewDisplayYn(reviewId, dto.getDisplayYn());
        return ResponseEntity.noContent().build();
    }
}
