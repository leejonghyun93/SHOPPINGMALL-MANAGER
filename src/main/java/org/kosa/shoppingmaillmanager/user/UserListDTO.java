package org.kosa.shoppingmaillmanager.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserListDTO {
	// 검색조건 + 페이징조건
	private int pageNo = 1;			// 페이지 번호
    private int size = 10;				// 한 페이지 보여줄 데이터 수
    private String searchColumn;		// 회원
    private String searchValue;
    private String sortOption;
    
    // 컬럼명
    private String user_id;
    private String name;
    private String nickname;
    private String email;
    private String phone;
    private String address;
    private String gender;
    private String created_date;
    private String blacklisted;
    private String status;
    private String grade_id;
    private String secession_yn;
    private String secession_date;
    
    // 회원관리 메뉴구분 필터
    private String filterType; //  "normal", "locked", "withdrawn", "host"
    
    // 일반회원 조회용
    private boolean excludeAdminAndHost = false; // 내부 플래그
    
    // 회원관리 주소 구분
    public void applyFilterType() {
        if ("normal".equals(filterType)) {  // 일반회원 조회
            this.excludeAdminAndHost = true;
            this.blacklisted = "N";
            this.status = "Y";
            this.secession_yn = "N";
        } else if ("locked".equals(filterType)) {  // 잠긴회원 조회
            this.blacklisted = "Y";
            this.status = "N";
            this.secession_yn = "N";
        } else if ("widthdrawn".equals(filterType)) {  // 탈퇴회원 조회
            this.secession_yn = "Y";
        } else if ("host".equals(filterType)) {  // 호스트 조회
            this.grade_id = "HOST";
            this.blacklisted = "N";
            this.status = "Y";
            this.secession_yn = "N";
        }
    }
}
