package org.kosa.shoppingmaillmanager.host.product;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductOptionDto;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductSimpleDTO;
import org.kosa.shoppingmaillmanager.host.product.entity.Product;
import org.kosa.shoppingmaillmanager.host.product.entity.ProductOption;

@Mapper
public interface ProductDAO {

    // 목록/카운트/상태 관련
    List<ProductSimpleDTO> selectProductList(Map<String, Object> paramMap);
    long countProductList(Map<String, Object> paramMap);
    List<Map<String, Object>> countProductStatusMapRaw(@Param("hostId") String hostId);

    // 진열 여부 변경
    void updateDisplayYn(@Param("productId") Integer productId, @Param("displayYn") String displayYn);

    // 상품 조회
    ProductSimpleDTO selectProductById(@Param("hostId") String hostId,
                                       @Param("productId") Integer productId);

    // 상품 필드 개별 수정
    void updateProductField(@Param("hostId") String hostId,
                             @Param("productId") Integer productId,
                             @Param("updates") Map<String, Object> updates);

    // 상품 등록
    void insertProduct(Product product);  // productId 자동 생성
    void insertProductOption(ProductOption option);

    // 옵션 조회
    List<ProductOptionDto> findOptionsByProductId(@Param("productId") Integer productId);

    // 단건 조회
    Product findById(Integer productId); // 이건 기존에 있음

    // ✅ 상품 수정용: 전체 필드 업데이트
    void updateProduct(Product product);

    // ✅ 해당 상품의 옵션 전체 삭제
    void deleteProductOptionsByProductId(Integer productId);

    // ✅ 상품 수정 시 사용될 단순 조회 (권한 확인용)
    Product findProductById(Integer productId);
}
