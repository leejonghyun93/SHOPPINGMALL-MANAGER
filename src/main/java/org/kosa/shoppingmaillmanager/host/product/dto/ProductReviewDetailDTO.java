package org.kosa.shoppingmaillmanager.host.product.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductReviewDetailDTO {
    private String reviewId;
    private String userId;          // ✅ 작성자 ID
    private String productName;     // ✅ 상품명
    private String content;
    private int rating;
    private String displayYn;
    private int helpfulCount;
    private LocalDateTime createdDate;
    private List<String> imageUrls;
}
