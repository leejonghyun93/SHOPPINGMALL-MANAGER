package org.kosa.shoppingmaillmanager.host.product.review;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_product_review")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReview {

    @Id
    @Column(name = "REVIEW_ID", length = 50)
    private String reviewId;

    @Column(name = "PRODUCT_ID", length = 50, nullable = false)
    private String productId;

    @Column(name = "ORDER_ID", length = 50)
    private String orderId;

    @Column(name = "USER_ID", length = 50, nullable = false)
    private String userId;

    @Column(name = "CONTENT", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "RATING", nullable = false)
    private Integer rating;

    @Column(name = "HELPFUL_COUNT", nullable = false)
    private Integer helpfulCount = 0;

    @Column(name = "IS_PHOTO", length = 1)
    private String isPhoto = "N";

    @Column(name = "IS_VERIFIED", length = 1)
    private String isVerified = "N";

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;

    @Column(name = "display_yn", length = 1)
    private String displayYn = "Y";
}
