package org.kosa.shoppingmaillmanager.host.product.category;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryTreeDTO {
    private Integer categoryId;
    private String categoryName;
    private Integer categoryLevel; // 1: 대분류, 2: 중분류, 3: 소분류
    private Integer parentCategoryId;
    private List<CategoryTreeDTO> children; // 하위 카테고리(트리 구조)

    /**
     * 주어진 카테고리 ID에 대해 대/중/소분류명을 반환합니다.
     * @param categoryId 찾고자 하는 카테고리 ID
     * @param allCategories 전체 카테고리 flat 리스트
     * @return [main, mid, sub] 순서의 String 배열
     */
    public static String[] getCategoryNames(Integer categoryId, List<CategoryTreeDTO> allCategories) {
        String main = null, mid = null, sub = null;
        CategoryTreeDTO current = allCategories.stream()
            .filter(c -> c.getCategoryId().equals(categoryId))
            .findFirst()
            .orElse(null);
        while (current != null) {
            if (current.getCategoryLevel() == 3) {
                sub = current.getCategoryName();
            } else if (current.getCategoryLevel() == 2) {
                mid = current.getCategoryName();
            } else if (current.getCategoryLevel() == 1) {
                main = current.getCategoryName();
            }
            Integer parentId = current.getParentCategoryId();
            current = parentId != null ? allCategories.stream()
                .filter(c -> c.getCategoryId().equals(parentId))
                .findFirst()
                .orElse(null) : null;
        }
        return new String[] {main, mid, sub};
    }

    /**
     * 소분류(leaf)인지 여부
     */
    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    /**
     * 트리 구조를 flat 리스트(1차원 배열)로 변환 (재귀)
     */
    public static void toFlatList(CategoryTreeDTO node, List<CategoryTreeDTO> flat) {
        flat.add(node);
        if (node.getChildren() != null) {
            for (CategoryTreeDTO child : node.getChildren()) {
                toFlatList(child, flat);
            }
        }
    }

    /**
     * 트리에서 특정 카테고리ID로 노드 찾기 (재귀)
     */
    public static CategoryTreeDTO findById(CategoryTreeDTO root, Integer categoryId) {
        if (root.getCategoryId().equals(categoryId)) return root;
        if (root.getChildren() != null) {
            for (CategoryTreeDTO child : root.getChildren()) {
                CategoryTreeDTO found = findById(child, categoryId);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * 프론트 전송용 최소 정보 DTO 변환
     */
    public SimpleCategoryDTO toSimpleDto() {
        return new SimpleCategoryDTO(categoryId, categoryName, parentCategoryId);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SimpleCategoryDTO {
        private Integer categoryId;
        private String categoryName;
        private Integer parentCategoryId;
    }

    /**
     * "대분류 > 중분류 > 소분류" 전체 경로 문자열 반환
     */
    public static String getCategoryPath(Integer categoryId, List<CategoryTreeDTO> allCategories) {
        String[] names = getCategoryNames(categoryId, allCategories);
        StringBuilder sb = new StringBuilder();
        for (String name : names) {
            if (name != null) {
                if (sb.length() > 0) sb.append(" > ");
                sb.append(name);
            }
        }
        return sb.toString();
    }

    /**
     * 특정 레벨의 모든 카테고리만 flat하게 추출 (예: level=1 대분류만)
     */
    public static List<CategoryTreeDTO> filterByLevel(List<CategoryTreeDTO> flatList, int level) {
        List<CategoryTreeDTO> result = new ArrayList<>();
        for (CategoryTreeDTO c : flatList) {
            if (c.getCategoryLevel() != null && c.getCategoryLevel() == level) {
                result.add(c);
            }
        }
        return result;
    }
}
