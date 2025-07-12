package org.kosa.shoppingmaillmanager.user;

import java.util.List;

import org.kosa.shoppingmaillmanager.host.order.OrderListDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    private String user_id;         // 사용자 ID (PK)
    private String password;        // 비밀번호
    private String name;            // 이름
    private String email;           // 이메일
    private String phone;           // 전화번호
    private String zipcode;         // 우편번호
    private String address;         // 주소
    private String birth_date;      // 생년월일
    private String gender;          // 성별
    private String succession_yn;   // 승계 여부 (Y/N)
    private String blacklisted;     // 블랙리스트 여부 (Y/N)
    private String created_date;    // 생성일
    private String session_date;    // 세션 유지 시간 기준
    private int login_fail_cnt;     // 로그인 실패 횟수
    private String status;          // 계정 상태 (예: ACTIVE)
    private String last_login;      // 마지막 로그인 시간
    private String marketing_agree; // 마케팅 수신 동의 여부 (Y/N)
    private String social_id;       // 소셜 로그인 식별자 (카카오, 구글 등)
    private String marketing_agent; // 마케팅 유입 채널
    private String grade_id;        // 등급 ID (외래키로 다른 등급 테이블과 연결 가능)
    private String updated_date;    // 회원 정보 수정일 (자동 업데이트)
    private String myaddress;       // 상세 주소
    private String secession_yn;    // 탈퇴 여부 (Y/N)
    private String secession_date;  // 탈퇴 처리 날짜
    private String profile_img;     // 프로필 이미지 URL
    private String social_type;     // 소셜 로그인 타입 (예: KAKAO, GOOGLE)
    private String nickname;		// 닉네임

    // 아래는 호스트 테이블의 컬럼
    private int host_id;				// 호스트 아이디
    private String business_no;     // 사업자 등록 번호
    private String bank_name;       // 은행명
    private String account_no;      // 계좌번호
    private String channel_name;    // 라이브 방송 채널명
    private String intro;           // 소개글
    private String approved_yn;     // 관리자 승인 여부 (Y/N)
    
    // 아래는 관리자 테이블의 컬럼
    private String access_level;		// 권한 등급
    
    private List<OrderListDTO> orderList;	// 주문 목록
}