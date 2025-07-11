package org.kosa.shoppingmaillmanager.host.product;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.kosa.shoppingmaillmanager.host.product.dto.LowStockProductDto;
import org.kosa.shoppingmaillmanager.host.product.dto.LowStockProductSummaryDto;
import org.kosa.shoppingmaillmanager.host.product.dto.PopularProductDto;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductOptionDto;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductRequestDto;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductSearchCondition;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductSimpleDTO;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductStatusDto;
import org.kosa.shoppingmaillmanager.host.product.dto.ProductUpdateDto;
import org.kosa.shoppingmaillmanager.host.product.entity.Product;
import org.kosa.shoppingmaillmanager.host.product.entity.ProductOption;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

	private final ProductDAO productDAO;
	private final HostDAO hostDAO;
	private final FileStorageService fileStorageService;

	public ProductListResponse getProductList(String userId, ProductSearchCondition cond) {
		String hostId = hostDAO.findHostIdByUserId(userId);
		if (hostId == null) {
			throw new IllegalArgumentException("해당 유저의 호스트 ID를 찾을 수 없습니다.");
		}

		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("hostId", hostId);
		paramMap.put("status", cond.getStatus());
		paramMap.put("categoryId", cond.getCategoryId());
		paramMap.put("keyword", cond.getKeyword());
		paramMap.put("offset", (cond.getPage() - 1) * cond.getSize());
		paramMap.put("limit", cond.getSize());
		paramMap.put("sort", cond.getSort());

		List<ProductSimpleDTO> content = productDAO.selectProductList(paramMap);
		long total = productDAO.countProductList(paramMap);

		Map<String, Long> statusCounts = new HashMap<>();
		long totalCount = 0;
		List<Map<String, Object>> rawCounts = productDAO.countProductStatusMapRaw(hostId);
		for (Map<String, Object> row : rawCounts) {
			String status = (String) row.get("status");
			Long count = ((Number) row.get("cnt")).longValue();
			statusCounts.put(status, count);
			totalCount += count;
		}
		statusCounts.put("전체", totalCount);

		return new ProductListResponse(content, total, statusCounts);
	}

	// ✅ 진열여부 변경 - host 체크 포함
	public void updateDisplayYn(String userId, Integer productId, String displayYn) {
		String hostId = hostDAO.findHostIdByUserId(userId);
		if (hostId == null) {
			throw new IllegalArgumentException("호스트 정보를 찾을 수 없습니다.");
		}
		// 소유권 검증 (추가적으로 selectProductById로 조회할 수도 있음)
		ProductSimpleDTO dto = productDAO.selectProductById(hostId, productId);
		if (dto == null) {
			throw new NoSuchElementException("본인의 상품이 아니거나 존재하지 않습니다.");
		}
		productDAO.updateDisplayYn(productId, displayYn);
	}

	public ProductSimpleDTO getProductDetail(String userId, Integer productId) {
		String hostId = hostDAO.findHostIdByUserId(userId);
		ProductSimpleDTO dto = productDAO.selectProductById(hostId, productId);
		if (dto == null) {
			throw new NoSuchElementException("상품을 찾을 수 없습니다.");
		}
		
		List<ProductOptionDto> options = productDAO.findOptionsByProductId(productId);
	    dto.setOptions(options);
		
		return dto;
	}

	public void updateProductField(String userId, Integer productId, Map<String, Object> updates) {
		String hostId = hostDAO.findHostIdByUserId(userId);
		ProductSimpleDTO dto = productDAO.selectProductById(hostId, productId);
		if (dto == null) {
			throw new NoSuchElementException("본인의 상품이 아니거나 존재하지 않습니다.");
		}
		productDAO.updateProductField(hostId, productId, updates);
	}
	
	@Transactional
    public void registerProduct(String userId, ProductRequestDto dto, MultipartFile mainImage) throws IOException {
        // ✅ 1. userId → hostId 조회 및 검증
        String hostId = hostDAO.findHostIdByUserId(userId);
        if (hostId == null) {
            throw new IllegalArgumentException("호스트 정보가 존재하지 않습니다.");
        }

        
		// ✅ 2. 대표 이미지 저장
        String imagePath = fileStorageService.store(mainImage);

        // ✅ 3. 상품 등록
        Product product = Product.builder()
                .hostId(hostId)
                .categoryId(dto.getCategoryId())
                .name(dto.getName())
                .price(dto.getPrice())
                .salePrice(dto.getSalePrice())
                .stock(dto.getStock())
                .productStatus(dto.getProductStatus())
                .productShortDescription(dto.getProductShortDescription())
                .productDescription(dto.getProductDescription())
                .mainImage(imagePath)
                .build();
        productDAO.insertProduct(product);

        // ✅ 4. 옵션 등록
        if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
            for (ProductOptionDto optionDto : dto.getOptions()) {
                ProductOption option = ProductOption.builder()
                        .productId(product.getProductId())
                        .optionName(optionDto.getOptionName())
                        .salePrice(optionDto.getSalePrice())
                        .stock(optionDto.getStock())
                        .status(optionDto.getStatus())
                        .build();
                productDAO.insertProductOption(option);
            }
        }
    }
	
	@Transactional
	public void updateProduct(String userId, Integer productId, ProductUpdateDto dto) throws IOException {
	    // 1. userId → hostId 조회
	    String hostId = hostDAO.findHostIdByUserId(userId);
	    if (hostId == null) throw new IllegalArgumentException("호스트 정보가 없습니다.");

	    // 2. 상품 존재 여부 확인
	    Product existing = productDAO.findProductById(productId);
	    if (existing == null) throw new IllegalArgumentException("상품이 존재하지 않습니다.");
	    if (!existing.getHostId().equals(hostId)) throw new SecurityException("수정 권한이 없습니다.");

	    // 3. 대표 이미지 교체 시 저장
	    String imagePath = existing.getMainImage();
	    if (dto.getMainImage() != null && !dto.getMainImage().isEmpty()) {
	        imagePath = fileStorageService.store(dto.getMainImage());
	    }

	    // 4. 상품 업데이트
	    Product updated = Product.builder()
	            .productId(productId)
	            .hostId(hostId)
	            .categoryId(dto.getCategoryId())
	            .name(dto.getName())
	            .price(dto.getPrice())
	            .salePrice(dto.getSalePrice())
	            .stock(dto.getStock())
	            .productStatus(dto.getProductStatus())
	            .productShortDescription(dto.getProductShortDescription())
	            .productDescription(dto.getProductDescription())
	            .mainImage(imagePath)
	            .build();
	    productDAO.updateProduct(updated);

	    // 5. 옵션 전체 삭제 후 재등록
	    productDAO.deleteProductOptionsByProductId(productId);
	    if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
	        for (ProductOptionDto optionDto : dto.getOptions()) {
	            ProductOption option = ProductOption.builder()
	                    .productId(productId)
	                    .optionName(optionDto.getOptionName())
	                    .salePrice(optionDto.getSalePrice())
	                    .stock(optionDto.getStock())
	                    .status(optionDto.getStatus())
	                    .build();
	            productDAO.insertProductOption(option);
	        }
	    }
	}
	
	public LowStockProductSummaryDto getLowStockProducts(String userId) {
	    // 1. hostId 조회
	    String hostId = hostDAO.findHostIdByUserId(userId);
	    if (hostId == null) {
	        throw new IllegalArgumentException("호스트 정보를 찾을 수 없습니다.");
	    }

	    // 2. DAO로부터 데이터 조회
	    List<LowStockProductDto> products = productDAO.findLowStockProducts(hostId);
	    int totalCount = productDAO.countLowStockProducts(hostId);

	    // 3. 응답 DTO 조립
	    LowStockProductSummaryDto result = new LowStockProductSummaryDto();
	    result.setProducts(products);
	    result.setTotalCount(totalCount);
	    return result;
	}
	
	public List<PopularProductDto> getPopularProducts(String userId) {
	    // 1. hostId 조회
	    String hostId = hostDAO.findHostIdByUserId(userId);
	    if (hostId == null) {
	        throw new IllegalArgumentException("호스트 정보를 찾을 수 없습니다.");
	    }

	    // 2. 인기 상품 목록 조회 (판매량 기준 Top 5)
	    return productDAO.findPopularProducts(hostId);
	}

	public ProductStatusDto getProductStatus(String userId) {
	    // 1. hostId 조회
	    String hostId = hostDAO.findHostIdByUserId(userId);
	    if (hostId == null) {
	        throw new IllegalArgumentException("호스트 정보를 찾을 수 없습니다.");
	    }

	    // 2. 상태별 상품 수 조회
	    return productDAO.countProductStatus(hostId);
	}
}
