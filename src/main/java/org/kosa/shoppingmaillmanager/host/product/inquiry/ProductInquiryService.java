package org.kosa.shoppingmaillmanager.host.product.inquiry;

import java.util.List;

import org.kosa.shoppingmaillmanager.host.product.HostDAO;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductInquiryAnswerDTO;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductInquiryDTO;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductInquiryDetailDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductInquiryService {

    private final ProductInquiryDAO productInquiryDAO;
    private final HostDAO hostDAO;

    // 1. 상품별 문의 목록
    public List<ProductInquiryDTO> getInquiriesByProductId(String userId, int productId) {
        String hostId = validateHostOwnership(userId, productId);
        return productInquiryDAO.selectInquiriesByProductId(hostId, productId);
    }

    // 2. 문의 상세 + 답변 리스트
    public ProductInquiryDetailDTO getInquiryDetailWithAnswers(String userId, int productId, String qnaId) {
        String hostId = validateHostOwnership(userId, productId);
        ProductInquiryDetailDTO detail = productInquiryDAO.selectInquiryDetail(hostId, productId, qnaId);
        if (detail != null) {
            List<ProductInquiryAnswerDTO> answers = productInquiryDAO.selectAnswersByQnaId(qnaId);
            detail.setAnswers(answers);
        }
        return detail;
    }

    // 3. 답변 등록
    @Transactional
    public void createAnswer(String userId, int productId, String qnaId, ProductInquiryAnswerDTO answerDTO) {
        validateHostOwnership(userId, productId);
        answerDTO.setQnaId(qnaId);
        answerDTO.setUserId(userId);
        productInquiryDAO.insertAnswer(qnaId, answerDTO);
        productInquiryDAO.updateQnaStatus(qnaId, "ANSWERED");
    }

    // 4. 답변 수정
    public void updateAnswer(String userId, int productId, String qnaId, String answerId, ProductInquiryAnswerDTO answerDTO) {
        validateHostOwnership(userId, productId);
        answerDTO.setAnswerId(answerId);
        answerDTO.setQnaId(qnaId);
        answerDTO.setUserId(userId);
        productInquiryDAO.updateAnswer(answerId, answerDTO);
    }

    // 5. 답변 삭제
    @Transactional
    public void deleteAnswer(String userId, int productId, String qnaId, String answerId) {
        validateHostOwnership(userId, productId);
        productInquiryDAO.deleteAnswer(answerId);
        List<ProductInquiryAnswerDTO> remain = productInquiryDAO.selectAnswersByQnaId(qnaId);
        if (remain == null || remain.isEmpty()) {
            productInquiryDAO.updateQnaStatus(qnaId, "WAITING");
        }
    }

    // 판매자 검증
    private String validateHostOwnership(String userId, int productId) {
        String hostId = hostDAO.findHostIdByUserId(userId);
        String productHostId = productInquiryDAO.selectHostIdByProductId(productId);
        if (hostId == null || !hostId.equals(productHostId)) {
            throw new SecurityException("해당 상품에 접근 권한이 없습니다.");
        }
        return hostId;
    }
    
 // 6. 판매자 전체 상품에 대한 문의 목록 조회
    public List<ProductInquiryDTO> getInquiriesBySeller(String userId, String keyword) {
        String hostId = hostDAO.findHostIdByUserId(userId);
        return productInquiryDAO.selectInquiriesByHostId(hostId, keyword);
    }
    
 // 7. 미답변 문의 최신순 상위 5개 (대시보드용)
    public List<ProductInquiryDTO> getRecentUnansweredInquiries(String userId) {
        String hostId = hostDAO.findHostIdByUserId(userId);
        return productInquiryDAO.selectRecentUnansweredInquiries(hostId);
    }
}
