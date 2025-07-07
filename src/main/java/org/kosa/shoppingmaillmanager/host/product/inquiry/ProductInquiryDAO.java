package org.kosa.shoppingmaillmanager.host.product.inquiry;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductInquiryAnswerDTO;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductInquiryDTO;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductInquiryDetailDTO;

@Mapper
public interface ProductInquiryDAO {
    // 상품별 문의 목록 (호스트 검증용)
    List<ProductInquiryDTO> selectInquiriesByProductId(
        @Param("hostId") String hostId,
        @Param("productId") int productId
    );

    // 문의 상세 (상품명 포함, 호스트 검증용)
    ProductInquiryDetailDTO selectInquiryDetail(
        @Param("hostId") String hostId,
        @Param("productId") int productId,
        @Param("qnaId") String qnaId
    );

    // 문의 상태 변경 (답변 등록/삭제 시)
    int updateQnaStatus(
        @Param("qnaId") String qnaId,
        @Param("qnaStatus") String qnaStatus
    );
    
 // 상품 ID로 host_id 조회 (판매자 검증용)
    String selectHostIdByProductId(@Param("productId") int productId);
    
 // 답변 리스트
    List<ProductInquiryAnswerDTO> selectAnswersByQnaId(@Param("qnaId") String qnaId);

    // 답변 등록
    int insertAnswer(@Param("qnaId") String qnaId, @Param("answer") ProductInquiryAnswerDTO answer);

    // 답변 수정
    int updateAnswer(@Param("answerId") String answerId, @Param("answer") ProductInquiryAnswerDTO answer);

    // 답변 삭제
    int deleteAnswer(@Param("answerId") String answerId);
    
    List<ProductInquiryDTO> selectInquiriesByHostId(@Param("hostId") String hostId, @Param("keyword") String keyword);

 // 7. 미답변 문의 최근 5건
    List<ProductInquiryDTO> selectRecentUnansweredInquiries(@Param("hostId") String hostId);
}
