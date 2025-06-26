package org.kosa.shoppingmaillmanager.host.product.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductReviewService {

    private final ProductReviewDAO reviewDAO;

    // 상품코드별 후기 전체(공개/비공개 모두), 최신순
    public List<ProductReviewDTO> getReviewsByProductId(String productId) {
        return reviewDAO.selectReviewsWithProductNameByProductId(productId);
    }

    // 단일 후기 상세
    public ProductReviewDTO getReviewById(String reviewId) {
        return reviewDAO.selectReviewById(reviewId);
    }

    @Transactional
    public void updateReviewDisplayYn(String reviewId, String displayYn) {
        int updated = reviewDAO.updateReviewDisplayYn(reviewId, displayYn);
        if (updated == 0) {
            throw new EntityNotFoundException("후기를 찾을 수 없습니다. reviewId=" + reviewId);
        }
    }
}