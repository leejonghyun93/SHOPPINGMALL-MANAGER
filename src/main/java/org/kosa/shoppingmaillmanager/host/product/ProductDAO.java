package org.kosa.shoppingmaillmanager.host.product;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductDTO;

@Mapper
public interface ProductDAO {
    List<ProductDTO> selectProductListWithCategory(Map<String, Object> params);
}
