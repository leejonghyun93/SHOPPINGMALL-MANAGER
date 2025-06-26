package org.kosa.shoppingmaillmanager.host.product.inquiry;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductInquiryDAO {
    // 상품별 문의 목록
    List<ProductInquiryDTO> selectInquiriesByProductId(@Param("productId") int productId);

    // 문의 상세 (상품명 포함)
    ProductInquiryDetailDTO selectInquiryDetail(@Param("productId") int productId, @Param("qnaId") String qnaId);

    // 문의 상태 변경 (답변 등록/삭제 시)
    int updateQnaStatus(@Param("qnaId") String qnaId, @Param("qnaStatus") String qnaStatus);
}
