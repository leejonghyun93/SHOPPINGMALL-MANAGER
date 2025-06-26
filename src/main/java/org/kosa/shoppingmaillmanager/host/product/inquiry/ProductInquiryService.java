package org.kosa.shoppingmaillmanager.host.product.inquiry;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ProductInquiryService {

    private final ProductInquiryDAO productInquiryDAO;
    private final ProductInquiryAnswerDAO answerDAO;

    public ProductInquiryService(ProductInquiryDAO productInquiryDAO,
                                 ProductInquiryAnswerDAO answerDAO) {
        this.productInquiryDAO = productInquiryDAO;
        this.answerDAO = answerDAO;
    }

    // 상품별 문의 목록
    public List<ProductInquiryDTO> getInquiriesByProductId(int productId) {
        return productInquiryDAO.selectInquiriesByProductId(productId);
    }

    // 문의 상세 + 답변 리스트
    public ProductInquiryDetailDTO getInquiryDetailWithAnswers(int productId, String qnaId) {
        ProductInquiryDetailDTO detail = productInquiryDAO.selectInquiryDetail(productId, qnaId);
        if (detail != null) {
            List<ProductInquiryAnswerDTO> answers = answerDAO.selectAnswersByQnaId(qnaId);
            detail.setAnswers(answers);
        }
        return detail;
    }
}
