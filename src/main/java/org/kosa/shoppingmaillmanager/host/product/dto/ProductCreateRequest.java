// DTO(Data Transfer Object)란?
// - 계층 간(예: 프론트엔드 ↔ 백엔드, 컨트롤러 ↔ 서비스) 데이터 전달을 위한 객체입니다.
// - 엔티티(테이블 매핑 객체)와 분리해서, 네트워크 전송/입력/출력에 최적화된 구조로 사용합니다.
// - 기능(등록/수정/조회 등)별로 필요한 필드만 포함시켜 별도 DTO를 만드는 것이 실무 표준입니다.

package org.kosa.shoppingmaillmanager.host.product.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProductCreateRequest {
    // 카테고리ID (대/중/소 중 마지막 선택)
    private Integer categoryId;
    // 상품명
    private String name;
    // 상품 가격
    private Integer price;
    // 판매가 (할인 등 적용된 실제 판매가격)
    private Integer salePrice;
    // 간단설명 (리스트/검색용)
    private String productShortDescription;
    // 상세설명 (HTML, 에디터 내용)
    private String productDescription;
    // 재고수량
    private Integer stock;
    // 상품 상태 (예: ACTIVE, SOLD_OUT 등)
    private String productStatus;
    // 대표이미지 파일 (프론트에서 name="mainImage"로 전송)
    private MultipartFile mainImage;
    // 옵션 정보 (JSON 문자열로 받아서 파싱)
    private String options; // 옵션 정보(JSON 문자열)
    // 상세이미지(에디터 이미지 등), 필요시 MultipartFile[] detailImages; 도 추가 가능
}
