package org.kosa.shoppingmaillmanager.host.broadcast;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BroadCastDAO {
	public int insert(BroadCast broadCast);
	public List<BroadCastProduct> selectByBroadcastId(@Param("keyword") String keyword);
	public int insertProduct(BroadCastProduct product);
	public List<BroadCastProduct> findByKeyword(@Param("keyword") String keyword);
	public BroadCast findBroadcastById(@Param("broadcast_id") int broadcast_id);
    public List<BroadCastProduct> findProductsByBroadcastId(@Param("broadcast_id") int broadcastId);
    public List<BroadCastViewer> findViewersByBroadcastId(@Param("broadcast_id") int broadcastId);
	public BroadCast findById(int broadcast_id);
	public void updateStatus(BroadCast broadCast);
	public void updateBroadcastCategoryByTopProductCategory(int broadcastId);
	
	public void insertViewer(BroadCastViewer viewer);
	public void updateLeftTime(@Param("user_id") String user_id, @Param("broadcast_id") int broadcast_id);
	public void updateTotalViewersManual(@Param("broadcast_id" )int broadcast_id, @Param("total") long total);
	public List<BroadCastListDTO> findBroadcastList(Map<String, Object> map);
	public int countBroadcastList(Map<String, Object> map);
	public String findCategoryName(@Param("category_id") Long category_id);
	public void updateVideoUrl(@Param("broadcast_id") int broadcastId, @Param("video_url") String videoUrl);
	public void updateStreamUrl(BroadCast broadCast);
}
