package org.kosa.shoppingmaillmanager.host.product;

import java.util.*;
import java.util.stream.Collectors;

import org.kosa.shoppingmaillmanager.host.product.category.CategoryDAO;
import org.kosa.shoppingmaillmanager.host.product.category.CategoryTreeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryDAO categoryDAO; // MyBatis DAO

    public Page<ProductDTO> getAllProducts(Pageable pageable, String status, Long categoryId, String keyword) {
        System.out.println("검색 쿼리 실행! keyword=" + keyword);

        // 전체 카테고리 트리 한 번만 조회
        List<CategoryTreeDTO> allCategories = categoryDAO.selectCategoryTree();

        List<Long> categoryIds = null;
        if (categoryId != null) {
            // 하위 카테고리 포함 모든 카테고리 ID 수집
            categoryIds = findAllDescendantCategoryIds(categoryId, allCategories);
        }

        Page<Product> page;

        // --- 검색 조건 추가 ---
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 카테고리, 상태, 키워드 모두 있는 경우
            if (categoryIds != null && !categoryIds.isEmpty()) {
                List<String> categoryIdsStr = categoryIds.stream().map(String::valueOf).collect(Collectors.toList());

                if (status != null && !status.isEmpty() && !status.equals("ALL")) {
                    page = productRepository.findByCategoryIdInAndProductStatusAndKeyword(
                        categoryIdsStr, status, keyword, pageable);
                } else {
                    page = productRepository.findByCategoryIdInAndKeyword(
                        categoryIdsStr, keyword, pageable);
                }
            } else {
                if (status != null && !status.isEmpty() && !status.equals("ALL")) {
                    page = productRepository.findByProductStatusAndKeyword(
                        status, keyword, pageable);
                } else {
                    page = productRepository.findByKeyword(keyword, pageable);
                }
            }
        } else {
            // 기존 로직 (키워드 없을 때)
            if (categoryIds != null && !categoryIds.isEmpty()) {
                List<String> categoryIdsStr = categoryIds.stream().map(String::valueOf).collect(Collectors.toList());

                if (status != null && !status.isEmpty() && !status.equals("ALL")) {
                    page = productRepository.findByCategoryIdInAndProductStatus(
                        categoryIdsStr, status, pageable);
                } else {
                    page = productRepository.findByCategoryIdIn(
                        categoryIdsStr, pageable);
                }
            } else {
                if (status != null && !status.isEmpty() && !status.equals("ALL")) {
                    page = productRepository.findByProductStatus(status, pageable);
                } else {
                    page = productRepository.findAll(pageable);
                }
            }
        }

        // 상품 → DTO 변환시 카테고리명 세팅 (유틸리티 메서드 활용)
        return page.map(product -> {
            ProductDTO dto = ProductDTO.fromEntity(product);
            // 카테고리명 세팅
            if (product.getCategoryId() != null) {
                try {
                    String[] names = CategoryTreeDTO.getCategoryNames(Long.valueOf(product.getCategoryId()), allCategories);
                    dto.setMainCategoryName(names[0]);
                    dto.setMidCategoryName(names[1]);
                    dto.setSubCategoryName(names[2]);
                } catch (Exception e) {
                    // 예외 발생 시 카테고리명은 null로 둠
                }
            }
            // 진열여부 세팅 (displayYn → displayStatus로)
            dto.setDisplayYn(product.getDisplayYn());
            return dto;
        });
    }

    private List<Long> findAllDescendantCategoryIds(Long rootId, List<CategoryTreeDTO> allCategories) {
        Set<Long> result = new HashSet<>();
        findDescendants(rootId, allCategories, result);
        return new ArrayList<>(result);
    }

    private void findDescendants(Long parentId, List<CategoryTreeDTO> allCategories, Set<Long> result) {
        result.add(parentId);
        for (CategoryTreeDTO cat : allCategories) {
            if (parentId.equals(cat.getParentCategoryId())) {
                findDescendants(cat.getCategoryId(), allCategories, result);
            }
        }
    }

    public Map<String, Long> getProductStatusCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("ALL", productRepository.count());
        counts.put("ACTIVE", productRepository.countByProductStatus("ACTIVE"));
        counts.put("INACTIVE", productRepository.countByProductStatus("INACTIVE"));
        counts.put("SOLD_OUT", productRepository.countByProductStatus("SOLD_OUT"));
        return counts;
    }

    // 진열여부 토글 API
    @Transactional
    public void updateDisplayYn(String productId, String displayYn) {
        int updated = productRepository.updateDisplayYn(productId, displayYn);
        if (updated == 0) {
            throw new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId);
        }
    }
}
