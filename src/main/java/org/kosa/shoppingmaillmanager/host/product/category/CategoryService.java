package org.kosa.shoppingmaillmanager.host.product.category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kosa.shoppingmaillmanager.host.product.dto.CategoryTreeDTO;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryDAO categoryDAO;

    /**
     * 전체 카테고리를 트리 구조로 반환 (대분류 → 하위 포함)
     * → /categories/tree
     */
    public List<CategoryTreeDTO> getCategoryTree() {
        List<CategoryTreeDTO> flatList = categoryDAO.selectCategoryTree();
        return buildCategoryTree(flatList);
    }

    /**
     * 대분류 카테고리만 반환 (level = 1)
     * → /categories/main
     */
    public List<CategoryTreeDTO> getMainCategories() {
        return categoryDAO.selectMainCategories();
    }

    /**
     * 전체 카테고리를 flat list로 반환 (1차원 배열)
     * → /categories/flat
     */
    public List<CategoryTreeDTO> getFlatCategoryList() {
        return categoryDAO.selectCategoryTree();
    }

    /**
     * 특정 카테고리 ID를 기준으로 하위 카테고리 포함 전체 ID 리스트 반환
     * (예: 대분류 1 → 중/소분류까지 포함)
     */
    public List<Long> getCategoryAndAllChildIds(Long mainCategoryId) {
        List<CategoryTreeDTO> flatList = categoryDAO.selectCategoryTree();

        Map<Long, CategoryTreeDTO> map = new HashMap<>();
        for (CategoryTreeDTO dto : flatList) {
            map.put(dto.getCategoryId(), dto); // ✅ dto.getCategoryId()가 Long
        }

        List<Long> result = new ArrayList<>();
        collectChildCategoryIds(mainCategoryId, map, result);
        return result;
    }

    /**
     * 재귀적으로 하위 카테고리 ID 수집
     */
    private void collectChildCategoryIds(Long parentId, Map<Long, CategoryTreeDTO> map, List<Long> result) {
        if (!result.contains(parentId)) {
            result.add(parentId);
        }
        for (CategoryTreeDTO dto : map.values()) {
            if (parentId.equals(dto.getParentCategoryId())) {
                collectChildCategoryIds(dto.getCategoryId(), map, result);
            }
        }
    }

    /**
     * flat list → 트리 구조로 변환
     */
    private List<CategoryTreeDTO> buildCategoryTree(List<CategoryTreeDTO> flatList) {
        Map<Long, CategoryTreeDTO> map = new HashMap<>();
        List<CategoryTreeDTO> rootList = new ArrayList<>();

        for (CategoryTreeDTO dto : flatList) {
            dto.setChildren(new ArrayList<>());
            map.put(dto.getCategoryId(), dto);
        }

        for (CategoryTreeDTO dto : flatList) {
            if (dto.getParentCategoryId() == null) {
                rootList.add(dto);
            } else {
                CategoryTreeDTO parent = map.get(dto.getParentCategoryId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }
        }

        return rootList;
    }
}
