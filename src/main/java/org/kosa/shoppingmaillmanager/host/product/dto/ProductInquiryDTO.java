package org.kosa.shoppingmaillmanager.host.product.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductInquiryDTO {
    private String qnaId;
    private String productId;
    private String productName;

    private String userId;
    private String title;
    private String content;

    private String qnaStatus;     // WAITING, ANSWERED 등
    private String isSecret;      // 'Y' 또는 'N'
    private Integer viewCount;

    private String answerUserId;
    private LocalDateTime answerDate;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
