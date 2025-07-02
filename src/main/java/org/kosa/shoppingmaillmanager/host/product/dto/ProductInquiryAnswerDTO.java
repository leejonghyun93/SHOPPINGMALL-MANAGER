package org.kosa.shoppingmaillmanager.host.product.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductInquiryAnswerDTO {
    private String answerId;
    private String qnaId;
    private String userId; // 답변 작성자
    private String content;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
