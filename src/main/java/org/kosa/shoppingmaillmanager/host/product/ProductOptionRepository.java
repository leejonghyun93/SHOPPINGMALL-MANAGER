package org.kosa.shoppingmaillmanager.host.product;

import org.kosa.shoppingmaillmanager.host.product.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Integer> {
    // 필요시 옵션 관련 커스텀 쿼리 메서드 추가
}