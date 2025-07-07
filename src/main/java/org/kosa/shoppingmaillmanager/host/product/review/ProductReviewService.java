package org.kosa.shoppingmaillmanager.host.product.review;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.kosa.shoppingmaillmanager.host.product.HostDAO;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductReviewDTO;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductReviewDetailDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductReviewService {

    private final ProductReviewDAO productReviewDAO;
    private final ProductReviewImageDAO productReviewImageDAO;
    private final HostDAO hostDAO;

    // 후기 상세 조회
    public ProductReviewDetailDTO getReviewDetail(String userId, int productId, String reviewId) {
        validateHostOwnership(userId, productId);
        ProductReviewDetailDTO detail = productReviewDAO.selectReviewDetailById(reviewId);
        if (detail != null) {
            List<String> imageUrls = productReviewImageDAO.selectImageUrlsByReviewId(reviewId);
            detail.setImageUrls(imageUrls);
        }
        return detail;
    }

    // 공개 여부 수정
    @Transactional
    public void updateDisplayYn(String userId, int productId, String reviewId, String displayYn) {
        validateHostOwnership(userId, productId);
        int updated = productReviewDAO.updateReviewDisplayYn(reviewId, displayYn);
        if (updated == 0) {
            throw new EntityNotFoundException("해당 후기를 찾을 수 없습니다. reviewId=" + reviewId);
        }
    }

    // 후기 목록 조회
    public List<ProductReviewDTO> getReviewList(String userId, int productId) {
        validateHostOwnership(userId, productId);
        return productReviewDAO.selectReviewsWithProductNameByProductId(productId);
    }

    // 판매자 권한 확인
    private void validateHostOwnership(String userId, int productId) {
        String hostId = hostDAO.findHostIdByUserId(userId);
        String productHostId = productReviewDAO.selectHostIdByProductId(productId);
        if (hostId == null || !hostId.equals(productHostId)) {
            throw new SecurityException("해당 상품에 대한 접근 권한이 없습니다.");
        }
    }
    
 // 판매자가 등록한 전체 상품의 후기 목록 조회 (keyword는 상품명 or 내용 포함 검색)
    public List<ProductReviewDTO> getReviewListByHost(String userId, String keyword) {
        String hostId = hostDAO.findHostIdByUserId(userId);
        if (hostId == null) {
            throw new SecurityException("판매자 권한이 없습니다.");
        }
        return productReviewDAO.selectReviewsByHostId(hostId, keyword);
    }
}
