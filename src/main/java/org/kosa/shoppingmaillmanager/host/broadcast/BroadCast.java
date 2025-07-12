package org.kosa.shoppingmaillmanager.host.broadcast;

import java.util.List;

import org.kosa.shoppingmaillmanager.host.product.dto.CategoryTreeDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BroadCast {
	private int broadcast_id;              // 라이브 방송 고유 ID
	private String broadcaster_id;          // 방송자 ID
	private String title;                   // 방송 제목
	private String description;             // 방송 설명
	private String broadcast_status;        // 방송 상태 (scheduled, starting, live, paused, ended, cancelled)
	private String scheduled_start_time;    // 예정 시작 시간
	private String scheduled_end_time;      // 예정 종료 시간
	private String actual_start_time;       // 실제 시작 시간
	private String actual_end_time;         // 실제 종료 시간
	private Boolean is_public;              // 공개 여부
	private int max_viewers;                // 최대 시청자 수 (0은 무제한)
	private int current_viewers;            // 현재 시청자 수
	private int total_viewers;              // 총 시청자 수
	private int peak_viewers;               // 최대 동시 시청자 수
	private int like_count;                 // 좋아요 수
	private String thumbnail_url;           // 썸네일 이미지 URL
	private String stream_url;              // 스트림 URL
	private Long category_id;                // 카테고리 ID (tb_category 참조)
	private String tags;                    // 태그 목록 (콤마 구분)
	private String created_at;              // 생성일시
	private String updated_at;              // 수정일시
	private String stream_key; 			// 스트림 key
	private String video_url;				// 비디오 url
	
	private String category_name;		// 카테고리명

	// OBS 연결 관련 필드
	private String obs_host; 				// obs 설치된 pc의 ip주소
	private int obs_port;						// obs Websocket 포트번호
	private String obs_password;			// obs Websocket 비밀번호
	
	// nginx 서버 관련 필드
	private String nginx_host;				// docker 설치된 ip 주소
	
	// 상품 목록
	private List<BroadCastProduct> productList;
	
	// 시청자 목록
	private List<BroadCastViewer> viewerList;
	
	// 카테고리 불러오기
	private CategoryTreeDTO category;
}
