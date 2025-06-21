package org.kosa.shoppingmaillmanager.host.product;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductDAO {

    // 필요시 상품 목록 + 카테고리명 조회 메서드 선언 가능
    List<ProductDTO> selectProductListWithCategory(Map<String, Object> params);
}
