package org.kosa.shoppingmaillmanager.host.broadcast;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BroadCastRegisterResult {
	private BroadCast saved;
	private String stream_key;
}
