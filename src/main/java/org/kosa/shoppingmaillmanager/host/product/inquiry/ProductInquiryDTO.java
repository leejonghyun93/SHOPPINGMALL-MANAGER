package org.kosa.shoppingmaillmanager.host.product.inquiry;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ProductInquiryDTO {
 private String qnaId;
 private int productId;
 private String productName;
 private String userId;
 private String title;
 private String content;
 private String qnaStatus;
 private String isSecret;
 private int viewCount;
 private LocalDateTime answerDate;
 private String answerUserId;
 private LocalDateTime createdDate;
 private LocalDateTime updatedDate;

 // getters and setters
}
