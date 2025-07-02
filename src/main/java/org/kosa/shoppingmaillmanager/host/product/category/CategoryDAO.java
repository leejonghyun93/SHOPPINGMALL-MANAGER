package org.kosa.shoppingmaillmanager.host.product.category;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.kosa.shoppingmaillmanager.host.product.dto.CategoryTreeDTO;

@Mapper
public interface CategoryDAO {
    List<CategoryTreeDTO> selectCategoryTree();       // 전체 카테고리 (flat)
    List<CategoryTreeDTO> selectMainCategories();     // 대분류만 (level = 1)
}