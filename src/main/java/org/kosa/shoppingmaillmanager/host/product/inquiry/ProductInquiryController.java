package org.kosa.shoppingmaillmanager.host.product.inquiry;

import java.util.List;

import org.kosa.shoppingmaillmanager.host.product.dto.ProductInquiryAnswerDTO;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductInquiryDTO;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductInquiryDetailDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ProductInquiryController {

    private final ProductInquiryService productInquiryService;

    // 1. 상품별 문의 목록
    @GetMapping("/products/{productId}/inquiries")
    public ResponseEntity<List<ProductInquiryDTO>> getInquiriesByProductId(
            @PathVariable int productId,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        List<ProductInquiryDTO> inquiries = productInquiryService.getInquiriesByProductId(userId, productId);
        return ResponseEntity.ok(inquiries);
    }

    // 2. 문의 상세 + 답변 리스트
    @GetMapping("/products/{productId}/inquiries/{qnaId}")
    public ResponseEntity<ProductInquiryDetailDTO> getInquiryDetail(
            @PathVariable int productId,
            @PathVariable String qnaId,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        ProductInquiryDetailDTO detail = productInquiryService.getInquiryDetailWithAnswers(userId, productId, qnaId);
        if (detail == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(detail);
    }

    // 3. 답변 등록
    @PostMapping("/products/{productId}/inquiries/{qnaId}/answers")
    public ResponseEntity<Void> createAnswer(
            @PathVariable int productId,
            @PathVariable String qnaId,
            @RequestBody ProductInquiryAnswerDTO answerDTO,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        productInquiryService.createAnswer(userId, productId, qnaId, answerDTO);
        return ResponseEntity.ok().build();
    }

    // 4. 답변 수정
    @PutMapping("/products/{productId}/inquiries/{qnaId}/answers/{answerId}")
    public ResponseEntity<Void> updateAnswer(
            @PathVariable int productId,
            @PathVariable String qnaId,
            @PathVariable String answerId,
            @RequestBody ProductInquiryAnswerDTO answerDTO,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        productInquiryService.updateAnswer(userId, productId, qnaId, answerId, answerDTO);
        return ResponseEntity.ok().build();
    }

    // 5. 답변 삭제
    @DeleteMapping("/products/{productId}/inquiries/{qnaId}/answers/{answerId}")
    public ResponseEntity<Void> deleteAnswer(
            @PathVariable int productId,
            @PathVariable String qnaId,
            @PathVariable String answerId,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        productInquiryService.deleteAnswer(userId, productId, qnaId, answerId);
        return ResponseEntity.ok().build();
    }
    
 // 6. 판매자 전체 상품 문의 목록 조회 (상품명, 제목, 내용 검색)
    @GetMapping("/seller/inquiries")
    public ResponseEntity<List<ProductInquiryDTO>> getInquiriesBySeller(
            HttpServletRequest request,
            @RequestParam(value = "keyword", required = false) String keyword) {

        String userId = (String) request.getAttribute("userId");
        List<ProductInquiryDTO> inquiries = productInquiryService.getInquiriesBySeller(userId, keyword);
        return ResponseEntity.ok(inquiries);
    }
    
    @GetMapping("/seller/recentInquiries")
    public ResponseEntity<List<ProductInquiryDTO>> getRecentUnansweredInquiries(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        List<ProductInquiryDTO> recentInquiries = productInquiryService.getRecentUnansweredInquiries(userId);
        return ResponseEntity.ok(recentInquiries);
    }
}