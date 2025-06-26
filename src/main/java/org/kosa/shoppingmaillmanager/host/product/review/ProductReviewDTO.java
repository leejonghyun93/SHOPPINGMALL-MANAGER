package org.kosa.shoppingmaillmanager.host.product.review;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductReviewDTO {
    private String reviewId;
    private String productId;
    private String orderId;
    private String userId;
    private String content;
    private Integer rating;
    private Integer helpfulCount;
    private String isPhoto;
    private String isVerified;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String displayYn;
    private String productName;

    // 엔티티 → DTO 변환 메서드
    public static ProductReviewDTO fromEntity(ProductReview review) {
        if (review == null) return null;
        ProductReviewDTO dto = new ProductReviewDTO();
        dto.setReviewId(review.getReviewId());
        dto.setProductId(review.getProductId());
        dto.setOrderId(review.getOrderId());
        dto.setUserId(review.getUserId());
        dto.setContent(review.getContent());
        dto.setRating(review.getRating());
        dto.setHelpfulCount(review.getHelpfulCount());
        dto.setIsPhoto(review.getIsPhoto());
        dto.setIsVerified(review.getIsVerified());
        dto.setCreatedDate(review.getCreatedDate());
        dto.setUpdatedDate(review.getUpdatedDate());
        dto.setDisplayYn(review.getDisplayYn());
        return dto;
    }
}
