package org.kosa.shoppingmaillmanager.host.product.inquiry;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductInquiryAnswerService {

    private final ProductInquiryAnswerDAO answerDAO;
    private final ProductInquiryDAO productInquiryDAO;

    public ProductInquiryAnswerService(ProductInquiryAnswerDAO answerDAO,
                                       ProductInquiryDAO productInquiryDAO) {
        this.answerDAO = answerDAO;
        this.productInquiryDAO = productInquiryDAO;
    }

    // 답변 등록
    @Transactional
    public void createAnswer(String qnaId, ProductInquiryAnswerDTO answerDTO) {

        answerDAO.insertAnswer(qnaId, answerDTO);
        // 문의 상태 ANSWERED로 변경
        productInquiryDAO.updateQnaStatus(qnaId, "ANSWERED");
    }

    // 답변 수정
    public void updateAnswer(String qnaId, String answerId, ProductInquiryAnswerDTO answerDTO) {
        answerDTO.setAnswerId(answerId);
        answerDTO.setQnaId(qnaId);
        answerDAO.updateAnswer(answerId, answerDTO);
    }

    // 답변 삭제
    @Transactional
    public void deleteAnswer(String qnaId, String answerId) {
        answerDAO.deleteAnswer(answerId);
        // 답변이 남아있는지 확인 후 문의 상태 WAITING으로 변경
        List<ProductInquiryAnswerDTO> remain = answerDAO.selectAnswersByQnaId(qnaId);
        if (remain == null || remain.isEmpty()) {
            productInquiryDAO.updateQnaStatus(qnaId, "WAITING");
        }
    }
}
