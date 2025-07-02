package org.kosa.shoppingmaillmanager.host.product.review;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductReviewImageDAO {
    List<String> selectImageUrlsByReviewId(@Param("reviewId") String reviewId);
}
