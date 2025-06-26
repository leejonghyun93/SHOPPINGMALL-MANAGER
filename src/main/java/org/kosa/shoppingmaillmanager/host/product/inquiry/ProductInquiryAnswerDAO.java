package org.kosa.shoppingmaillmanager.host.product.inquiry;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductInquiryAnswerDAO {
    // 특정 문의(QNA_ID)의 답변 리스트
    List<ProductInquiryAnswerDTO> selectAnswersByQnaId(@Param("qnaId") String qnaId);

    // 답변 등록
    int insertAnswer(@Param("qnaId") String qnaId, @Param("answer") ProductInquiryAnswerDTO answer);

    // 답변 수정
    int updateAnswer(@Param("answerId") String answerId, @Param("answer") ProductInquiryAnswerDTO answer);

    // 답변 삭제
    int deleteAnswer(@Param("answerId") String answerId);
}
