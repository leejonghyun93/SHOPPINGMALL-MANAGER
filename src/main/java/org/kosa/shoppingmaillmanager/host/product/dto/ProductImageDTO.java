package org.kosa.shoppingmaillmanager.host.product.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductImageDTO {
    private String imageId;
    private Integer productId;
    private String imageUrl;
    private String fileName;
    private Long fileSize;
    private String storageType;
    private Integer imageSeq;
    private String isMainImage;
    private String imageAlt;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
