package org.kosa.shoppingmaillmanager.host.broadcast;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BroadCastListDTO {
	private int pageNo = 1;
	private int size = 10;
	private String searchValue;
	
	private Integer broadcast_id;
	private String title;
	private String broadcaster_id;
	private String created_at;
	private Integer current_viewers;
	private Long category_id;
	private String category_name;
}
