package org.kosa.shoppingmaillmanager.host.product.inquiry;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class ProductInquiryDetailDTO {
    private String qnaId;
    private int productId;
    private String productName;
    private String userId;
    private String title;
    private String content;
    private String qnaStatus;
    private String isSecret;
    private int viewCount;
    private String answerUserId;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private List<ProductInquiryAnswerDTO> answers; // 답변 리스트
}
