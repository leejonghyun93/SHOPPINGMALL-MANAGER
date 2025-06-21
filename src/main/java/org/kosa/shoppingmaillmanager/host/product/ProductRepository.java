package org.kosa.shoppingmaillmanager.host.product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, String> {
    // 상태별 필터링
    Page<Product> findByProductStatus(String productStatus, Pageable pageable);
    long countByProductStatus(String productStatus);

    // 추가: categoryId 리스트로 필터링하는 메서드
    Page<Product> findByCategoryIdIn(List<String> categoryIds, Pageable pageable);
    Page<Product> findByCategoryIdInAndProductStatus(List<String> categoryIds, String productStatus, Pageable pageable);
 // 상품명 또는 상품코드에 keyword가 포함된 상품 조회
 // 상품명 또는 상품ID에 keyword가 포함된 상품 조회
    @Query("SELECT p FROM Product p WHERE p.name LIKE CONCAT('%', :keyword, '%') OR p.productId LIKE CONCAT('%', :keyword, '%')")
    Page<Product> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE (p.name LIKE CONCAT('%', :keyword, '%') OR p.productId LIKE CONCAT('%', :keyword, '%')) AND p.productStatus = :status")
    Page<Product> findByProductStatusAndKeyword(@Param("status") String status, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE (p.name LIKE CONCAT('%', :keyword, '%') OR p.productId LIKE CONCAT('%', :keyword, '%')) AND p.categoryId IN :categoryIds")
    Page<Product> findByCategoryIdInAndKeyword(@Param("categoryIds") List<String> categoryIds, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE (p.name LIKE CONCAT('%', :keyword, '%') OR p.productId LIKE CONCAT('%', :keyword, '%')) AND p.categoryId IN :categoryIds AND p.productStatus = :status")
    Page<Product> findByCategoryIdInAndProductStatusAndKeyword(@Param("categoryIds") List<String> categoryIds, @Param("status") String status, @Param("keyword") String keyword, Pageable pageable);
    
    @Modifying
    @Query("UPDATE Product p SET p.displayYn = :displayYn WHERE p.productId = :productId")
    int updateDisplayYn(@Param("productId") String productId, @Param("displayYn") String displayYn);
}
