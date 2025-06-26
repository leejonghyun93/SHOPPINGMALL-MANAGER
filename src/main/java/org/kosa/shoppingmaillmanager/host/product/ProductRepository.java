package org.kosa.shoppingmaillmanager.host.product;

import org.kosa.shoppingmaillmanager.host.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    // 상태별 개수 (상태 카운트만 필요)
    long countByProductStatus(String productStatus);

    // 진열여부 업데이트 (진열여부 토글만 필요)
    @Modifying
    @Query("UPDATE Product p SET p.displayYn = :displayYn WHERE p.productId = :productId")
    int updateDisplayYn(@Param("productId") Integer productId, @Param("displayYn") String displayYn);
}
