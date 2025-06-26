package org.kosa.shoppingmaillmanager.host.broadcast;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BroadCastStatusHistory {
	private int history_id;            // 방송 상태 변경 이력 고유 ID
	private int broadcast_id;          // 방송 ID
	private String previous_status;    // 이전 상태
	private String new_status;         // 새로운 상태
	private String changed_by;         // 상태 변경한 사용자 ID
	private String change_reason;      // 변경 사유
	private String created_at;         // 변경 시각
}
