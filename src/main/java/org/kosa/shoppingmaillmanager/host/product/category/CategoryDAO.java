package org.kosa.shoppingmaillmanager.host.product.category;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CategoryDAO {
    List<CategoryTreeDTO> selectCategoryTree();
    // 대분류만 조회 (필요시)
    List<CategoryTreeDTO> selectMainCategories();
}
