package org.kosa.shoppingmaillmanager.host.product.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductInquiryDetailDTO {
    private String qnaId;
    private String productId;
    private String productName;

    private String userId;
    private String title;
    private String content;

    private String qnaStatus;
    private String isSecret;
    private Integer viewCount;

    private String answerUserId;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    private List<ProductInquiryAnswerDTO> answers;
}
