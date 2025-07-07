package org.kosa.shoppingmaillmanager.host.product.review;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductReviewDTO;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductReviewDetailDTO;

import java.util.List;

@Mapper
public interface ProductReviewDAO {
	List<ProductReviewDTO> selectReviewsWithProductNameByProductId(@Param("productId") int productId);

	ProductReviewDetailDTO selectReviewDetailById(String reviewId);

    int updateReviewDisplayYn(@Param("reviewId") String reviewId, @Param("displayYn") String displayYn);

    String selectHostIdByProductId(@Param("productId") int productId);

    List<ProductReviewDTO> selectReviewsByHostId(@Param("hostId") String hostId, @Param("keyword") String keyword);
}
