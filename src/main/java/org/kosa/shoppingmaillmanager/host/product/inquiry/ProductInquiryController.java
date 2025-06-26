package org.kosa.shoppingmaillmanager.host.product.inquiry;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products/{productId}/inquiries")
public class ProductInquiryController {

    private final ProductInquiryService productInquiryService;
    private final ProductInquiryAnswerService answerService;

    public ProductInquiryController(ProductInquiryService productInquiryService,
                                    ProductInquiryAnswerService answerService) {
        this.productInquiryService = productInquiryService;
        this.answerService = answerService;
    }

    // 1. 상품별 문의 목록
    @GetMapping
    public ResponseEntity<List<ProductInquiryDTO>> getInquiriesByProductId(@PathVariable int productId) {
        List<ProductInquiryDTO> inquiries = productInquiryService.getInquiriesByProductId(productId);
        return ResponseEntity.ok(inquiries);
    }

    // 2. 문의 상세 + 답변 리스트
    @GetMapping("/{qnaId}")
    public ResponseEntity<ProductInquiryDetailDTO> getInquiryDetail(
            @PathVariable int productId,
            @PathVariable String qnaId) {
        ProductInquiryDetailDTO detail = productInquiryService.getInquiryDetailWithAnswers(productId, qnaId);
        if (detail == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(detail);
    }

    // 3. 답변 등록
    @PostMapping("/{qnaId}/answers")
    public ResponseEntity<Void> createAnswer(
            @PathVariable int productId,
            @PathVariable String qnaId,
            @RequestBody ProductInquiryAnswerDTO answerDTO) {
        answerService.createAnswer(qnaId, answerDTO);
        return ResponseEntity.ok().build();
    }

    // 4. 답변 수정
    @PutMapping("/{qnaId}/answers/{answerId}")
    public ResponseEntity<Void> updateAnswer(
            @PathVariable int productId,
            @PathVariable String qnaId,
            @PathVariable String answerId,
            @RequestBody ProductInquiryAnswerDTO answerDTO) {
        answerService.updateAnswer(qnaId, answerId, answerDTO);
        return ResponseEntity.ok().build();
    }

    // 5. 답변 삭제
    @DeleteMapping("/{qnaId}/answers/{answerId}")
    public ResponseEntity<Void> deleteAnswer(
            @PathVariable int productId,
            @PathVariable String qnaId,
            @PathVariable String answerId) {
        answerService.deleteAnswer(qnaId, answerId);
        return ResponseEntity.ok().build();
    }
}
