package org.kosa.shoppingmaillmanager.host.product.category;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryTreeDTO {
    private Long categoryId;
    private String categoryName;
    private Integer categoryLevel; // 1: 대분류, 2: 중분류, 3: 소분류
    private Long parentCategoryId;
    private List<CategoryTreeDTO> children; // 하위 카테고리(트리 구조)

    /**
     * 주어진 카테고리 ID에 대해 대/중/소분류명을 반환합니다.
     * @param categoryId 찾고자 하는 카테고리 ID
     * @param allCategories 전체 카테고리 flat 리스트
     * @return [main, mid, sub] 순서의 String 배열
     */
    public static String[] getCategoryNames(Long categoryId, List<CategoryTreeDTO> allCategories) {
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
            Long parentId = current.getParentCategoryId();
            current = parentId != null ? allCategories.stream()
                .filter(c -> c.getCategoryId().equals(parentId))
                .findFirst()
                .orElse(null) : null;
        }
        return new String[] {main, mid, sub};
    }
}
