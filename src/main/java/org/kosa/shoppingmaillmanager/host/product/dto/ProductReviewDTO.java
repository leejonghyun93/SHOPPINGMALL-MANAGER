package org.kosa.shoppingmaillmanager.host.product.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductReviewDTO {
    private String reviewId;
    private String productId;
    private String authorName;
    private String productName;
    private String userId;        // 추가
    private String content;
    private int rating;
    private int helpfulCount;     // 추가
    private String isPhoto;       // 추가
    private LocalDateTime createdDate;
    private String displayYn;
}

