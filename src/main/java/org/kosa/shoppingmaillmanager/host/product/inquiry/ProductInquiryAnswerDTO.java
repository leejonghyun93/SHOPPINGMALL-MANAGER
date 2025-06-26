package org.kosa.shoppingmaillmanager.host.product.inquiry;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ProductInquiryAnswerDTO {
    private String answerId;
    private String qnaId;
    private String userId;
    private String content;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
