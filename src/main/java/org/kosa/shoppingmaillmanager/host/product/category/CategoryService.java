package org.kosa.shoppingmaillmanager.host.product.category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryDAO categoryDAO;

    // flat list를 트리로 변환
    public List<CategoryTreeDTO> buildCategoryTree(List<CategoryTreeDTO> flatList) {
        Map<Integer, CategoryTreeDTO> map = new HashMap<>();
        List<CategoryTreeDTO> roots = new ArrayList<>();

        for (CategoryTreeDTO dto : flatList) {
            dto.setChildren(new ArrayList<>());
            map.put(dto.getCategoryId(), dto);
        }
        for (CategoryTreeDTO dto : flatList) {
            if (dto.getParentCategoryId() == null) {
                roots.add(dto);
            } else {
                CategoryTreeDTO parent = map.get(dto.getParentCategoryId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }
        }
        return roots;
    }

    // 실제로 사용할 트리 반환 메서드
    public List<CategoryTreeDTO> getCategoryTree() {
        List<CategoryTreeDTO> flatList = categoryDAO.selectCategoryTree(); // DAO에서 flat list 조회
        return buildCategoryTree(flatList); // 트리로 변환해서 반환
    }

    public List<CategoryTreeDTO> getMainCategories() {
        return categoryDAO.selectMainCategories();
    }

    // 특정 대분류 ID와 그 하위 모든 카테고리 ID 리스트 반환
    public List<Integer> getCategoryAndAllChildIds(Integer mainCategoryId) {
        List<CategoryTreeDTO> flatList = categoryDAO.selectCategoryTree();

        Map<Integer, CategoryTreeDTO> map = new HashMap<>();
        for (CategoryTreeDTO dto : flatList) {
            map.put(dto.getCategoryId(), dto);
        }

        List<Integer> result = new ArrayList<>();
        collectChildCategoryIds(mainCategoryId, map, result);

        return result;
    }

    // 재귀적으로 하위 카테고리 ID 모두 수집하는 헬퍼 메서드
    private void collectChildCategoryIds(Integer parentId, Map<Integer, CategoryTreeDTO> map, List<Integer> result) {
        if (!result.contains(parentId)) {
            result.add(parentId);
        }
        for (CategoryTreeDTO dto : map.values()) {
            if (parentId.equals(dto.getParentCategoryId())) {
                collectChildCategoryIds(dto.getCategoryId(), map, result);
            }
        }
    }
}
