package org.kosa.shoppingmaillmanager.host.product;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import org.kosa.shoppingmaillmanager.host.product.category.CategoryDAO;
import org.kosa.shoppingmaillmanager.host.product.category.CategoryTreeDTO;
import org.kosa.shoppingmaillmanager.host.product.dto.OptionDTO;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductCreateRequest;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductDTO;
import org.kosa.shoppingmaillmanager.host.product.entity.Product;
import org.kosa.shoppingmaillmanager.host.product.entity.ProductOption;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;

    private static final String MAIN_IMAGE_UPLOAD_DIR = "C:/upload/product/main/";

    private List<CategoryTreeDTO> cachedCategoryTree;

    private List<CategoryTreeDTO> getCategoryTreeCached() {
        if (cachedCategoryTree == null) {
            cachedCategoryTree = categoryDAO.selectCategoryTree();
        }
        return cachedCategoryTree;
    }

    public ProductListResponse getAllProducts(int page, int size, String status, Integer categoryId, String keyword) {
        List<CategoryTreeDTO> allCategories = getCategoryTreeCached();

        int offset = Math.max(0, (page - 1) * size);
        Map<String, Object> params = new HashMap<>();
        params.put("offset", offset);
        params.put("limit", size);

        if (categoryId != null) {
            params.put("categoryIds", findAllDescendantCategoryIds(categoryId, allCategories));
        }
        if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
            params.put("status", status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            params.put("keyword", keyword.trim());
        }

        List<ProductDTO> content = productDAO.selectProductListWithCategory(params);

        params.remove("offset");
        params.remove("limit");
        int totalElements = productDAO.selectProductListWithCategory(params).size();

        Map<String, Long> statusCounts = getProductStatusCounts();

        return new ProductListResponse(content, totalElements, statusCounts);
    }

    private List<Integer> findAllDescendantCategoryIds(Integer rootId, List<CategoryTreeDTO> allCategories) {
        Set<Integer> result = new HashSet<>();
        findDescendants(rootId, allCategories, result);
        return new ArrayList<>(result);
    }

    private void findDescendants(Integer parentId, List<CategoryTreeDTO> allCategories, Set<Integer> result) {
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

    @Transactional
    public void updateDisplayYn(Integer productId, String displayYn) {
        int updated = productRepository.updateDisplayYn(productId, displayYn);
        if (updated == 0) {
            throw new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId);
        }
    }

    public ProductDTO getProductById(Integer productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. productId=" + productId));

        ProductDTO dto = ProductDTO.fromEntity(product);
        List<CategoryTreeDTO> allCategories = getCategoryTreeCached();

        if (product.getCategoryId() != null) {
            try {
                String[] names = CategoryTreeDTO.getCategoryNames(product.getCategoryId(), allCategories);
                dto.setMainCategoryName(names[0]);
                dto.setMidCategoryName(names[1]);
                dto.setSubCategoryName(names[2]);
            } catch (Exception e) {
                // 카테고리명 예외 무시
            }
        }
        dto.setDisplayYn(product.getDisplayYn());
        return dto;
    }

    @Transactional
    public void updateProductField(Integer productId, Map<String, Object> updates) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. productId=" + productId));

        updates.forEach((k, v) -> {
            switch (k) {
                case "stock" -> product.setStock((Integer) v);
                case "salePrice" -> product.setSalePrice((Integer) v);
                case "status" -> product.setProductStatus((String) v);
                // 추가 필드 필요시 확장
            }
        });

        productRepository.save(product);
    }

    @Transactional
    public void createProduct(ProductCreateRequest req) {

        String mainImageUrl = null;
        MultipartFile mainImage = req.getMainImage();

        if (mainImage != null && !mainImage.isEmpty()) {
            String ext = StringUtils.getFilenameExtension(mainImage.getOriginalFilename());
            String uuidFileName = UUID.randomUUID() + (ext != null ? "." + ext : "");
            File dest = new File(MAIN_IMAGE_UPLOAD_DIR, uuidFileName);

            try {
                mainImage.transferTo(dest);
                mainImageUrl = "/upload/product/main/" + uuidFileName;
            } catch (IOException e) {
                throw new RuntimeException("대표이미지 파일 저장 실패", e);
            }
        }

        Product product = new Product();
        product.setHostId(1L);
        product.setCategoryId(req.getCategoryId());
        product.setName(req.getName());
        product.setPrice(req.getPrice());
        product.setSalePrice(req.getSalePrice());
        product.setProductShortDescription(req.getProductShortDescription());
        product.setProductDescription(req.getProductDescription()); // HTML 그대로 저장
        product.setStock(req.getStock());
        product.setProductStatus(req.getProductStatus());
        product.setCreatedDate(LocalDateTime.now());
        product.setMainImage(mainImageUrl);
        product.setDisplayYn("Y");

        productRepository.save(product);

        // 옵션 저장
        if (req.getOptions() != null && !req.getOptions().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<OptionDTO> options = mapper.readValue(req.getOptions(), new TypeReference<>() {});
                for (OptionDTO opt : options) {
                    ProductOption option = new ProductOption();
                    option.setProductId(product.getProductId());
                    option.setOptionName(opt.getOptionName());
                    option.setSalePrice(opt.getSalePrice());
                    option.setStock(opt.getStock());
                    option.setStatus(opt.getStatus());
                    productOptionRepository.save(option);
                }
            } catch (Exception e) {
                throw new RuntimeException("옵션 파싱/저장 실패", e);
            }
        }
    }
}
