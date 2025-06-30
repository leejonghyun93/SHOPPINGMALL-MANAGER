package org.kosa.shoppingmaillmanager.host.broadcast;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BroadCastViewer {
	private int viewer_id;              // 시청자 고유 ID
	private int broadcast_id;           // 해당 방송 ID
	private String user_id;             // 시청자 ID (NULL이면 익명 시청자)
	private String username;            // 시청자명
	private String ip_address;          // IPv4/IPv6 주소
	private String user_agent;          // 브라우저 정보
	private String device_type;         // 디바이스 타입 (desktop, mobile, tablet, tv)
	private String joined_at;           // 입장 시간
	private String left_at;             // 퇴장 시간
	private int watch_duration;         // 시청 시간 (초)
	private boolean is_active;          // 현재 시청 중 여부
	private String last_activity_at;    // 마지막 활동 시간
}
