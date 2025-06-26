package org.kosa.shoppingmaillmanager.host.product.review;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductReviewDAO {
    List<ProductReviewDTO> selectReviewsWithProductNameByProductId(String productId);
    ProductReviewDTO selectReviewById(String reviewId);
    int updateReviewDisplayYn(@Param("reviewId") String reviewId, @Param("displayYn") String displayYn);
}
