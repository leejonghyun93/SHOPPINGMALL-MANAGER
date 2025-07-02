package org.kosa.shoppingmaillmanager.host.product.dto;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class CategoryTreeDTO {
	private Long categoryId;
	private String categoryName;
	private Integer categoryLevel;
	private Long parentCategoryId;

	private List<CategoryTreeDTO> children; // 자식 카테고리들

	public static String getCategoryPath(Integer targetId, List<CategoryTreeDTO> flatList) {
		// id → dto 매핑
		Map<Long, CategoryTreeDTO> map = new HashMap<>();
		for (CategoryTreeDTO dto : flatList) {
			map.put(dto.getCategoryId(), dto);
		}

		List<String> path = new LinkedList<>();
		CategoryTreeDTO current = map.get(targetId.longValue());

		// 위로 거슬러 올라가면서 이름 쌓기
		while (current != null) {
			path.add(0, current.getCategoryName());
			current = map.get(current.getParentCategoryId());
		}

		return String.join(" > ", path); // 예: 식품 > 국/탕/찌개
	}
}
