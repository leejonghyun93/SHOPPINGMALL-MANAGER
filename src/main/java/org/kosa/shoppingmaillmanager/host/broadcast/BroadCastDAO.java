package org.kosa.shoppingmaillmanager.host.broadcast;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BroadCastDAO {

	public BroadCast insert(BroadCast broadCast);
	public List<BroadCastProduct> selectByBroadcastId(@Param("broadcast_id") int broadcastId);
	public BroadCastProduct insertProduct(BroadCastProduct product);

}
