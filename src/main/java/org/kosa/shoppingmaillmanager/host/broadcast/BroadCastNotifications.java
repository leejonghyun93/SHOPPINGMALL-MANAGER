package org.kosa.shoppingmaillmanager.host.broadcast;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BroadCastNotifications {
	private int notification_id;        // 알림 고유 ID
	private int broadcast_id;           // 관련 방송 ID
	private String user_id;             // 알림 받을 사용자 ID
	private String type;                // 알림 타입 (broadcast_start, broadcast_scheduled, broadcast_reminder, broadcast_ended)
	private String title;               // 알림 제목
	private String message;             // 알림 내용
	private boolean is_read;            // 읽음 여부
	private boolean is_sent;            // 발송 여부
	private String priority;            // 우선순위 (low, normal, high)
	private String created_at;          // 알림 생성 시간
	private String sent_at;             // 발송 시간
	private String read_at;             // 읽은 시간
}
